/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class RDFServiceDataset implements Dataset {

    private RDFServiceDatasetGraph g;
    
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
        return g.containsGraph(Node.createURI(arg0));
    }

    @Override
    public Model getDefaultModel() {
        return RDFServiceGraph.createRDFServiceModel(g.getDefaultGraph());
    }

    @Override
    public Lock getLock() {
        return g.getLock();
    }

    @Override
    public Model getNamedModel(String arg0) {
        return RDFServiceGraph.createRDFServiceModel(g.getGraph(Node.createURI(arg0)));
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

}
