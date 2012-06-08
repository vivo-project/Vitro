package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class RDFServiceGraphBulkUpdater extends SimpleBulkUpdateHandler {

    private static final Log log = LogFactory.getLog(SparqlGraphBulkUpdater.class);
    private RDFServiceGraph graph;
    
    public RDFServiceGraphBulkUpdater(RDFServiceGraph graph) {
        super(graph);
        this.graph = graph;
    }
    
    @Override
    public void add(Triple[] arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (int i = 0 ; i < arg0.length ; i++) {
            g.add(arg0[i]);
        }
        add(g);
    }

    @Override
    public void add(List<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        for (Triple t : arg0) {
            g.add(t);
        }
        add(g);
    }

    @Override
    public void add(Iterator<Triple> arg0) {
        Graph g = GraphFactory.createPlainGraph();
        while (arg0.hasNext()) {
            Triple t = arg0.next();
            g.add(t);
        }
        add(g);
    }

    @Override
    public void add(Graph arg0) {
        add(arg0, false);
    }

    @Override
    public void add(Graph g, boolean arg1) {
        Model[] model = separateStatementsWithBlankNodes(g);
        addModel(model[1] /* nonBlankNodeModel */);
        // replace following call with different method
        addModel(model[0] /*blankNodeModel*/);
    }
    
    /**
     * Returns a pair of models.  The first contains any statement containing at 
     * least one blank node.  The second contains all remaining statements.
     * @param g
     * @return
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
    public void delete(Graph g, boolean withReifications) {
        delete(g);
    }
    
    @Override 
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
    public void removeAll() {
        removeAll(graph);
        notifyRemoveAll(); 
    }

    protected void notifyRemoveAll() { 
        manager.notifyEvent(graph, GraphEvents.removeAll);
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        removeAll(graph, s, p, o);
        manager.notifyEvent(graph, GraphEvents.remove(s, p, o));
    }

    public static void removeAll(Graph g, Node s, Node p, Node o)
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

    public static void removeAll( Graph g )
    {
        g.getBulkUpdateHandler().delete(g);
    }

}
