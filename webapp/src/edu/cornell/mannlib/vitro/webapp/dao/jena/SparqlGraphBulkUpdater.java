package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SparqlGraphBulkUpdater extends SimpleBulkUpdateHandler {

    public SparqlGraphBulkUpdater(GraphWithPerform graph) {
        super(graph);
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
        ExtendedIterator<Triple> it = GraphUtil.findAll(g);
        try {
            while (it.hasNext()) {
                Triple t = it.next();
                g.delete(t);
                it.remove();
            } 
        } finally {
            it.close();
        }
    }

}
