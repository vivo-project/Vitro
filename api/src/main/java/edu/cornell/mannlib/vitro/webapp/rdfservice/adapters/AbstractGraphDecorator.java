package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * The base class for a delegating graph decorator.
 *
 * As implemented, all methods simply delegate to the inner graph. Subclasses
 * should override selected methods to provide functionality.
 */
public abstract class AbstractGraphDecorator implements 
        Graph {
    
    private final Graph inner;

    protected AbstractGraphDecorator(Graph g) {
        if (g == null) {
            throw new IllegalArgumentException("g may not be null.");
        }
        this.inner = g;
    }

    @Override
    public String toString() {
        return ToString.simpleName(this) + "[" + ToString.hashHex(this)
                + ", inner=" + ToString.graphToString(inner) + "]";
    }

    @Override
    public void add(Triple arg0) throws AddDeniedException {
        inner.add(arg0);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public boolean contains(Triple arg0) {
        return inner.contains(arg0);
    }

    @Override
    public boolean contains(Node arg0, Node arg1, Node arg2) {
        return inner.contains(arg0, arg1, arg2);
    }

    @Override
    public void delete(Triple arg0) throws DeleteDeniedException {
        inner.delete(arg0);
    }

    @Override
    public boolean dependsOn(Graph arg0) {
        return inner.dependsOn(arg0);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple arg0) {
        return inner.find(arg0);
    }

    @Override
    public ExtendedIterator<Triple> find(Node arg0, Node arg1, Node arg2) {
        return inner.find(arg0, arg1, arg2);
    }

    @Override
    public Capabilities getCapabilities() {
        return inner.getCapabilities();
    }

    @Override
    public GraphEventManager getEventManager() {
        return inner.getEventManager();
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return inner.getPrefixMapping();
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler() {
        return inner.getStatisticsHandler();
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return inner.getTransactionHandler();
    }

    @Override
    public boolean isClosed() {
        return inner.isClosed();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean isIsomorphicWith(Graph arg0) {
        return inner.isIsomorphicWith(arg0);
    }

    @Override
    public void remove(Node arg0, Node arg1, Node arg2) {
        inner.remove(arg0, arg1, arg2);
    }

    @Override
    public int size() {
        return inner.size();
    }
    
}