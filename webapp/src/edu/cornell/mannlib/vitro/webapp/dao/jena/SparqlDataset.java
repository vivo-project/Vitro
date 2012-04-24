package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Iterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class SparqlDataset implements Dataset {

    private String endpointURI;
    
    public SparqlDataset(String endpointURI) {
        this.endpointURI = endpointURI;
    }
    
    @Override
    public DatasetGraph asDatasetGraph() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsNamedModel(String arg0) {
        return true;
    }

    @Override
    public Model getDefaultModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Lock getLock() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Model getNamedModel(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<String> listNames() {
        // TODO Auto-generated method stub
        return null;
    }

}
