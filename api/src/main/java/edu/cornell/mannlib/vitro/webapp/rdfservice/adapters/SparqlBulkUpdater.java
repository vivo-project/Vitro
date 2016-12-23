/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.StringWriter;

public class SparqlBulkUpdater extends AbstractBulkUpdater {
    private SparqlGraph graph;

    public SparqlBulkUpdater(SparqlGraph graph) {
        this.graph = graph;
    }

    @Override
    protected void performAddModel(Model model) {
        verbModel(model, "INSERT");
    }

    @Override
    protected void performRemoveModel(Model model) {
        verbModel(model, "DELETE");
    }

    private void verbModel(Model model, String verb) {
        Model m = ModelFactory.createDefaultModel();
        int testLimit = 1000;
        StmtIterator stmtIt = model.listStatements();
        int count = 0;
        try {
            while (stmtIt.hasNext()) {
                count++;
                m.add(stmtIt.nextStatement());
                if (count % testLimit == 0 || !stmtIt.hasNext()) {
                    StringWriter sw = new StringWriter();
                    m.write(sw, "N-TRIPLE");
                    StringBuffer updateStringBuff = new StringBuffer();
                    String graphURI = graph.getGraphURI();
                    updateStringBuff.append(verb + " DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" ));
                    updateStringBuff.append(sw);
                    updateStringBuff.append(((graphURI != null) ? " } " : "") + " }");

                    String updateString = updateStringBuff.toString();

                    //log.info(updateString);

                    graph.executeUpdate(updateString);

                    m.removeAll();
                }
            }
        } finally {
            stmtIt.close();
        }
    }

    @Override
    protected void performRemoveAll() {
        ExtendedIterator<Triple> it = GraphUtil.findAll(graph);
        try {
            while (it.hasNext()) {
                Triple t = it.next();
                graph.delete(t);
                it.remove();
            }
        } finally {
            it.close();
        }

        // get rid of remaining blank nodes using a SPARQL DELETE
        graph.removeAll();

        graph.getEventManager().notifyEvent(graph, GraphEvents.removeAll);
    }
}
