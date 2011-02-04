/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


public class PropertyInstanceDaoJenaTest {
	String isDependentRelation =
		" <"+VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT+"> \"true\"^^xsd:boolean .\n" ;
	
	String nosePropIsDependentRel = 
	"<"+VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT+"> rdf:type owl:AnnotationProperty .\n" +
    " ex:hasNose " + isDependentRelation;
	
    String prefixesN3 = 
        "@prefix vitro: <" + VitroVocabulary.vitroURI + "> . \n" +
        "@prefix xsd: <" + XSD.getURI() + "> . \n " +
        "@prefix ex: <http://example.com/> . \n" +            
        "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"+
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"+
        "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n";

	
    void printModels(Model expected, Model result){
    	System.out.println("Expected:");
    	expected.write(System.out);
    	System.out.println("Result:");
    	result.write(System.out);    
    }
    
 
    @org.junit.Test
    public void testStmtNonForceDelete() {
        String n3 = 
            prefixesN3 +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        String expected = 
            prefixesN3 +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.add(model.listStatements());        
        WebappDaoFactory wdf = new WebappDaoFactoryJena(ontModel);
        wdf.getPropertyInstanceDao().deleteObjectPropertyStatement("http://example.com/bob", "http://example.com/hasNose", "http://example.com/nose1");       
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        wipeOutModTime(ontModel);
        //Model resultModel = ModelFactory.createDefaultModel().add(ontModel.listStatements());
        
        boolean same = expectedModel.isIsomorphicWith( ontModel.getBaseModel() );
        if( ! same ) printModels( expectedModel, ontModel.getBaseModel());
        Assert.assertTrue( same );
    }
    
    
    @org.junit.Test
    public void testStmtSimpleForceDelete() {
        String n3= 
            prefixesN3 + 
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation + 
            " ex:bob ex:hasNose ex:nose1 .   \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation ;                                          
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.add(model.listStatements());        
        WebappDaoFactory wdf = new WebappDaoFactoryJena(ontModel);
        wdf.getPropertyInstanceDao().deleteObjectPropertyStatement("http://example.com/bob", "http://example.com/hasNose", "http://example.com/nose1");       
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        wipeOutModTime(ontModel);
        //Model resultModel = ModelFactory.createDefaultModel().add(ontModel.listStatements());
        
        boolean same = expectedModel.isIsomorphicWith( ontModel.getBaseModel() );
        if( ! same ) printModels( expectedModel, ontModel.getBaseModel());
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testStmtForceDeleteWithLiterals() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n"  ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.add(model.listStatements());        
        WebappDaoFactory wdf = new WebappDaoFactoryJena(ontModel);
        wdf.getPropertyInstanceDao().deleteObjectPropertyStatement("http://example.com/bob", "http://example.com/hasNose", "http://example.com/nose1");       
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        wipeOutModTime(ontModel);
        //Model resultModel = ModelFactory.createDefaultModel().add(ontModel.listStatements());
        
        boolean same = expectedModel.isIsomorphicWith( ontModel.getBaseModel() );
        if( ! same ) printModels( expectedModel, ontModel.getBaseModel());
        Assert.assertTrue( same );
    }

    void wipeOutModTime(Model model){
		model.removeAll(null, model.createProperty(VitroVocabulary.MODTIME), null);
	}
    
    @org.junit.Test
    public void testGetAllPossiblePropInstForIndividual() {
        String n3 = prefixesN3 +
            "ex:hasMold a owl:ObjectProperty . \n" +
            "ex:hasSpore a owl:ObjectProperty . \n" +
            "ex:hasFungus a owl:ObjectProperty . \n" +
            "ex:redHerring a owl:ObjectProperty . \n" +
            "ex:Person a owl:Class . \n" +
            "ex:Agent a owl:Class . \n" +
            "ex:Mold a owl:Class . \n" +
            "ex:Spore a owl:Class . \n" +
            "ex:Fungus a owl:Class . \n" +
            "ex:Organism a owl:Class . \n" +
            "ex:Mold rdfs:subClassOf ex:Organism . \n" +
            "ex:Spore rdfs:subClassOf ex:Organism . \n" +
            "ex:Fungus rdfs:subClassOf ex:Organism . \n" +
            "ex:Person rdfs:subClassOf ex:Agent . \n" +
            "ex:hasFungus rdfs:range ex:Fungus . \n" +
            "ex:hasFungus rdfs:domain ex:Agent . \n" +
            "ex:Agent rdfs:subClassOf [ a owl:Restriction ; \n" +
                                       "owl:onProperty ex:hasMold ; \n" +
                                       "owl:allValuesFrom ex:Organism ] . \n" +
            "ex:Person rdfs:subClassOf [ a owl:Restriction ; \n" +
                                       "owl:onProperty ex:hasMold ; \n" +
                                       "owl:allValuesFrom ex:Mold ] . \n" +
            "ex:Agent rdfs:subClassOf [ a owl:Restriction ; \n" +
                                       "owl:onProperty ex:hasSpore ; \n" +
                                       "owl:allValuesFrom ex:Organism ] . \n" +
            "ex:Person rdfs:subClassOf [ a owl:Restriction ; \n" +
                                       "owl:onProperty ex:hasSpore ; \n" +
                                       "owl:someValuesFrom ex:Spore ] . \n" +            
            "ex:bob a ex:Person ; a ex:Agent . \n";
        
        // The applicable properties for bob should be:
        // 1. hasMold (values from Mold)
        // 2. hasSpore (values from Organism)
        // 3. hasFungus (values from Fungus)
        
        OntModel ontModel = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM));
        ontModel.read(new StringReader(n3), null, "N3");
        
        WebappDaoFactory wadf = new WebappDaoFactoryJena(ontModel);
        Assert.assertEquals(4, wadf.getObjectPropertyDao().getAllObjectProperties().size());
        Assert.assertEquals(6, wadf.getVClassDao().getAllVclasses().size());
        Assert.assertNotNull(wadf.getIndividualDao().getIndividualByURI("http://example.com/bob"));
        
        Collection<PropertyInstance> pinsts = wadf.getPropertyInstanceDao()
                .getAllPossiblePropInstForIndividual("http://example.com/bob");
        
        Assert.assertEquals(3, pinsts.size());
        
        Map<String, String> propToRange = new HashMap<String,String>();
        for (PropertyInstance pi : pinsts) {
            propToRange.put(pi.getPropertyURI(), pi.getRangeClassURI());
        }
        
        Assert.assertEquals("http://example.com/Mold", propToRange.get("http://example.com/hasMold"));
        Assert.assertEquals("http://example.com/Organism", propToRange.get("http://example.com/hasSpore"));
        Assert.assertEquals("http://example.com/Fungus", propToRange.get("http://example.com/hasFungus"));    
            
    }
}
