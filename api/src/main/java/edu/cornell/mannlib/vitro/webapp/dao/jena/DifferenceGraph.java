/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Set;

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
import org.apache.jena.util.iterator.WrappedIterator;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class DifferenceGraph implements Graph {

    private Graph g;
    private Graph subtract;
    
    public DifferenceGraph(Graph g, Graph subtract) {    
        this.g = g;
        this.subtract = subtract;
    }
    
    @Override
    public void close() {
        // not clear what the best behavior here is
    }

    @Override
    public boolean contains(Triple arg0) {
        return g.contains(arg0) && !subtract.contains(arg0);
    }

    @Override
    public boolean contains(Node arg0, Node arg1, Node arg2) {
        return g.contains(arg0, arg1, arg2) && !subtract.contains(arg0, arg1, arg2);
    }

    @Override
    public void delete(Triple arg0) throws DeleteDeniedException {
        g.delete(arg0);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        return find(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
	public void remove(Node arg0, Node arg1, Node arg2) {
		g.remove(arg0, arg1, arg2);
	}

    @Override
    public boolean dependsOn(Graph arg0) {
        return g.dependsOn(arg0);
    }

    @Override
    public ExtendedIterator<Triple> find(Node arg0, Node arg1, Node arg2) {
        Set<Triple> tripSet = g.find(arg0, arg1, arg2).toSet();
        tripSet.removeAll(subtract.find(arg0, arg1, arg2).toSet());
        return WrappedIterator.create(tripSet.iterator());
    }

    @Override
    public Capabilities getCapabilities() {
        return g.getCapabilities();
    }

    @Override
    public GraphEventManager getEventManager() {
        return g.getEventManager();
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return g.getPrefixMapping();
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler() {
        return g.getStatisticsHandler();
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return g.getTransactionHandler();
    }

    @Override
    public boolean isClosed() {
        return g.isClosed();
    }

    @Override
    public boolean isEmpty() {
        return g.isEmpty();
    }

    @Override
    public boolean isIsomorphicWith(Graph arg0) {
        return g.isIsomorphicWith(arg0);
    }

    @Override
    public int size() {
        return g.size() - subtract.size();
    }

    @Override
    public void add(Triple arg0) throws AddDeniedException {
        g.add(arg0);
    }

	@Override
	public void clear() {
		g.clear();
	}

	@Override
	public String toString() {
		return "DifferenceGraph[" + ToString.hashHex(this) + ", g="
				+ ToString.graphToString(g) + ", subtract="
				+ ToString.graphToString(subtract) + "]";
	}

}
