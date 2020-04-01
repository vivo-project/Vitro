package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.apache.jena.rdf.model.ResourceFactory.createStringLiteral;

/**
 * @author awoods
 * @since 2020-04-01
 */
public class AdditionsAndRetractionsTest {

    @Test
    public void testToString() {
        Model additions = ModelFactory.createDefaultModel();
        additions.add(createStatement(
                createResource("test:add"),
                createProperty("test:prop"),
                createStringLiteral("new")));

        Model retractions = ModelFactory.createDefaultModel();
        retractions.add(createStatement(
                createResource("test:retract"),
                createProperty("test:prop"),
                createStringLiteral("old")));
        AdditionsAndRetractions aar = new AdditionsAndRetractions(additions, retractions);

        final String s = aar.toString();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("test:add"));
        Assert.assertTrue(s.contains("test:retract"));
    }
}