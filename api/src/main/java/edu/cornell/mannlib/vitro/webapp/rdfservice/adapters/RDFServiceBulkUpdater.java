/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class RDFServiceBulkUpdater extends AbstractBulkUpdater {
    RDFServiceGraph graph;

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

        String findPattern = "?s ?p ?o";

        StringBuffer findQuery = new StringBuffer("CONSTRUCT { ")
                .append(findPattern)
                .append(" } WHERE { \n");
        if (graphURI != null) {
            findQuery.append("  GRAPH <" + graphURI + "> { ");
        }
        findQuery.append(findPattern);
        if (graphURI != null) {
            findQuery.append(" } ");
        }
        findQuery.append("\n}");

        String queryString = findQuery.toString();

        int chunkSize = 50000;
        boolean done = false;

        while (!done) {
            String chunkQueryString = queryString + " LIMIT " + chunkSize;

            try {
                Model chunkToRemove = RDFServiceUtils.parseModel(
                        graph.getRDFService().sparqlConstructQuery(
                                chunkQueryString, RDFService.ModelSerializationFormat.N3),
                        RDFService.ModelSerializationFormat.N3);
                if (chunkToRemove.size() > 0) {
                    ChangeSet cs = graph.getRDFService().manufactureChangeSet();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    chunkToRemove.write(out, "N-TRIPLE");
                    cs.addRemoval(new ByteArrayInputStream(out.toByteArray()),
                            RDFService.ModelSerializationFormat.N3, graphURI);
                    graph.getRDFService().changeSetUpdate(cs);
                } else {
                    done = true;
                }
            } catch (RDFServiceException e) {
                throw new RuntimeException(e);
            }
        }

        graph.getEventManager().notifyEvent(graph, GraphEvents.removeAll);
    }
}
