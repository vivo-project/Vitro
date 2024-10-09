package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.junit.Test;

public class JenaModelUtilsTest {

    @Test
    public void removeUsingSparqlConstructTest() {
        Model removeFrom = getModel();
        Model toRemove = getModel();
        JenaModelUtils.removeUsingSparqlConstruct(toRemove, removeFrom);
        assertTrue(removeFrom.isEmpty());
    }

    private Model getModel() {
        Model m = ModelFactory.createDefaultModel();
        for (int i = 0; i < 1000; i++) {
            m.add(getStatement());
        }
        return m;
    }

    private StatementImpl getStatement() {
        ResourceImpl subject = new ResourceImpl("test:uri");
        PropertyImpl property = new PropertyImpl("test:property");
        Resource object = ResourceFactory.createResource();
        StatementImpl statement = new StatementImpl(subject, property, object);
        return statement;
    }
}
