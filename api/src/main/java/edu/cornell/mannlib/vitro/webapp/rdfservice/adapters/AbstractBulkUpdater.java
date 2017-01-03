/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public abstract class AbstractBulkUpdater {
    public void add(Graph g) {
        Model[] model = separateStatementsWithBlankNodes(g);
        performAddModel(model[1] /* nonBlankNodeModel */);
        // replace following call with different method
        performAddModel(model[0] /*blankNodeModel*/);
    }

    public void remove(Graph g) {
        performRemoveModel(ModelFactory.createModelForGraph(g));
    }

    public void removeAll() {
        performRemoveAll();
    }

    protected abstract void performAddModel(Model model);

    protected abstract void performRemoveModel(Model model);

    protected abstract void performRemoveAll();

    private Model[] separateStatementsWithBlankNodes(Graph g) {
        Model gm = ModelFactory.createModelForGraph(g);
        Model blankNodeModel = ModelFactory.createDefaultModel();
        Model nonBlankNodeModel = ModelFactory.createDefaultModel();
        StmtIterator sit = gm.listStatements();
        while (sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (!stmt.getSubject().isAnon() && !stmt.getObject().isAnon()) {
                nonBlankNodeModel.add(stmt);
            } else {
                blankNodeModel.add(stmt);
            }
        }
        Model[] result = new Model[2];
        result[0] = blankNodeModel;
        result[1] = nonBlankNodeModel;
        return result;
    }
}
