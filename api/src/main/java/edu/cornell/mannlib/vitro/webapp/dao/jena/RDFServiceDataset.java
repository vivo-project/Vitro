/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.LabelExistsException;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

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
		return g.supportsTransactions();
	}

	@Override
	public boolean supportsTransactionAbort() {
		return g.supportsTransactionAbort();
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
	}

	@Override
	public void commit() {
		g.commit();
	}

	@Override
	public void abort() {
		g.abort();
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
