/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import org.junit.Assert;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.N3EditUtils;


public class EditN3UtilsTest {

    @Test
    public void testStripInvalidXMLChars() {
        Model m = ModelFactory.createDefaultModel();
        String containsInvalidXMLChars = "Blah \u0001blah \u0002blah\uDDDD";
        String clean = "Blah blah blah"; 
        
        // add a statement with the literal incompatible with XML to model m
        m.add(m.createResource(), RDFS.label, containsInvalidXMLChars);
        
        Assert.assertFalse(isSerializableAsXML(m));
        
        String stripped = N3EditUtils.stripInvalidXMLChars(
                                containsInvalidXMLChars);
        Assert.assertEquals(clean, stripped);
        
        // clear the model of any statements
        m.removeAll();
        // add a statement with a literal that has been stripped of bad chars
        m.add(m.createResource(), RDFS.label, stripped);
        
        Assert.assertTrue(isSerializableAsXML(m));      
    }
    
    private boolean isSerializableAsXML(Model m) {
        try {
            NullOutputStream nullStream = new NullOutputStream();
            m.write(nullStream, "RDF/XML");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
}
