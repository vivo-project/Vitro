package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.StringReader;
import java.util.List;

import junit.framework.Assert;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.XSD;

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
}
