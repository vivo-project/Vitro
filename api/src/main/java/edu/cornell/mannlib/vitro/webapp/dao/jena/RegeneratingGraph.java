/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class RegeneratingGraph implements Graph, Regenerable {

	private final static Log log = LogFactory.getLog(RegeneratingGraph.class);
	
	private GraphGenerator generator;
	private Graph g;
	
	public RegeneratingGraph(GraphGenerator graphGenerator) {
		this.generator = graphGenerator;
		regenerate();
	}
	
	public RegeneratingGraph(Graph initGraph, GraphGenerator graphGenerator) {
		this.g = initGraph;
		this.generator = graphGenerator;
	}
	
	@Override
	public void regenerate() {
		this.g = generator.generateGraph();
	}	
	
	/*
	 * a nonsense query that should never send back actual result data
	 */
	private void sendTestQuery() {
		this.g.contains(
				DCTerms.Agent.asNode(),RDF.type.asNode(),DCTerms.Agent.asNode());
	}

	@Override
	protected void finalize() {
		close();
	}
	
	@Override
	public void close() {
        try {
            g.close();
        } catch (Exception e) {
            regenerate();
            g.close();
        }
	}
	
	@Override
	public boolean contains(Triple arg0) {
		try {
			regenerateIfClosed();
            return g.contains(arg0);
        } catch (Exception e) {
            regenerate();
            return g.contains(arg0);
        }
	}
	
	@Override
	public boolean contains(Node arg0, Node arg1, Node arg2) {
		try {
			regenerateIfClosed();
			return g.contains(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return g.contains(arg0, arg1, arg2);
        }
	}
	
	@Override
	public void delete(Triple arg0) throws DeleteDeniedException {
		try {
			regenerateIfClosed();			
            g.delete(arg0);
        } catch (Exception e) {
            regenerate();
            g.delete(arg0);
        }
	}
	
	@Override
	public boolean dependsOn(Graph arg0) {
		try {
			regenerateIfClosed();			
            return g.dependsOn(arg0);
        } catch (Exception e) {
            regenerate();
            return g.dependsOn(arg0);
        }
	}

	@Override
	public ExtendedIterator<Triple> find(TripleMatch arg0) {
		try {
			regenerateIfClosed();
            return g.find(arg0);
        } catch (Exception e) {
            regenerate();
            return g.find(arg0);
        }
	}

	@Override
	public ExtendedIterator<Triple> find(Node arg0, Node arg1, Node arg2) {
		try {
			regenerateIfClosed();
            return g.find(arg0,arg1,arg2);
        } catch (Exception e) {
            regenerate();
            return g.find(arg0,arg1,arg2);
        }
	}
	
	@Override
	@Deprecated
	public BulkUpdateHandler getBulkUpdateHandler() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getBulkUpdateHandler();
        } catch (Exception e) {
            regenerate();
            return g.getBulkUpdateHandler();
        }
	}

	@Override
	public Capabilities getCapabilities() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getCapabilities();
        } catch (Exception e) {
            regenerate();
            return g.getCapabilities();
        }
	}

	
	@Override
	public GraphEventManager getEventManager() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getEventManager();
        } catch (Exception e) {
            regenerate();
            return g.getEventManager();
        }
	}

	
	@Override
	public PrefixMapping getPrefixMapping() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getPrefixMapping();
        } catch (Exception e) {
            regenerate();
            return g.getPrefixMapping();
        }
	}

	
	@Override
	public GraphStatisticsHandler getStatisticsHandler() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getStatisticsHandler();
        } catch (Exception e) {
            regenerate();
            return g.getStatisticsHandler();
        }
	}

	
	@Override
	public TransactionHandler getTransactionHandler() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getTransactionHandler();
        } catch (Exception e) {
            regenerate();
            return g.getTransactionHandler();
        }
	}

	
	@Override
	public boolean isClosed() {
		try {
			regenerateIfClosed();
            return g.isClosed();
        } catch (Exception e) {
            regenerate();
            return g.isClosed();
        }
	}

	
	@Override
	public boolean isEmpty() {
		try {
			regenerateIfClosed();
            return g.isEmpty();
        } catch (Exception e) {
            regenerate();
            return g.isEmpty();
        }
	}

	
	@Override
	public boolean isIsomorphicWith(Graph arg0) {
		try {
			regenerateIfClosed();
            return g.isIsomorphicWith(arg0);
        } catch (Exception e) {
            regenerate();
            return g.isIsomorphicWith(arg0);
        }
	}
	
	@Override
	public int size() {
		try {
			regenerateIfClosed();
            return g.size();
        } catch (Exception e) {
            regenerate();
            return g.size();
        }
	}

	
	@Override
	public void add(Triple arg0) throws AddDeniedException {
		try {
			regenerateIfClosed();
            g.add(arg0);
        } catch (Exception e) {
            regenerate();
            g.add(arg0);
        }
	}
	
	@Override
	public void clear() {
		try {
			regenerateIfClosed();
            g.clear();
        } catch (Exception e) {
            regenerate();
            g.clear();
        }
	}

	@Override
	public void remove(Node arg0, Node arg1, Node arg2) {
		try {
			regenerateIfClosed();
            g.remove(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            g.remove(arg0, arg1, arg2);
        }
	}

	private void regenerateIfClosed() {
		if (generator.isGraphClosed()) {
			regenerate();
		}
	}

	@Override
	public String toString() {
		return "RegeneratingGraph[" + ToString.hashHex(this) + ", "
				+ ToString.graphToString(g) + "]";
	}
}
