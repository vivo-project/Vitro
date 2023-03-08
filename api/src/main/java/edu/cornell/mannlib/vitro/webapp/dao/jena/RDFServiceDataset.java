/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.MapFilter;
import org.apache.jena.util.iterator.MapFilterIterator;
import org.apache.jena.util.iterator.WrappedIterator;
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
    public Model getUnionModel() {
        return RDFServiceGraph.createRDFServiceModel(g.getUnionGraph());
    }

    @Override
    public Lock getLock() {
        return g.getLock();
    }

    @Override
    public Model getNamedModel(String arg0) {
        Model model = RDFServiceGraph.createRDFServiceModel(
            g.getGraph(NodeFactory.createURI(arg0)));
        return model;
    }

    @Override
    public Model getNamedModel(Resource uri) {
        Model model = RDFServiceGraph.createRDFServiceModel(g.getGraph(uri.asNode()));
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
    public Dataset addNamedModel(Resource resource, Model model) {
        graphNameNullCheck(resource);

        g.addGraph(resource.asNode(), model.getGraph());
        return this;

    }

    @Override
    public Dataset addNamedModel(String uri, Model model) {
        Iterator<Node> graphNodes = g.listGraphNodes();
        while (graphNodes.hasNext()) {
            Node graphNode = graphNodes.next();

            graphNameAlreadyExistsCheck(graphNode, uri);
        }
        g.addGraph(NodeFactory.createURI(uri), model.getGraph());
        return this;
    }

    @Override
    public Context getContext() {
        return g.getContext();
    }

    @Override
    public Dataset removeNamedModel(String uri) {
        g.removeGraph(NodeFactory.createURI(uri));
        return this;
    }

    @Override
    public Dataset replaceNamedModel(String uri, Model model) {
        graphNameNullCheck(uri);

        removeNamedModel(uri);
        addNamedModel(uri, model);
        return this;
    }

    @Override
    public Dataset removeNamedModel(Resource resource) {
        graphNameNullCheck(resource);

        g.removeGraph(resource.asNode()) ;
        return this;
    }

    @Override
    public Dataset replaceNamedModel(Resource resource, Model model) {
        graphNameNullCheck(resource) ;

        g.removeGraph(resource.asNode()) ;
        g.addGraph(resource.asNode(), model.getGraph() ) ;

        return this;
    }

    @Override
    public Dataset setDefaultModel(Model model) {
        g.setDefaultGraph(model.getGraph());
        return this;
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
        // the Graph transaction handlers don't seem to support .end()
        this.transactionMode = null;
        g.end();
    }

    @Override
    public String toString() {
        return "RDFServiceDataset[" + ToString.hashHex(this) + ", " + g + "]";
    }

    @Override
    public boolean isEmpty() {
        return g.isEmpty();
    }

    @Override
    public TxnType transactionType() {
        return g.transactionType();
    }

    @Override
    public boolean promote(Promote promote) {
        return g.promote(promote);
    }

    @Override
    public ReadWrite transactionMode() {
        return g.transactionMode();
    }

    @Override
    public void begin(TxnType txnType) {
        g.begin(txnType);
    }

    @Override
    public boolean containsNamedModel(Resource resource) {
        graphNameNullCheck(resource);

        return g.containsGraph(resource.asNode()) ;

    }

    /***
     * Get whether or not transactions are supported for a graph.
     *
     * @param graph The graph to check if it supports transactions.
     *
     * @return True if supported and false otherwise.
     */
    private boolean supportsTransactions(Graph graph) {
        return (graph.getTransactionHandler() != null
                   && graph.getTransactionHandler().transactionsSupported());
    }

    @Override
    public Iterator<Resource> listModelNames() {
        MapFilter<Node, Resource> mapper = x -> new ResourceImpl(x, null);
        ExtendedIterator<Node> eIter = WrappedIterator.create(g.listGraphNodes()) ;
        return new MapFilterIterator<>(mapper, eIter) ;

    }

    /**
     * Check to see if the graph is defined.
     *
     * @param name The graph name, such as a resource or a URI.
     *
     * @throws ARQException If name is null.
     */
    private static void graphNameNullCheck(Object name) {
        if (name == null) {
            throw new ARQException("Graph name is null");
        }
    }

    /**
     * Check to see if the graph URI already exists.
     *
     * @param graphNode The graph node to check.
     * @param uri The URI to check if already exists.
     *
     * @throws ARQException If URI already exists.
     */
    private static void graphNameAlreadyExistsCheck(Node graphNode, Object uri) {
        if (graphNode.hasURI((String) uri)) {
            throw new ARQException("Can't add named model '" + uri + "': model already exists");
        }
    }
}
