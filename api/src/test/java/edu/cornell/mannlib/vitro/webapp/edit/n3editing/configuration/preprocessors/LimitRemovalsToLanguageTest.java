package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;
import org.testng.Assert;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class LimitRemovalsToLanguageTest extends AbstractTestClass {
    
    @Test
    /**
     * Test that retractions are properly limited to the specified language
     */
    public void testPreprocess() {
        LimitRemovalsToLanguage preproc = new LimitRemovalsToLanguage("en-US");
        Model additions = ModelFactory.createDefaultModel();
        Model retractions = ModelFactory.createDefaultModel();
        Resource res = ResourceFactory.createResource("http://example.com/i/n1");
        // eliminate Spanish retraction if only English is being edited
        retractions.add(res, RDFS.label, ResourceFactory.createLangLiteral("en-US1", "en-US"));
        retractions.add(res, RDFS.label, ResourceFactory.createLangLiteral("es1", "es"));
        additions.add(res, RDFS.label, ResourceFactory.createLangLiteral("en-US2", "en-US"));
        preproc.preprocess(retractions, additions, null);
        Assert.assertEquals(retractions.size(), 1);
        Assert.assertTrue(retractions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("en-US1", "en-US")));
        Assert.assertEquals(additions.size(), 1);
        Assert.assertTrue(additions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("en-US2", "en-US")));
        additions.removeAll();
        retractions.removeAll();
        // Keep all retractions unmolested if no labels at all are being re-added.
        // (The form may be trying to delete the entire individual.)
        retractions.add(res, RDFS.label, ResourceFactory.createLangLiteral("en-US1", "en-US"));
        retractions.add(res, RDFS.label, ResourceFactory.createLangLiteral("es1", "es"));
        preproc.preprocess(retractions, additions, null);
        Assert.assertEquals(retractions.size(), 2);
        Assert.assertTrue(retractions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("en-US1", "en-US")));
        Assert.assertTrue(retractions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("es1", "es")));
        Assert.assertEquals(additions.size(), 0);
        additions.removeAll();
        retractions.removeAll();
        // Keep both retractions if the form supplies new values for both languages
        retractions.add(res, RDFS.label, ResourceFactory.createLangLiteral("en-US1", "en-US"));
        retractions.add(res, RDFS.label, ResourceFactory.createLangLiteral("es1", "es"));
        additions.add(res, RDFS.label, ResourceFactory.createLangLiteral("en-US2", "en-US"));
        additions.add(res, RDFS.label, ResourceFactory.createLangLiteral("es2", "es"));
        preproc.preprocess(retractions, additions, null);        
        Assert.assertEquals(retractions.size(), 2);
        Assert.assertTrue(retractions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("en-US1", "en-US")));
        Assert.assertTrue(retractions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("es1", "es")));
        Assert.assertEquals(additions.size(), 2);
        Assert.assertTrue(additions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("en-US2", "en-US")));
        Assert.assertTrue(additions.contains(res, RDFS.label, ResourceFactory.createLangLiteral("es2", "es")));
        additions.removeAll();
        retractions.removeAll();
    }
 
}
