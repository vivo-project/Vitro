/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.graph.impl.SimpleEventManager;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.Command;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.SingletonIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class RDFServiceGraph implements GraphWithPerform {

    private RDFService rdfService;
    private String graphURI;
    private static final Log log = LogFactory.getLog(RDFServiceGraph.class);

    private PrefixMapping prefixMapping = new PrefixMappingImpl();
    private GraphEventManager eventManager;

    private boolean inTransaction = false;
    private Graph additionsGraph = ModelFactory.createDefaultModel().getGraph();
    private Graph removalsGraph = ModelFactory.createDefaultModel().getGraph();

    /**
     * Returns a SparqlGraph for the union of named graphs in a remote repository
     * @param rdfService RDF Service
     */
    public RDFServiceGraph(RDFService rdfService) {
        this(rdfService, null);
    }

    /**
     * Returns a SparqlGraph for a particular named graph in a remote repository
     * @param rdfService RDFService
     * @param graphURI Graph URI
     */
    public RDFServiceGraph(RDFService rdfService, String graphURI) {
       this.rdfService = rdfService;
       this.graphURI = graphURI;
    }

    public RDFService getRDFService() {
        return this.rdfService;
    }

    public String getGraphURI() {
        return graphURI;
    }

    @Override
    public void add(Triple arg0) throws AddDeniedException {
        performAdd(arg0);
    }

    private StringBuilder serialize(StringBuilder sb, Triple t) {
        sb.append(sparqlNodeUpdate(t.getSubject(), "")).append(" ")
               .append(sparqlNodeUpdate(t.getPredicate(), "")).append(" ")
               .append(sparqlNodeUpdate(t.getObject(), "")).append(" .");
        return sb;
    }

    private synchronized void flush() {
        ChangeSet changeSet = rdfService.manufactureChangeSet();
        try {
            if(!removalsGraph.isEmpty()) {
                String removals = serializeGraph(removalsGraph);
                changeSet.addRemoval(RDFServiceUtils.toInputStream(removals),
                        RDFService.ModelSerializationFormat.N3, graphURI);
                removalsGraph.clear();
            }
            if(!additionsGraph.isEmpty()) {
                String additions = serializeGraph(additionsGraph);
                changeSet.addAddition(RDFServiceUtils.toInputStream(additions),
                        RDFService.ModelSerializationFormat.N3, graphURI);
                additionsGraph.clear();
            }
            rdfService.changeSetUpdate(changeSet);
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }

    private synchronized String serializeGraph(Graph graph) {
        StringBuilder sb = new StringBuilder();
        Iterator<Triple> tripIt = graph.find(null, null, null);
        while(tripIt.hasNext()) {
            serialize(sb.append(" \n"), tripIt.next());
        }
        return sb.toString();
    }

    @Override
    public void performAdd(Triple t) {
        if(inTransaction) {
            stageAddition(t);
        } else {
            ChangeSet changeSet = rdfService.manufactureChangeSet();
            try {
                changeSet.addAddition(RDFServiceUtils.toInputStream(serialize(new StringBuilder(), t).toString()),
                        RDFService.ModelSerializationFormat.N3, graphURI);
                rdfService.changeSetUpdate(changeSet);
            } catch (RDFServiceException rdfse) {
                throw new RuntimeException(rdfse);
            }
        }
    }

    private void stageAddition(Triple t) {
        if(removalsGraph.contains(t)) {
            removalsGraph.remove(t.getSubject(), t.getPredicate(), t.getObject());
        } else {
            additionsGraph.add(t);
        }
    }

    @Override
    public void performDelete(Triple t) {
        if(inTransaction) {
            stageDeletion(t);
        } else {
            ChangeSet changeSet = rdfService.manufactureChangeSet();
            try {
                changeSet.addRemoval(RDFServiceUtils.toInputStream(serialize(new StringBuilder(), t).toString()),
                        RDFService.ModelSerializationFormat.N3, graphURI);
                rdfService.changeSetUpdate(changeSet);
            } catch (RDFServiceException rdfse) {
                throw new RuntimeException(rdfse);
            }
        }
    }

    private synchronized void stageDeletion(Triple t) {
        if(additionsGraph.contains(t)) {
            additionsGraph.remove(t.getSubject(), t.getPredicate(), t.getObject());
        } else {
            removalsGraph.add(t);
        }
    }

    public void removeAll() {
        // only to be used with a single graph
        if (graphURI == null) {
            return;
        }
        String constructStr = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphURI + "> { ?s ?p ?o } }";
        try {
            if(inTransaction) {
                Model model = ModelFactory.createDefaultModel();
                rdfService.sparqlConstructQuery(constructStr, model);
                stageRemoveAll(model);
            } else {
                InputStream model = rdfService.sparqlConstructQuery(
                        constructStr, RDFService.ModelSerializationFormat.N3);
                ChangeSet changeSet = rdfService.manufactureChangeSet();
                changeSet.addRemoval(model, RDFService.ModelSerializationFormat.N3, graphURI);
                rdfService.changeSetUpdate(changeSet);
            }
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }

    private void stageRemoveAll(Model removals) {
        StmtIterator sit = removals.listStatements();
        while (sit.hasNext()) {
            Triple t = sit.nextStatement().asTriple();
            stageDeletion(t);
        }
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
        StringBuilder containsQuery = new StringBuilder("SELECT * WHERE { \n");
        if (graphURI != null) {
            containsQuery.append("  GRAPH <").append(graphURI).append("> { ");
        }
        containsQuery.append(sparqlNode(subject, "?s"))
        .append(" ")
        .append(sparqlNode(predicate, "?p"))
        .append(" ")
        .append(sparqlNode(object, "?o"));
        if (graphURI != null) {
            containsQuery.append(" } \n");
        }
        containsQuery.append("} \nLIMIT 1 ");

        ResultSetConsumer.HasResult consumer = new ResultSetConsumer.HasResult();
        execSelect(containsQuery.toString(), consumer);
        boolean initialResult = consumer.hasResult();
        if(!inTransaction) {
            return initialResult;
        } else {
            Triple t = Triple.create(subject, predicate, object);
            return (initialResult || additionsGraphContains(t))
                    && !removalsGraphContains(t);
        }
    }

    private synchronized boolean additionsGraphContains(Triple t) {
        return additionsGraph.contains(t);
    }

    private synchronized boolean removalsGraphContains(Triple t) {
        return removalsGraph.contains(t);
    }

    @Override
    public void delete(Triple arg0) throws DeleteDeniedException {
        performDelete(arg0);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        return find(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
	public void remove(Node subject, Node predicate, Node object) {
		for (Triple t : find(subject, predicate, object).toList()) {
			delete(t);
		}
	}

    @Override
    public boolean dependsOn(Graph arg0) {
        return false; // who knows?
    }

    public static String sparqlNode(Node node, String varName) {
        if (node == null || node.isVariable()) {
            return varName;
        } else if (node.isBlank()) {
            return "<fake:blank>"; // or throw exception?
        } else if (node.isURI()) {
            StringBuilder uriBuff = new StringBuilder();
            return uriBuff.append("<").append(node.getURI()).append(">").toString();
        } else if (node.isLiteral()) {
            StringBuffer literalBuff = new StringBuffer();
            literalBuff.append("\"");
            pyString(literalBuff, node.getLiteralLexicalForm());
            literalBuff.append("\"");
            /*
             * UQAM-Bug-Correction
             * reversing the condition tests.
             * It is important to prioritize the language typology test in order to exploit the linguistic context in testing the type of data
             */
            if (!StringUtils.isEmpty(node.getLiteralLanguage())) {
                literalBuff.append("@").append(node.getLiteralLanguage());
            } else if (node.getLiteralDatatypeURI() != null) {
                literalBuff.append("^^<").append(node.getLiteralDatatypeURI()).append(">");
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
    public ExtendedIterator<Triple> find(final Node subject, final Node predicate, final Node object) {
        if (!isVar(subject) && !isVar(predicate)  && !isVar(object)) {
            if (contains(subject, predicate, object)) {
                return new SingletonIterator<Triple>(new Triple(subject, predicate, object));
            } else {
                return WrappedIterator.create(Collections.<Triple>emptyIterator());
            }
        }
        StringBuilder findQuery = new StringBuilder("SELECT * WHERE { \n");
        if (graphURI != null) {
            findQuery.append("  GRAPH <").append(graphURI).append("> { ");
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

        final List<Triple> triplist = new ArrayList<Triple>();
        if(inTransaction) {
            addAdditions(triplist, additionsGraph.find(subject, predicate, object));
            subtractRemovals(triplist, removalsGraph.find(subject, predicate, object));
        }

        execSelect(queryString, new ResultSetConsumer() {
            @Override
            protected void processQuerySolution(QuerySolution qs) {
                Triple t = new Triple(isVar(subject) ? qs.get("?s").asNode() : subject,
                        isVar(predicate) ? qs.get("?p").asNode() : predicate,
                        isVar(object) ? qs.get("?o").asNode() : object);
                //log.info(t);
                triplist.add(t);
            }
        });

/*
        ResultSet rs = execSelect(queryString);
        while (rs.hasNext()) {
            QuerySolution soln = rs.nextSolution();
            Triple t = new Triple(isVar(subject) ? soln.get("?s").asNode() : subject,
                    isVar(predicate) ? soln.get("?p").asNode() : predicate,
                    isVar(object) ? soln.get("?o").asNode() : object);
            //log.info(t);
            triplist.add(t);
        }
*/
        //log.info(triplist.size() + " results");
        return WrappedIterator.create(triplist.iterator());
    }

    private void addAdditions(List<Triple> tripList, ExtendedIterator<Triple> tripIt) {
        while(tripIt.hasNext()) {
            Triple t = tripIt.next();
            if(!tripList.contains(t)) {
                tripList.add(t);
            }
         }
    }

    private void subtractRemovals(List<Triple> tripList, ExtendedIterator<Triple> tripIt) {
        while(tripIt.hasNext()) {
            Triple t = tripIt.next();
            if(tripList.contains(t)) {
                tripList.remove(t);
            }
         }
    }

    private boolean isVar(Node node) {
        return (node == null || node.isVariable() || node == Node.ANY);
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
        return transactionHandler;
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
        throw new UnsupportedOperationException("isIsomorphicWith() not supported " +
        		"by SPARQL graphs");
    }

    @Override
    public int size() {
        int size = find(null, null, null).toList().size();
        return size;
    }

    @Override
	public void clear() {
    	removeAll();
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

	private final TransactionHandler transactionHandler = new TransactionHandler() {
	    @Override
	    public synchronized void abort() {
	        inTransaction = false;
	        removalsGraph.clear();
	        additionsGraph.clear();
	    }

        @Override
        public synchronized void begin() {
            inTransaction = true;
        }

        @Override
        public synchronized void commit() {
            flush();
            inTransaction = false;
        }

        @Override
        public Object executeInTransaction(Command arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void execute(Runnable runnable) {

        }

        @Override
        public <T> T calculate(Supplier<T> supplier) {
            return null;
        }

        @Override
        public boolean transactionsSupported() {
            return true;
        }
    };

    private void execSelect(String queryStr, ResultSetConsumer consumer) {
        try {
            rdfService.sparqlSelectQuery(queryStr, consumer);
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }

    /*
     *
     * see http://www.python.org/doc/2.5.2/ref/strings.html
     * or see jena's n3 grammar jena/src/org.apache/jena/n3/n3.g
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

    public static Model createRDFServiceModel(final RDFServiceGraph g) {
        Model m = VitroModelFactory.createModelForGraph(g);
        m.register(new StatementListener() {
            @Override
            public void notifyEvent(Model m, Object event) {
                ChangeSet changeSet = g.getRDFService().manufactureChangeSet();
                changeSet.addPreChangeEvent(event);
                try {
                    g.getRDFService().changeSetUpdate(changeSet);
                } catch (RDFServiceException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        return m;
    }

	@Override
	public String toString() {
		return "RDFServiceGraph[" + ToString.hashHex(this) + ", " + rdfService
				+ ", graphURI=" + ToString.modelName(graphURI) + "]";
	}

}
