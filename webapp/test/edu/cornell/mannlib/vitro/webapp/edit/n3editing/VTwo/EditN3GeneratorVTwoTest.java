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
