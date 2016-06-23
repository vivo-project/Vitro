/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class RDFServiceGraphBulkUpdater implements BulkUpdateHandler {
	private static final Log log = LogFactory.getLog(RDFServiceGraphBulkUpdater.class);

    private final RDFServiceGraph graph;
    private final GraphEventManager manager;
    
    public RDFServiceGraphBulkUpdater(RDFServiceGraph graph) {
        this.graph = graph;
        this.manager = graph.getEventManager();
    }
    
    @Override
    @Deprecated
    public void add(Triple[] arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (int i = 0 ; i < arg0.length ; i++) {
            g.add(arg0[i]);
        }
        add(g);
    }

    @Override
    @Deprecated
    public void add(List<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (Triple t : arg0) {
            g.add(t);
        }
        add(g);
    }

    @Override
    @Deprecated
    public void add(Iterator<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        while (arg0.hasNext()) {
            Triple t = arg0.next();
            g.add(t);
        }
        add(g);
    }

    @Override
    @Deprecated
    public void add(Graph arg0) {
        add(arg0, false);
    }

    @Override
    @Deprecated
    public void add(Graph g, boolean arg1) {
        Model[] model = separateStatementsWithBlankNodes(g);
        addModel(model[1] /* nonBlankNodeModel */);
        // replace following call with different method
        addModel(model[0] /*blankNodeModel*/);
    }
    
    /**
     * Returns a pair of models.  The first contains any statement containing at 
     * least one blank node.  The second contains all remaining statements.
     * @param g Graph
     */
    
    private Model[] separateStatementsWithBlankNodes(Graph g) {
        Model gm = ModelFactory.createModelForGraph(g);
        Model blankNodeModel = ModelFactory.createDefaultModel();
        Model nonBlankNodeModel = ModelFactory.createDefaultModel();
        StmtIterator sit = gm.listStatements();
        while (sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (!stmt.getSubject().isAnon() && !stmt.getObject().isAnon()) {
                nonBlankNodeModel.add(stmt);
            } else {
                blankNodeModel.add(stmt);
            }
        }
        Model[] result = new Model[2];
        result[0] = blankNodeModel;
        result[1] = nonBlankNodeModel;
        return result;
    }
    

	@Override
    @Deprecated
	public void delete(Triple[] arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (int i = 0 ; i < arg0.length ; i++) {
            g.add(arg0[i]);
        }
        delete(g);
	}

	@Override
    @Deprecated
	public void delete(List<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (Triple t : arg0) {
            g.add(t);
        }
        delete(g);
	}

	@Override
    @Deprecated
	public void delete(Iterator<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        while (arg0.hasNext()) {
            Triple t = arg0.next();
            g.add(t);
        }
        delete(g);
	}

    @Override 
    @Deprecated
    public void delete(Graph g, boolean withReifications) {
        delete(g);
    }
    
    @Override 
    @Deprecated
    public void delete(Graph g) {
        deleteModel(ModelFactory.createModelForGraph(g));
    }
    
    public void addModel(Model model) {
        ChangeSet changeSet = graph.getRDFService().manufactureChangeSet();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "N-TRIPLE");    
        changeSet.addAddition(new ByteArrayInputStream(
                out.toByteArray()), RDFService.ModelSerializationFormat.N3, 
                        graph.getGraphURI());
        try {
            graph.getRDFService().changeSetUpdate(changeSet);
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }
    
    public void deleteModel(Model model) {
        ChangeSet changeSet = graph.getRDFService().manufactureChangeSet();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "N-TRIPLE");    
        changeSet.addRemoval(new ByteArrayInputStream(
                out.toByteArray()), RDFService.ModelSerializationFormat.N3, 
                        graph.getGraphURI());
        try {
            graph.getRDFService().changeSetUpdate(changeSet);
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }    

    @Override 
    @Deprecated
    public void removeAll() {
        removeAll(graph, null, null, null);
        notifyRemoveAll(); 
    }

	protected void notifyRemoveAll() { 
        manager.notifyEvent(graph, GraphEvents.removeAll);
    }

    @Override
    @Deprecated
    public void remove(Node s, Node p, Node o) {
        removeAll(graph, s, p, o);
        manager.notifyEvent(graph, GraphEvents.remove(s, p, o));
    }

    private static void removeAll(Graph g, Node s, Node p, Node o)
    {    
		log.debug("removeAll: g=" + ToString.graphToString(g) + ", s=" + s
				+ ", p=" + p + ", o=" + o);
        if (!(g instanceof RDFServiceGraph)) {
            removeAllTripleByTriple(g, s, p, o);
            return;
        }
        
        RDFServiceGraph graph = (RDFServiceGraph) g;
        String graphURI = graph.getGraphURI();
        
        StringBuffer findPattern = new StringBuffer()
        .append(sparqlNode(s, "?s"))
        .append(" ")
        .append(sparqlNode(p, "?p"))
        .append(" ")
        .append(sparqlNode(o, "?o"));
        
        StringBuffer findQuery = new StringBuffer("CONSTRUCT { ")
        .append(findPattern)
        .append(" } WHERE { \n");
        if (graphURI != null) {
            findQuery.append("  GRAPH <" + graphURI + "> { ");
        }
        findQuery.append(findPattern);
        if (graphURI != null) {
            findQuery.append(" } ");
        }
        findQuery.append("\n}");
        
        String queryString = findQuery.toString();
        
        int chunkSize = 50000;
        boolean done = false;
        
        while (!done) { 
            String chunkQueryString = queryString + " LIMIT " + chunkSize;
            
            try {
                Model chunkToRemove = RDFServiceUtils.parseModel(
                        graph.getRDFService().sparqlConstructQuery(
                                chunkQueryString, RDFService.ModelSerializationFormat.N3), 
                                         RDFService.ModelSerializationFormat.N3);
                if (chunkToRemove.size() > 0) {
                    ChangeSet cs = graph.getRDFService().manufactureChangeSet();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    chunkToRemove.write(out, "N-TRIPLE");    
                    cs.addRemoval(new ByteArrayInputStream(out.toByteArray()), 
                            RDFService.ModelSerializationFormat.N3, graphURI);
                    graph.getRDFService().changeSetUpdate(cs);
                } else {
                    done = true;
                }
            } catch (RDFServiceException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    private static String sparqlNode(Node node, String varName) {
        if (node == null || node.isVariable() || node.isBlank()) {
            return varName;
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
    
    /*
     * 
     * see http://www.python.org/doc/2.5.2/ref/strings.html
     * or see jena's n3 grammar jena/src/com/hp/hpl/jena/n3/n3.g
     */ 
    private static void pyString(StringBuffer sbuff, String s)
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
    
    private static void removeAllTripleByTriple(Graph g, Node s, Node p, Node o)
    {        
        ExtendedIterator<Triple> it = g.find( s, p, o );
        try { 
            while (it.hasNext()) {
                Triple t = it.next();
                g.delete(t);
                it.remove(); 
            } 
        }
        finally {
            it.close();
        }
    }

}
