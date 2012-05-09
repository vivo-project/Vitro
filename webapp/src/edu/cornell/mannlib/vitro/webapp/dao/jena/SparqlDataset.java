package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class SparqlDataset implements Dataset {

    private SparqlDatasetGraph g;
    
    public SparqlDataset(SparqlDatasetGraph g) {
        this.g = g;
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
        return ModelFactory.createModelForGraph(g.getDefaultGraph());
    }

    @Override
    public Lock getLock() {
        return g.getLock();
    }

    @Override
    public Model getNamedModel(String arg0) {
        return ModelFactory.createModelForGraph(g.getGraph(Node.createURI(arg0)));
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
