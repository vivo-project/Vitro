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
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.RDF;

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
	
	public void regenerate() {
		this.g = generator.generateGraph();
	}	
	
	/*
	 * a nonsense query that should never send back actual result data
	 */
	private void sendTestQuery() {
		this.g.contains(
				DAML_OIL.Thing.asNode(),RDF.type.asNode(),DAML_OIL.Thing.asNode());
	}

	protected void finalize() {
		close();
	}
	
	public void close() {
        try {
            g.close();
            if (generator instanceof RDBGraphGenerator) {
        		((RDBGraphGenerator) generator).getConnection().close();
        	}
        } catch (Exception e) {
            regenerate();
            g.close();
        }
	}
	
	public boolean contains(Triple arg0) {
		try {
			regenerateIfClosed();
            return g.contains(arg0);
        } catch (Exception e) {
            regenerate();
            return g.contains(arg0);
        }
	}
	
	public boolean contains(Node arg0, Node arg1, Node arg2) {
		try {
			regenerateIfClosed();
			return g.contains(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return g.contains(arg0, arg1, arg2);
        }
	}
	
	public void delete(Triple arg0) throws DeleteDeniedException {
		try {
			regenerateIfClosed();			
            g.delete(arg0);
        } catch (Exception e) {
            regenerate();
            g.delete(arg0);
        }
	}
	
	public boolean dependsOn(Graph arg0) {
		try {
			regenerateIfClosed();			
            return g.dependsOn(arg0);
        } catch (Exception e) {
            regenerate();
            return g.dependsOn(arg0);
        }
	}

	public ExtendedIterator find(TripleMatch arg0) {
		try {
			regenerateIfClosed();
            return g.find(arg0);
        } catch (Exception e) {
            regenerate();
            return g.find(arg0);
        }
	}

	public ExtendedIterator find(Node arg0, Node arg1, Node arg2) {
		try {
			regenerateIfClosed();
            return g.find(arg0,arg1,arg2);
        } catch (Exception e) {
            regenerate();
            return g.find(arg0,arg1,arg2);
        }
	}
	
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

	
	public Reifier getReifier() {
		try {
			regenerateIfClosed();
			sendTestQuery();
            return g.getReifier();
        } catch (Exception e) {
            regenerate();
            return g.getReifier();
        }
	}

	
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

	
	public boolean isClosed() {
		try {
			regenerateIfClosed();
            return g.isClosed();
        } catch (Exception e) {
            regenerate();
            return g.isClosed();
        }
	}

	
	public boolean isEmpty() {
		try {
			regenerateIfClosed();
            return g.isEmpty();
        } catch (Exception e) {
            regenerate();
            return g.isEmpty();
        }
	}

	
	public boolean isIsomorphicWith(Graph arg0) {
		try {
			regenerateIfClosed();
            return g.isIsomorphicWith(arg0);
        } catch (Exception e) {
            regenerate();
            return g.isIsomorphicWith(arg0);
        }
	}

	
	public QueryHandler queryHandler() {
		try {
			regenerateIfClosed();
			sendTestQuery();
			return g.queryHandler();
		} catch (Exception e) {
			regenerate();
			return g.queryHandler();
		}
	}

	
	public int size() {
		try {
			regenerateIfClosed();
            return g.size();
        } catch (Exception e) {
            regenerate();
            return g.size();
        }
	}

	
	public void add(Triple arg0) throws AddDeniedException {
		try {
			regenerateIfClosed();
            g.add(arg0);
        } catch (Exception e) {
            regenerate();
            g.add(arg0);
        }
	}
	
	private void regenerateIfClosed() {
		if (generator.isGraphClosed()) {
			regenerate();
		}
	}

}
