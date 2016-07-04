/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.utils.http.HttpClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.SimpleEventManager;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.SingletonIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;
import org.apache.http.util.EntityUtils;

public class SparqlGraph implements GraphWithPerform {
    
    private String endpointURI;
    private String graphURI;
    private HttpClient httpClient;
    private static final Log log = LogFactory.getLog(SparqlGraph.class);
    
    private BulkUpdateHandler bulkUpdateHandler;
    private PrefixMapping prefixMapping = new PrefixMappingImpl();
    private GraphEventManager eventManager;
    
    /**
     * Returns a SparqlGraph for the union of named graphs in a remote repository 
     * @param endpointURI Endpoint URI
     */
    public SparqlGraph(String endpointURI) {
        this(endpointURI, null);
    }
    
    /**
     * Returns a SparqlGraph for a particular named graph in a remote repository 
     * @param endpointURI Endpoint URI
     * @param graphURI Graph URI
     */
    public SparqlGraph(String endpointURI, String graphURI) {
       this.endpointURI = endpointURI;
       this.graphURI = graphURI;
       
       this.httpClient = HttpClientFactory.getHttpClient();
    }
    
    public String getEndpointURI() {
        return endpointURI;
    }
    
    public String getGraphURI() {
        return graphURI;
    }

    @Override
    public void add(Triple arg0) throws AddDeniedException {
        performAdd(arg0);
    }

    public void executeUpdate(String updateString) {
        HttpPost meth = new HttpPost(endpointURI);
        try {
            meth.addHeader("Content-Type", "application/x-www-form-urlencoded");
            meth.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                    new BasicNameValuePair("update", updateString))));
            HttpResponse response = httpClient.execute(meth);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 399) {
                    log.error("response " + statusCode + " to update. \n");
                    throw new RuntimeException("Unable to perform SPARQL UPDATE: \n"
                            + updateString);
                }
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to perform SPARQL UPDATE", e);
        } finally {
            meth.abort();
        }
    }
    
    @Override
    public void performAdd(Triple t) {
        
        //log.info("adding " + t);
        
        String updateString = "INSERT DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" )  
                + sparqlNodeUpdate(t.getSubject(), "") + " " 
                + sparqlNodeUpdate(t.getPredicate(), "") + " " 
                + sparqlNodeUpdate(t.getObject(), "") + " } " 
                + ((graphURI != null) ? " } " : "");
        
        
        if (graphURI != null) {
            log.info("=====> update to graph " + graphURI);
        }
        log.info(updateString);
        
        executeUpdate(updateString);
                
    }
    
    @Override
    public void performDelete(Triple t) {
                
        String updateString = "DELETE DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" )  
                + sparqlNodeUpdate(t.getSubject(), "") + " " 
                + sparqlNodeUpdate(t.getPredicate(), "") + " " 
                + sparqlNodeUpdate(t.getObject(), "") + " } " 
                + ((graphURI != null) ? " } " : "");
        
        //log.info(updateString);
        
        executeUpdate(updateString);
    }
    
    public void removeAll() {
        // now we flush out any remaining blank nodes
        String updateString = "DELETE { ?s ?p ?o } WHERE { \n" +
                              ((getGraphURI() != null) ? ("GRAPH <" + getGraphURI() + "> { \n") : ("")) +
                              "    ?s ?p ?o \n" +
                              ((getGraphURI() != null) ? "} \n" : "") +
                              "}";
        executeUpdate(updateString);
    }
    
    @Override
    public void close() {
        // can't close a remote endpoint
    }

    @Override
    public boolean contains(Triple arg0) {
       return contains(arg0.getSubject(), arg0.getPredicate(), arg0.getObject());
    }

    @Override
    public boolean contains(Node subject, Node predicate, Node object) {
		if ((subject != null && subject.isBlank())
				|| (predicate != null && predicate.isBlank())
				|| (object != null && object.isBlank())) {
            return false;
        }
        StringBuffer containsQuery = new StringBuffer("ASK { \n");
        if (graphURI != null) {
            containsQuery.append("  GRAPH <" + graphURI + "> { ");
        }
        containsQuery.append(sparqlNode(subject, "?s"))
        .append(" ")
        .append(sparqlNode(predicate, "?p"))
        .append(" ")
        .append(sparqlNode(object, "?o"));
        if (graphURI != null) {
            containsQuery.append(" } \n");
        }
        containsQuery.append("\n}");
        boolean result = execAsk(containsQuery.toString());
        return result;
    }

    @Override
    public void delete(Triple arg0) throws DeleteDeniedException {
        performDelete(arg0);
    }

	@Override
	public void clear() {
		removeAll();
	}

	@Override
	public void remove(Node subject, Node predicate, Node object) {
		for (Triple t: find(subject, predicate, object).toList()) {
			delete(t);
		}
	}

    @Override
    public boolean dependsOn(Graph arg0) {
        return false; // who knows?
    }

    @Override
    public ExtendedIterator<Triple> find(TripleMatch arg0) {
        //log.info("find(TripleMatch) " + arg0);
        Triple t = arg0.asTriple();
        return find(t.getSubject(), t.getPredicate(), t.getObject());
    }

    public static String sparqlNode(Node node, String varName) {
        if (node == null || node.isVariable()) {
            return varName;
        } else if (node.isBlank()) {
            return "<fake:blank>"; // or throw exception?
        } else if (node.isURI()) {
            StringBuffer uriBuff = new StringBuffer();
            return uriBuff.append("<").append(node.getURI()).append(">").toString();
        } else if (node.isLiteral()) {
            StringBuffer literalBuff = new StringBuffer();
            literalBuff.append("\"");
            pyString(literalBuff, node.getLiteralLexicalForm());
            literalBuff.append("\"");
            if (node.getLiteralDatatypeURI() != null) {
                literalBuff.append("^^<").append(node.getLiteralDatatypeURI()).append(">");
            } else if (node.getLiteralLanguage() != null && node.getLiteralLanguage() != "") {
                literalBuff.append("@").append(node.getLiteralLanguage());
            }
            return literalBuff.toString();
        } else {
            return varName;
        }
    }
    
    public static String sparqlNodeUpdate(Node node, String varName) {
        if (node.isBlank()) {
            return "_:" + node.getBlankNodeLabel().replaceAll("\\W", "");
        } else {
            return sparqlNode(node, varName);
        }
    }
    
    public static String sparqlNodeDelete(Node node, String varName) {
        if (node.isBlank()) {
            return "?" + node.getBlankNodeLabel().replaceAll("\\W", "");
        } else {
            return sparqlNode(node, varName);
        }
    }
    
    @Override
    public ExtendedIterator<Triple> find(Node subject, Node predicate, Node object) {
        if (!isVar(subject) && !isVar(predicate)  && !isVar(object)) {
            if (contains(subject, predicate, object)) {
                return new SingletonIterator<Triple>(new Triple(subject, predicate, object));
            } else {
                return WrappedIterator.create(Collections.<Triple>emptyIterator());
            }
        }
        StringBuffer findQuery = new StringBuffer("SELECT * WHERE { \n");
        if (graphURI != null) {
            findQuery.append("  GRAPH <" + graphURI + "> { ");
        }
        findQuery.append(sparqlNode(subject, "?s"))
        .append(" ")
        .append(sparqlNode(predicate, "?p"))
        .append(" ")
        .append(sparqlNode(object, "?o"));
        if (graphURI != null) {
            findQuery.append("  } ");
        }
        findQuery.append("\n}");
        
        String queryString = findQuery.toString();
        //log.info(queryString);
        
//        //TODO remove me
//        if (queryString.contains("individual/AI") && queryString.contains("label")) {
//            throw new RuntimeException("break!");
//        }
        
        ResultSet rs = execSelect(queryString);
        //rs = execSelect(findQuery.toString());
        //rs = execSelect(findQuery.toString());
        
        List<Triple> triplist = new ArrayList<Triple>();
        while (rs.hasNext()) {
            QuerySolution soln = rs.nextSolution();
            Triple t = new Triple(isVar(subject) ? soln.get("?s").asNode() : subject, 
                                  isVar(predicate) ? soln.get("?p").asNode() : predicate, 
                                  isVar(object) ? soln.get("?o").asNode() : object);
            //log.info(t);
            triplist.add(t);
        }
        //log.info(triplist.size() + " results");
        return WrappedIterator.create(triplist.iterator());
    }

    private boolean isVar(Node node) {
        return (node == null || node.isVariable() || node == Node.ANY);
    }
    
    @Override
    @Deprecated
    public BulkUpdateHandler getBulkUpdateHandler() {
        if (this.bulkUpdateHandler == null) {
            this.bulkUpdateHandler = new SparqlGraphBulkUpdater(this);
        }
        return this.bulkUpdateHandler;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public GraphEventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new SimpleEventManager(this);
        }
        return eventManager;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler() {
        return null;
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        return !contains(null, null, null);
    }

    @Override
    public boolean isIsomorphicWith(Graph arg0) {
        log.info("Hey dummy!");
        throw new UnsupportedOperationException("isIsomorphicWith() not supported " +
        		"by SPARQL graphs");
    }

    @Override
    public int size() {
        int size = find(null, null, null).toList().size();
        return size;
    }
    
    private final static Capabilities capabilities = new Capabilities() {
        
        @Override
		public boolean addAllowed() {
            return false;
        }
        
        @Override
        public boolean addAllowed(boolean everyTriple) {
            return false;
        }
        
        @Override
        public boolean canBeEmpty() {
            return true;
        }
        
        @Override
        public boolean deleteAllowed() {
            return false;
        }
        
        @Override
        public boolean deleteAllowed(boolean everyTriple) {
            return false;
        }
        
        @Override
        public boolean findContractSafe() {
            return true;
        }
        
        @Override
        public boolean handlesLiteralTyping() {
            return true;
        }
        
        @Override
        public boolean iteratorRemoveAllowed() {
            return false;
        }
        
        @Override
        public boolean sizeAccurate() {
            return true;
        }
    };
    
    private boolean execAsk(String queryStr) {
        Query askQuery = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, askQuery);
        try {
            return qe.execAsk();
        } finally {
            qe.close();
        }
    }
    
    private ResultSet execSelect(String queryStr) {
        
//        long startTime1 = System.currentTimeMillis();
//        try {
//            
//            RepositoryConnection conn = getConnection();
//            try {
//                GraphQuery q = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
//                q.evaluate();
//            } catch (MalformedQueryException e) {
//                throw new RuntimeException(e);
//            } finally {
//                conn.close();
//            }
//        } catch (Exception re) {
//            //log.info(re,re);
//        }
        
//        log.info((System.currentTimeMillis() - startTime1) + " to execute via sesame");
        
        Query askQuery = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, askQuery);
        try {
            return new ResultSetMem(qe.execSelect());
        } finally {
            qe.close();
        }
    }
    
    /*
     * 
     * see http://www.python.org/doc/2.5.2/ref/strings.html
     * or see jena's n3 grammar jena/src/com/hp/hpl/jena/n3/n3.g
     */ 
    protected static void pyString(StringBuffer sbuff, String s)
    {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                sbuff.append('\\') ;
                sbuff.append(c) ;
                continue ;
            }            

            // Whitespace                        
            if (c == '\n'){ sbuff.append("\\n");continue; }
            if (c == '\t'){ sbuff.append("\\t");continue; }
            if (c == '\r'){ sbuff.append("\\r");continue; }
            if (c == '\f'){ sbuff.append("\\f");continue; }                            
            if (c == '\b'){ sbuff.append("\\b");continue; }
            if( c == 7 )  { sbuff.append("\\a");continue; }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            sbuff.append(c) ;
            
//            // Unicode escapes
//            // c < 32, c >= 127, not whitespace or other specials
//            String hexstr = Integer.toHexString(c).toUpperCase();
//            int pad = 4 - hexstr.length();
//            sbuff.append("\\u");
//            for (; pad > 0; pad--)
//                sbuff.append("0");
//            sbuff.append(hexstr);
        }
    }

	@Override
	public String toString() {
		return "SparqlGraph[" + ToString.hashHex(this) + ", endpoint="
				+ endpointURI + ", name=" + ToString.modelName(graphURI) + "]";
	}
    
}
