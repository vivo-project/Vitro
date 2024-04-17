package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;

public class AbstractBulkUpdatingModelTest {

    public AbstractBulkUpdatingModelTest() {
        super();
    }

    protected StatementImpl getStatement() {
        ResourceImpl subject = new ResourceImpl("test:uri1");
        PropertyImpl property = new PropertyImpl("http://www.w3.org/2000/01/rdf-schema#label");
        Literal object = ResourceFactory.createLangLiteral("Persons", "en-US");
        StatementImpl statement = new StatementImpl(subject, property, object);
        return statement;
    }

    static class WrappedUpdater extends AbstractBulkUpdater {

        private AbstractBulkUpdater wrappedUpdater;

        public void add(Graph g) {
            performAddModel(ModelFactory.createModelForGraph(g));
        }

        public WrappedUpdater(AbstractBulkUpdater updater) {
            this.wrappedUpdater = updater;
        }

        @Override
        protected void performAddModel(Model model) {
            wrappedUpdater.performAddModel(model);
        }

        @Override
        protected void performRemoveModel(Model model) {
            wrappedUpdater.performRemoveModel(model);
        }

        @Override
        protected void performRemoveAll() {
            wrappedUpdater.performRemoveAll();
        }
    }

}

