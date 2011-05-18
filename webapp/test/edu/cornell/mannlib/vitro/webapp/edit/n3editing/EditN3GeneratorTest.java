/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Generator;

public class EditN3GeneratorTest {

    EditN3Generator en3g ;
    
    @Before
    public void setUp() throws Exception {
        en3g = new EditN3Generator((EditConfiguration) null);
    }

    @Test
    public void testSubInLiterals() {
        String var = "TestVar";
        String target = "Fake n3 ?TestVar .";
        Literal literal = null;
        
        EditN3Generator en3g = new EditN3Generator((EditConfiguration) null);
        String result = en3g.subInLiterals(var, literal, target);
        Assert.assertNotNull( result );               
    }
    
    
    @Test
    public void testSubInLiteralsWithGroupReference() {
        String var = "TestVar";
        String target = "Fake n3 ?TestVar .";
        Literal literal = new EditLiteral("should not a regex group --> ?2 <--  blblkj (lskdfj) " ,null,null);
        
        EditN3Generator en3g = new EditN3Generator((EditConfiguration) null);
        String result = en3g.subInLiterals(var, literal, target);
        Assert.assertNotNull( result );    
        Assert.assertEquals("Fake n3 \"should not a regex group --> ?2 <--  blblkj (lskdfj) \" ." , result);
    }
    
    @Test
    public void testConflictingVarNames(){
      Map<String,String> varToExisting= new HashMap<String,String>();      
      varToExisting.put("bob", "http://uri.edu#BobTheElder");
      varToExisting.put("bobJr", "http://uri.edu#BobTheSon");
             
      String target =   "SELECT ?cat WHERE{ ?bobJr <http://uri.edu#hasCat> ?cat }" ;
      List<String> targets = new ArrayList<String>();
      targets.add(target);            
      
      List<String> out = en3g.subInUris(varToExisting, targets);
      Assert.assertNotNull(out);
      Assert.assertNotNull( out.get(0) );      
      String expected = "SELECT ?cat WHERE{ <http://uri.edu#BobTheSon>  <http://uri.edu#hasCat> ?cat }";
      Assert.assertEquals(expected, out.get(0) );
      
      //force a non match on a initial-partial var name
      varToExisting= new HashMap<String,String>();      
      varToExisting.put("bob", "http://uri.edu#BobTheElder");      
             
      target =   "SELECT ?cat WHERE{ ?bobJr <http://uri.edu#hasCat> ?cat }" ;
      targets = new ArrayList<String>();
      targets.add(target);            
      
      out = en3g.subInUris(varToExisting, targets);
      Assert.assertNotNull(out);
      Assert.assertNotNull( out.get(0) );      
      expected = target;
      Assert.assertEquals(expected, out.get(0) );
      
    }

}
