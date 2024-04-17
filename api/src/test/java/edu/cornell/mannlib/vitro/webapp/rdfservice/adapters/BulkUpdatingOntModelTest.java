package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import static org.apache.jena.ontology.OntModelSpec.OWL_MEM;

import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

public class BulkUpdatingOntModelTest extends AbstractBulkUpdatingModelTest {

    @Test
    public void testRemoveWithIterator() {
        OntModel wrappedModel = getWrappedModel();
        Model removeModel = VitroModelFactory.createModel();
        removeModel.add(getStatement());
        wrappedModel.remove(removeModel.listStatements());
    }

    @Test
    public void testRemoveStatementArray() {
        OntModel wrapModel = getWrappedModel();
        wrapModel.remove(new Statement[] { getStatement() });
    }

    @Test
    public void testRemoveStatementList() {
        OntModel wrapModel = getWrappedModel();
        wrapModel.remove(Arrays.asList(getStatement()));
    }

    @Test
    public void testAddWithIterator() {
        OntModel wrappedModel = getWrappedModel();
        Model model = VitroModelFactory.createModel();
        model.add(getStatement());
        wrappedModel.add(model.listStatements());
    }

    @Test
    public void testAddStatementArray() {
        OntModel wrapModel = getWrappedModel();
        wrapModel.add(new Statement[] { getStatement() });
    }

    @Test
    public void testAddStatementList() {
        OntModel wrapModel = getWrappedModel();
        wrapModel.add(Arrays.asList(getStatement()));
    }

    private OntModel getWrappedModel() {
        Model m = ModelFactory.createOntologyModel();
        RDFService rdfService = new RDFServiceModel(m);
        RDFServiceGraph g = new RDFServiceGraph(rdfService);

        Model bareModel = new BulkModelCom(g);
        OntModel ontModel = new BulkOntModelImpl(OWL_MEM, bareModel);
        BulkUpdatingOntModel wrappedModel = new BulkUpdatingOntModel(ontModel);
        wrappedModel.updater = new WrappedUpdater(wrappedModel.updater);
        return wrappedModel;
    }

}
