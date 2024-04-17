package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

public class BulkUpdatingModelTest extends AbstractBulkUpdatingModelTest {

    @Test
    public void testRemoveWithIterator() {
        BulkUpdatingModel wrappedModel = getWrappedModel();
        Model removeModel = VitroModelFactory.createModel();
        removeModel.add(getStatement());
        wrappedModel.remove(removeModel.listStatements());
    }

    @Test
    public void testRemoveStatementArray() {
        BulkUpdatingModel wrapModel = getWrappedModel();
        wrapModel.remove(new Statement[] { getStatement() });
    }

    @Test
    public void testRemoveStatementList() {
        BulkUpdatingModel wrapModel = getWrappedModel();
        wrapModel.remove(Arrays.asList(getStatement()));
    }

    @Test
    public void testAddWithIterator() {
        BulkUpdatingModel wrappedModel = getWrappedModel();
        Model model = VitroModelFactory.createModel();
        model.add(getStatement());
        wrappedModel.add(model.listStatements());
    }

    @Test
    public void testAddStatementArray() {
        BulkUpdatingModel wrapModel = getWrappedModel();
        wrapModel.add(new Statement[] { getStatement() });
    }

    @Test
    public void testAddStatementList() {
        BulkUpdatingModel wrapModel = getWrappedModel();
        wrapModel.add(Arrays.asList(getStatement()));
    }

    private BulkUpdatingModel getWrappedModel() {
        Model m = ModelFactory.createDefaultModel();
        RDFService rdfService = new RDFServiceModel(m);
        RDFServiceGraph g = new RDFServiceGraph(rdfService);
        BulkUpdatingModel wrappedModel = new BulkUpdatingModel(RDFServiceGraph.createRDFServiceModel(g));
        wrappedModel.updater = new WrappedUpdater(wrappedModel.updater);
        return wrappedModel;
    }

}
