/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

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

public class BlankNodeFilteringGraph implements Graph {
	
    private Graph graph;
    
	public BlankNodeFilteringGraph(Graph graph) {
        this.graph = graph;
    }

	@Override
	public void add(Triple t) throws AddDeniedException {
		graph.add(t);
	}

	@Override
	public void close() {
		graph.close();
	}

	@Override
	public boolean contains(Node arg0, Node arg1, Node arg2) {
		return graph.contains(arg0, arg1, arg2);
	}

	@Override
	public boolean contains(Triple arg0) {
		return graph.contains(arg0);
	}

	@Override
	public void delete(Triple t) throws DeleteDeniedException {
		graph.delete(t);
	}

	@Override
	public ExtendedIterator<Triple> find(Triple triple) {
		return find(triple.getSubject(), triple.getPredicate(), triple.getObject());
	}

	@Override
	public boolean dependsOn(Graph arg0) {
		return graph.dependsOn(arg0);
	}

	@Override
	public ExtendedIterator<Triple> find(Node subject, Node predicate, Node object) {
		
        List<Triple> nbTripList = new ArrayList<Triple>();
		ExtendedIterator<Triple> triples = graph.find(subject, predicate, object);
		
		while (triples.hasNext()) {
			Triple triple = triples.next();
			
			if (!triple.getSubject().isBlank() && !triple.getObject().isBlank()) {
				nbTripList.add(triple);
			}
		}
		
        return WrappedIterator.create(nbTripList.iterator());
	}

	@Override
	public Capabilities getCapabilities() {
		return graph.getCapabilities();
	}

	@Override
	public GraphEventManager getEventManager() {
		return graph.getEventManager();
	}

	@Override
	public PrefixMapping getPrefixMapping() {
		return graph.getPrefixMapping();
	}

	@Override
	public GraphStatisticsHandler getStatisticsHandler() {
		return graph.getStatisticsHandler();
	}

	@Override
	public TransactionHandler getTransactionHandler() {
		return graph.getTransactionHandler();
	}

	@Override
	public boolean isClosed() {
		return graph.isClosed();
	}

	@Override
	public boolean isEmpty() {
		return graph.isEmpty();
	}

	@Override
	public boolean isIsomorphicWith(Graph arg0) {
		return graph.isIsomorphicWith(arg0);
	}

	@Override
	public int size() {
		return graph.size();
	}

	@Override
	public void clear() {
		graph.clear();
	}

	@Override
	public void remove(Node arg0, Node arg1, Node arg2) {
		graph.remove(arg0, arg1, arg2);
	}

	@Override
	public String toString() {
		return "BlankNodeFilteringGraph[" + ToString.hashHex(this) + ", inner="
				+ ToString.graphToString(graph) + "]";
	}
}
