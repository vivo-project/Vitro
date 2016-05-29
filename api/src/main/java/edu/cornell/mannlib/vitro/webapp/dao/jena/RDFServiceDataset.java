/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.util.Context;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class RDFServiceDataset implements Dataset {

    private RDFServiceDatasetGraph g;
    private ReadWrite transactionMode;
    
    public RDFServiceDataset(RDFServiceDatasetGraph g) {
        this.g = g;
    }
    
    public RDFServiceDataset(RDFService rdfService) {
        this.g = new RDFServiceDatasetGraph(rdfService);
    }
    
    @Override
    public DatasetGraph asDatasetGraph() {
        return g;
    }

    @Override
    public void close() {
        g.close();
    }

    @Override
    public boolean containsNamedModel(String arg0) {
        return g.containsGraph(NodeFactory.createURI(arg0));
    }

    @Override
    public Model getDefaultModel() {
        return RDFServiceGraph.createRDFServiceModel(g.getDefaultGraph());
    }

    @Override
    public Lock getLock() {
        return g.getLock();
    }

    private final static Log log = LogFactory.getLog(RDFServiceDataset.class);
    
    @Override
    public Model getNamedModel(String arg0) {
        Model model = RDFServiceGraph.createRDFServiceModel(
                g.getGraph(NodeFactory.createURI(arg0)));
        return model;
    }

    @Override
    public Iterator<String> listNames() {
        ArrayList<String> nameList = new ArrayList<String>();
        Iterator<Node> nodeIt = g.listGraphNodes();
        while (nodeIt.hasNext()) {
            Node n = nodeIt.next();
            nameList.add(n.getURI());
        }
        return nameList.iterator();
    }

	@Override
	public void addNamedModel(String uri, Model model)
			throws LabelExistsException {
		Iterator<Node> graphNodes = g.listGraphNodes();
		while (graphNodes.hasNext()) {
			Node graphNode = graphNodes.next();
			if (graphNode.hasURI(uri)) {
				throw new LabelExistsException("Can't add named model '"+ uri
						+ "': model already exists");
			}
		}
		g.addGraph(NodeFactory.createURI(uri), model.getGraph());
	}

	@Override
	public Context getContext() {
		return g.getContext();
	}

	@Override
	public void removeNamedModel(String uri) {
		g.removeGraph(NodeFactory.createURI(uri));
	}

	@Override
	public void replaceNamedModel(String uri, Model model) {
		removeNamedModel(uri);
		addNamedModel(uri, model);
	}

	@Override
	public void setDefaultModel(Model model) {
		g.setDefaultGraph(model.getGraph());
	}

	@Override
	public boolean supportsTransactions() {
		if (g.getDefaultGraph().getTransactionHandler() == null) {
		    return false;
		} else {
		    return g.getDefaultGraph().getTransactionHandler().transactionsSupported();
		}
	}

	@Override
	public boolean isInTransaction() {
		return (transactionMode != null);
	}
	
    private boolean supportsTransactions(Graph graph) {
        return (graph.getTransactionHandler() != null 
                && graph.getTransactionHandler().transactionsSupported());
    }

	@Override
	public void begin(ReadWrite arg0) {
	    this.transactionMode = arg0;
	    g.begin(arg0);
	    for(String graphURI : g.getGraphCache().keySet()) {
            Graph graph = g.getGraphCache().get(graphURI);
            if (supportsTransactions(graph)) {
                graph.getTransactionHandler().begin();
            }
        }
	}

	@Override
	public void commit() {
	    for(String graphURI : g.getGraphCache().keySet()) {
            Graph graph = g.getGraphCache().get(graphURI);
            if(supportsTransactions(graph)) {
                graph.getTransactionHandler().commit();
            }
        }
	}

	@Override
	public void abort() {
	    for(String graphURI : g.getGraphCache().keySet()) {
            Graph graph = g.getGraphCache().get(graphURI);
            if(supportsTransactions(graph)) {
                graph.getTransactionHandler().abort();
            }
        }
	}

	@Override
	public void end() {
	    // the Graph tranaction handlers don't seem to support .end()
	    this.transactionMode = null;
	    g.end();
	}

	@Override
	public String toString() {
		return "RDFServiceDataset[" + ToString.hashHex(this) + ", " + g + "]";
	}

}
