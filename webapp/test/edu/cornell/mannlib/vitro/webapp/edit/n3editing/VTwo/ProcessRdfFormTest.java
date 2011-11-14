/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

public class ProcessRdfFormTest extends AbstractTestClass{
     
    @Test
    public void basicNewStatementTest() throws Exception{
        
        /* A very basic new statement edit. */        
        EditConfigurationVTwo config = new EditConfigurationVTwo();
        config.setEditKey("mockEditKey");
        config.setN3Required(Arrays.asList("?test1 ?test2 ?test3 ." ));
        config.setUrisOnform(Arrays.asList("test1", "test2", "test3"));
        
        Map<String,String[]> values = new HashMap<String, String[]>();        
        values.put("test1", (new String[] {"http://test.com/uri1"}));
        values.put("test2", (new String[] {"http://test.com/uri2"}));
        values.put("test3", (new String[] {"http://test.com/uri3"}));
        values.put("editKey", (new String[] {"mockEditKey"}));
                
        MultiValueEditSubmission submission = new MultiValueEditSubmission(values, config);
        
        ProcessRdfForm processor = new ProcessRdfForm(config,getMockNewURIMaker());
        
        /* test just the N3 substitution part */
        List<String>req = config.getN3Required();
        List<String>opt = config.getN3Optional();
        processor.subInValuesToN3( config , submission, req, opt, null , null);
        assertNotNull(req);
        assertTrue( req.size() > 0);
        assertNotNull(req.get(0));
        assertEquals("<http://test.com/uri1> <http://test.com/uri2> <http://test.com/uri3> .", req.get(0));
        /* test the N3 and parse RDF parts */
        AdditionsAndRetractions changes = processor.process( config, submission );
        
        assertNotNull( changes );
        assertNotNull( changes.getAdditions() );
        assertNotNull( changes.getRetractions());
        assertTrue( changes.getAdditions().size() == 1 );
        assertTrue( changes.getRetractions().size() == 0 );
        
        assertTrue( changes.getAdditions().contains(
                ResourceFactory.createResource("http://test.com/uri1"), 
                ResourceFactory.createProperty("http://test.com/uri2"),
                ResourceFactory.createResource("http://test.com/uri3")));
    }

    /* A very basic edit of an existing statement. */
    @Test
    public void basicEditStatement() throws Exception{
        String testXURI = "http://test.com/uriX";
        String testYURI = "http://test.com/uriY";
        String testZURIOrginal = "http://test.com/uriZ";
        String testZURIChanged = "http://test.com/uriZChanged";
        
        /* set up model */
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(testXURI), 
                  model.createProperty(testYURI), 
                  model.createResource(testZURIOrginal));
        
        /* set up EditConfiguration */
        EditConfigurationVTwo config = new EditConfigurationVTwo();
        config.setEditKey("mockEditKey");        
        config.setUrisOnform(Arrays.asList("testX", "testY", "testZ"));
        config.setN3Required( Arrays.asList("?testX ?testY ?testZ ." ));
        
        config.setVarNameForSubject("testX");
        config.setSubjectUri(testXURI);
        
        config.setPredicateUri(testYURI);
        config.setVarNameForPredicate("testY");
        
        config.setObject(testZURIOrginal);
        config.setVarNameForObject("testZ");                
                
        config.prepareForObjPropUpdate(model);               
        
        /* set up Submission */        
        Map<String,String[]> values = new HashMap<String, String[]>();                
        values.put("testZ", (new String[] {testZURIChanged}));
        values.put("editKey", (new String[] {"mockEditKey"}));               
        MultiValueEditSubmission submission = new MultiValueEditSubmission(values, config);
        
        ProcessRdfForm processor = new ProcessRdfForm(config,getMockNewURIMaker());        
        AdditionsAndRetractions changes = processor.process( config, submission );
             
        assertNotNull( changes );
        assertNotNull( changes.getAdditions() );
        assertNotNull( changes.getRetractions());
        
        assertTrue( changes.getAdditions().size() == 1 );
        assertTrue( changes.getRetractions().size() == 1 );
        
        assertTrue( changes.getAdditions().contains(
                ResourceFactory.createResource(testXURI), 
                ResourceFactory.createProperty(testYURI),
                ResourceFactory.createResource(testZURIChanged)));
        
        assertTrue( changes.getRetractions().contains(
                ResourceFactory.createResource(testXURI), 
                ResourceFactory.createProperty(testYURI),
                ResourceFactory.createResource(testZURIOrginal)));        
    }
    
    @Test
    public void substituteInSubPredObjURIsTest(){        
        String testXURI = "http://test.com/uriX";
        String testYURI = "http://test.com/uriY";
        String testZURI = "http://test.com/uriZ";        
                
        /* set up EditConfiguration */
        EditConfigurationVTwo config = new EditConfigurationVTwo();
        
        config.setVarNameForSubject("testX");
        config.setSubjectUri(testXURI);
        
        config.setPredicateUri(testYURI);
        config.setVarNameForPredicate("testY");
        
        config.setObject(testZURI);
        config.setVarNameForObject("testZ");
        
        List<String> a = Arrays.asList("a.0 ?testX ?testY ?testZ.", "a.1 ?testX ?testY ?testZ.");
        List<String> b = Arrays.asList("b.0 ?testX ?testY ?testZ.", "b.1 ?testX ?testY ?testZ.");
        
        ProcessRdfForm processor = new ProcessRdfForm(config,getMockNewURIMaker());                        
        
        processor.substituteInSubPredObjURIs(config, a, b);
        assertEquals("a.0 <" + testXURI + "> <" + testYURI + "> <" + testZURI + ">.", a.get(0));
        assertEquals("a.1 <" + testXURI + "> <" + testYURI + "> <" + testZURI + ">.", a.get(1));        
        assertEquals("b.0 <" + testXURI + "> <" + testYURI + "> <" + testZURI + ">.", b.get(0));
        assertEquals("b.1 <" + testXURI + "> <" + testYURI + "> <" + testZURI + ">.", b.get(1));
        
    }

    @Test
    public void unicodeTest() throws Exception{
        /* A test unicode characters with new statement edit. */                
        
        /* make configuration */
        EditConfigurationVTwo config = new EditConfigurationVTwo();
        config.setEditKey("mockEditKey");
        config.setN3Required(Arrays.asList("?test1 ?test2 ?test3 ." ));
        config.setUrisOnform(Arrays.asList("test1", "test2", "test3"));
        
        String test1 = "http://test.com/uriWithUnicodeƺ",
            test2 = "http://test.com/latin-1-ÙåàÞñöÿ",
            test3 = "http://test.com/moreUnicode-ἎἘὤ" ;
        
        /* make submission */
        Map<String,String[]> values = new HashMap<String, String[]>();        
        values.put("test1", (new String[] {test1}));
        values.put("test2", (new String[] {test2}));
        values.put("test3", (new String[] {test3}));
        values.put("editKey", (new String[] {"mockEditKey"}));                
        MultiValueEditSubmission submission = new MultiValueEditSubmission(values, config);
                
        ProcessRdfForm processor = new ProcessRdfForm(config,getMockNewURIMaker());
        
        /* test just the N3 substitution part */
        List<String>req = config.getN3Required();
        List<String>opt = config.getN3Optional();
        processor.subInValuesToN3( config , submission, req, opt, null , null);
        assertNotNull(req);
        assertTrue( req.size() > 0);
        assertNotNull(req.get(0));
        assertEquals("<" +test1+ "> <" +test2+ "> <" +test3+ "> .", req.get(0));
        
        /* test the N3 and parse RDF parts */
        AdditionsAndRetractions changes = processor.process( config, submission );
        
        assertNotNull( changes );
        assertNotNull( changes.getAdditions() );
        assertNotNull( changes.getRetractions());
        assertTrue( changes.getAdditions().size() == 1 );
        assertTrue( changes.getRetractions().size() == 0 );
        
        assertTrue( changes.getAdditions().contains(
                ResourceFactory.createResource(test1), 
                ResourceFactory.createProperty(test2),
                ResourceFactory.createResource(test3)));
    }
    
    @Test
    public void basicNewResourceTest() throws Exception{       
        /* A very basic new statement edit. */        
        EditConfigurationVTwo config = new EditConfigurationVTwo();
        config.setEditKey("mockEditKey");
        config.setN3Required(Arrays.asList("?newRes ?test2 ?test3 ." ));
        config.setUrisOnform(Arrays.asList( "test2", "test3"));
        config.addNewResource("newRes", null);
        config.setEntityToReturnTo("?newRes");        
        
        Map<String,String[]> values = new HashMap<String, String[]>();        

        values.put("test2", (new String[] {"http://test.com/uri2"}));
        values.put("test3", (new String[] {"http://test.com/uri3"}));
        values.put("editKey", (new String[] {"mockEditKey"}));
                
        MultiValueEditSubmission submission = new MultiValueEditSubmission(values, config);
        
        ProcessRdfForm processor = new ProcessRdfForm(config,getMockNewURIMaker());
        
        /* test just the N3 substitution part */
        List<String>req = config.getN3Required();
        List<String>opt = config.getN3Optional();
        processor.subInValuesToN3( config , submission, req, opt, null , null);
        assertNotNull(req);
        assertTrue( req.size() > 0);
        assertNotNull(req.get(0));
        assertEquals("<"+NEWURI_STRING + "0> <http://test.com/uri2> <http://test.com/uri3> .", req.get(0));
        
        assertEquals("<" + NEWURI_STRING + "0>", submission.getEntityToReturnTo());
        
        /* test the N3 and parse RDF parts */
        AdditionsAndRetractions changes = processor.process( config, submission );
        
        assertNotNull( changes );
        assertNotNull( changes.getAdditions() );
        assertNotNull( changes.getRetractions());
        assertTrue( changes.getAdditions().size() == 1 );
        assertTrue( changes.getRetractions().size() == 0 );
        
        assertTrue( changes.getAdditions().contains(
                ResourceFactory.createResource(NEWURI_STRING + "0"), 
                ResourceFactory.createProperty("http://test.com/uri2"),
                ResourceFactory.createResource("http://test.com/uri3")));
    }
    
    
    String NEWURI_STRING= "http://newURI/n";
    
    public NewURIMaker getMockNewURIMaker(){
        return new NewURIMaker() {
            int count = 0; 
            @Override
            public String getUnusedNewURI(String prefixURI) throws InsertException {
                if( prefixURI != null )
                    return prefixURI + count;
                else
                    return NEWURI_STRING + count;
            }
        };        
    }
}
