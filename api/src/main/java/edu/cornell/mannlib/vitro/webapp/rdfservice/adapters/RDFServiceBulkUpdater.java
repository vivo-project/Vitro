/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.rdf.model.Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RDFServiceBulkUpdater extends AbstractBulkUpdater {
    RDFServiceGraph graph;
	private static final int chunkSize = 50000;
    private static final String REMOVE_CHUNK_QUERY = 
			"CONSTRUCT { ?s ?p ?o } " +
			"WHERE { ?s ?p ?o } " +
			"LIMIT " + chunkSize;

    public RDFServiceBulkUpdater(RDFServiceGraph graph) {
        this.graph = graph;
    }

    @Override
    protected void performAddModel(Model model) {
        ChangeSet changeSet = graph.getRDFService().manufactureChangeSet();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "N-TRIPLE");
        changeSet.addAddition(new ByteArrayInputStream(
                        out.toByteArray()), RDFService.ModelSerializationFormat.N3,
                graph.getGraphURI());
        try {
            graph.getRDFService().changeSetUpdate(changeSet);
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }

    @Override
    protected void performRemoveModel(Model model) {
        ChangeSet changeSet = graph.getRDFService().manufactureChangeSet();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "N-TRIPLE");
        changeSet.addRemoval(new ByteArrayInputStream(
                        out.toByteArray()), RDFService.ModelSerializationFormat.N3,
                graph.getGraphURI());
        try {
            graph.getRDFService().changeSetUpdate(changeSet);
        } catch (RDFServiceException rdfse) {
            throw new RuntimeException(rdfse);
        }
    }

    @Override
    protected void performRemoveAll() {
        String graphURI = graph.getGraphURI();
        String queryString;
        if (graphURI != null) {
        	queryString = getRemoveGraphChunkPattern(graphURI);
        } else {
        	queryString = REMOVE_CHUNK_QUERY;	
        }
        while (!graph.isEmpty()) {
            try {
                InputStream chunkToRemove = graph.getRDFService().sparqlConstructQuery(
                queryString, RDFService.ModelSerializationFormat.N3);
                ChangeSet cs = graph.getRDFService().manufactureChangeSet();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                cs.addRemoval(chunkToRemove, RDFService.ModelSerializationFormat.N3, graphURI);
                graph.getRDFService().changeSetUpdate(cs);
            } catch (RDFServiceException e) {
                throw new RuntimeException(e);
            }
        }
        graph.getEventManager().notifyEvent(graph, GraphEvents.removeAll);
    }
    
    private static String getRemoveGraphChunkPattern(String uri) {
    	return 
    			"CONSTRUCT { ?s ?p ?o } " +
    			"WHERE { " +
    				"GRAPH <" + uri + "> {" +
    					"?s ?p ?o " +
    				"} "+
    			"} "+ 
    			"LIMIT " + chunkSize;
    }
}
