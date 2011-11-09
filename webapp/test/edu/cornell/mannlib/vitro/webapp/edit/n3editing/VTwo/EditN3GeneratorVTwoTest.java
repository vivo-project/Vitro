/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class EditN3GeneratorVTwoTest {
    @Test 
    public void testSubInMultiUrisNull(){
        String n3 = "?varXYZ" ;
        List<String> targets = new ArrayList<String>();
        targets.add(n3);
        
        Map<String,List<String>> keyToValues = new HashMap<String,List<String>>();
        List<String> targetValue = new ArrayList<String>();        
        targetValue.add(null);       
        keyToValues.put("varXYZ", targetValue);
        
        List<String> result = EditN3GeneratorVTwo.subInMultiUris(keyToValues, targets);
        Assert.assertNotNull(result);
        Assert.assertEquals(1,result.size());
        
        String resultN3 = result.get(0);
        Assert.assertNotNull(resultN3);
        Assert.assertTrue("String was empty", !resultN3.isEmpty());
        
        String not_expected = "<null>";
        Assert.assertTrue("must not sub in <null>", !not_expected.equals(resultN3));
    }
    
    
    @Test 
    public void testSubInUrisNull(){
        String n3 = " ?varXYZ " ;
        List<String> targets = new ArrayList<String>();
        targets.add(n3);
        
        Map<String,String> keyToValues = new HashMap<String,String>();                       
        keyToValues.put("varXYZ", "xyzURI");
        
        List<String> result = EditN3GeneratorVTwo.subInUris(keyToValues, targets);
        Assert.assertNotNull(result);
        Assert.assertEquals(1,result.size());
        
        String resultN3 = result.get(0);
        Assert.assertNotNull(resultN3);
        Assert.assertTrue("String was empty", !resultN3.isEmpty());                
        Assert.assertEquals(" <xyzURI> ", resultN3);
        
        keyToValues = new HashMap<String,String>();                       
        keyToValues.put("varXYZ", null);
        
        result = EditN3GeneratorVTwo.subInUris(keyToValues, targets);
        Assert.assertNotNull(result);
        Assert.assertEquals(1,result.size());
        
        resultN3 = result.get(0);
        resultN3 = result.get(0);
        Assert.assertNotNull(resultN3);
        Assert.assertTrue("String was empty", !resultN3.isEmpty());                
        Assert.assertEquals(" ?varXYZ ", resultN3);
    }

    
    
    /*
     
      
      [@prefix core: <http://vivoweb.org/ontology/core#> .
?person core:educationalTraining  ?edTraining .
?edTraining  a core:EducationalTraining ;
core:educationalTrainingOf ?person ;
<http://vivoweb.org/ontology/core#trainingAtOrganization> ?org .
, ?org <http://www.w3.org/2000/01/rdf-schema#label> ?orgLabel ., ?org a ?orgType .] 

     */

    //{person=http://caruso-laptop.mannlib.cornell.edu:8090/vivo/individual/n2576, predicate=http://vivoweb.org/ontology/core#educationalTraining, edTraining=null}
        
    
	@Test
    public void testSubInMultiUris() {
        String n3 = "?subject ?predicate ?multivalue ." ;
        List<String> strs = new ArrayList<String>();
        strs.add(n3);
         
        Map<String,List<String>> keyToValues = new HashMap<String,List<String>>();
        List<String> values = new ArrayList<String>();               
        values.add("http://a.com/2");
        values.add("http://b.com/ont#2");
        values.add("http://c.com/individual/n23431");        
        keyToValues.put("multivalue", values);
        
        List<String> subject = new ArrayList<String>();
        List<String> predicate = new ArrayList<String>();
        subject.add("http://testsubject.com/1");
        predicate.add("http://testpredicate.com/2");
        keyToValues.put("subject", subject);
        keyToValues.put("predicate", predicate);
        
        List<String> n3results = EditN3GeneratorVTwo.subInMultiUris(keyToValues, strs);
        
        Assert.assertNotNull(n3results);
        Assert.assertTrue( n3results.size() == 1 );
        String expected ="<http://testsubject.com/1> <http://testpredicate.com/2> <http://a.com/2>, <http://b.com/ont#2>, <http://c.com/individual/n23431> .";
        Assert.assertEquals(expected, n3results.get(0));
        
        //Replace subject and predicate with other variables
        
        //make a model,
        Model expectedModel = ModelFactory.createDefaultModel();
        StringReader expectedReader = new StringReader(expected);
        StringReader resultReader = new StringReader(n3results.get(0));
        expectedModel.read(expectedReader, null, "N3");
        Model resultModel = ModelFactory.createDefaultModel();
        resultModel.read(resultReader, null, "N3");
        Assert.assertTrue(expectedModel.isIsomorphicWith(resultModel));
    }
}
