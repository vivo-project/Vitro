/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


public class EditN3GeneratorVTwoTest {
    static EditN3GeneratorVTwo gen = new EditN3GeneratorVTwo();
    
    @Test
    public void testNullTarget(){
        List<String> targets = Arrays.asList("?var",null,null,"?var"); 
        
        Map<String,List<String>> keyToValues = new HashMap<String,List<String>>();        
        keyToValues.put("var", Arrays.asList("ABC"));
        keyToValues.put("var2", Arrays.asList((String)null));        
        /* test for exception */
        gen.subInMultiUris(null, targets);
        gen.subInMultiUris(keyToValues, null);
        gen.subInMultiUris(keyToValues, targets);
        
        Map<String,List<Literal>> keyToLiterals = new HashMap<String,List<Literal>>();        
        keyToLiterals.put("var", Arrays.asList( ResourceFactory.createTypedLiteral("String")));
        keyToLiterals.put("var2", Arrays.asList( (Literal)null));
        /* test for exception */
        gen.subInMultiLiterals(keyToLiterals, targets);
        gen.subInMultiLiterals(keyToLiterals, null);
        gen.subInMultiLiterals(null, targets);
    }
    
    
    @Test
    public void testPunctAfterVarName(){        
        List<String> targets = Arrays.asList("?var.","?var;","?var]","?var,"); 
        
        Map<String,List<String>> keyToValues = new HashMap<String,List<String>>();        
        keyToValues.put("var", Arrays.asList("ABC"));
        
        gen.subInMultiUris(keyToValues, targets);
        Assert.assertNotNull(targets);
        Assert.assertEquals(4,targets.size());
        
        Assert.assertEquals("<ABC>.", targets.get(0));
        Assert.assertEquals("<ABC>;", targets.get(1));
        Assert.assertEquals("<ABC>]", targets.get(2));
        Assert.assertEquals("<ABC>,", targets.get(3));                
    }
    
    @Test 
    public void testSubInMultiUrisNull(){        
        String n3 = "?varXYZ" ;
        List<String> targets = new ArrayList<String>();
        targets.add(n3);
        
        Map<String,List<String>> keyToValues = new HashMap<String,List<String>>();
        List<String> targetValue = new ArrayList<String>();        
        targetValue.add(null);       
        keyToValues.put("varXYZ", targetValue);
        
        gen.subInMultiUris(keyToValues, targets);
        Assert.assertNotNull(targets);
        Assert.assertEquals(1,targets.size());
        
        String resultN3 = targets.get(0);
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
        
        gen.subInUris(keyToValues, targets);
        List<String> result = targets;
        Assert.assertNotNull(result);
        Assert.assertEquals(1,result.size());
        
        String resultN3 = result.get(0);
        Assert.assertNotNull(resultN3);
        Assert.assertTrue("String was empty", !resultN3.isEmpty());                
        Assert.assertEquals(" <xyzURI> ", resultN3);
        
        keyToValues = new HashMap<String,String>();                       
        keyToValues.put("varXYZ", null);
                
        List<String> targets2 = new ArrayList<String>();
        targets2.add(n3);
        
        gen.subInUris(keyToValues, targets2);
        Assert.assertNotNull(targets2);
        Assert.assertEquals(1,targets2.size());
        
        resultN3 = targets2.get(0);        
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
        
        gen.subInMultiUris(keyToValues, strs);        
        
        Assert.assertNotNull(strs);
        Assert.assertTrue( strs.size() == 1 );
        String expected ="<http://testsubject.com/1> <http://testpredicate.com/2> <http://a.com/2>, <http://b.com/ont#2>, <http://c.com/individual/n23431> .";
        Assert.assertEquals(expected, strs.get(0));
        
        //Replace subject and predicate with other variables
        
        //make a model,
        Model expectedModel = ModelFactory.createDefaultModel();
        StringReader expectedReader = new StringReader(expected);
        StringReader resultReader = new StringReader(strs.get(0));
        expectedModel.read(expectedReader, null, "N3");
        Model resultModel = ModelFactory.createDefaultModel();
        resultModel.read(resultReader, null, "N3");
        Assert.assertTrue(expectedModel.isIsomorphicWith(resultModel));
    }
}
