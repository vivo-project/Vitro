/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.StringReader;
import java.util.List;

import junit.framework.Assert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class DependentResourceDeleteJenaTest {
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
    public void testStmtNormalDelete() {
        String n3 = 
            prefixesN3 +
            " ex:bob ex:hasNose ex:nose1 .  " ;            
                        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                                             
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createStatement(
        				model.createResource("http://example.com/bob"),
        				model.createProperty("http://example.com/hasNose"),
        				model.createResource("http://example.com/nose1")),
        		model);
        
        Model resultModel = ModelFactory.createDefaultModel();
        resultModel.add(deletes);
        
        //all statements should be deleted        
        Assert.assertTrue(resultModel.isIsomorphicWith( model ));
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
            " ex:bob ex:hasNose ex:nose1 .   \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createStatement(
        				model.createResource("http://example.com/bob"),
        				model.createProperty("http://example.com/hasNose"),
        				model.createResource("http://example.com/nose1")),
        				model);
        
        Model resultModel = ModelFactory.createDefaultModel();
        resultModel.add(deletes);
                
        //all statements should be deleted               
        Assert.assertTrue( resultModel.isIsomorphicWith( expectedModel ) ) ;              
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
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createStatement(
        				model.createResource("http://example.com/bob"),
        				model.createProperty("http://example.com/hasNose"),
        				model.createResource("http://example.com/nose1")),
        				model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
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
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
                model.createStatement(
                        model.createResource("http://example.com/bob"),
                        model.createProperty("http://example.com/hasNose"),
                        model.createResource("http://example.com/nose1")),
                model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }

    @org.junit.Test
    public void testStmtForceDeleteWithSimpleCycles() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:c ex:bob ." ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +                  
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\"." ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
       
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
                model.createStatement(
                        model.createResource("http://example.com/bob"),
                        model.createProperty("http://example.com/hasNose"),
                        model.createResource("http://example.com/nose1")),
                model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testStmtForceDeleteWithCycles() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
            " ex:nose1 ex:c ex:bob . \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:c ex:bob . \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n"  ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
       
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
                model.createStatement(
                        model.createResource("http://example.com/bob"),
                        model.createProperty("http://example.com/hasNose"),
                        model.createResource("http://example.com/nose1")),
                model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testStmtForceDeleteWithCycles2() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
            " ex:nose1 ex:c ex:nose1 . \n" +
            " ex:nose1 ex:c ex:bob . \n" ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n"  ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
                model.createStatement(
                        model.createResource("http://example.com/bob"),
                        model.createProperty("http://example.com/hasNose"),
                        model.createResource("http://example.com/nose1")),
                model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testStmtForceDeleteWithLinks() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:c ex:glasses65 . \n" +
            " ex:glasses65 ex:c ex:nose1 . \n" +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n"  +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
       
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
                model.createStatement(
                        model.createResource("http://example.com/bob"),
                        model.createProperty("http://example.com/hasNose"),
                        model.createResource("http://example.com/nose1")),
                model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testStmtForceDeleteWithBNodes() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose [ \n" +            
            "    ex:a \"this is a bnode\"; \n" +
            "    ex:c ex:glasses65 ] . \n" +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n"  +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        
        StmtIterator stmtIt = model.listStatements(
        		model.createResource("http://example.com/bob"),
                model.createProperty("http://example.com/hasNose"),
                (RDFNode)null);
                
        List<Statement> deletes = 
        	DependentResourceDeleteJena.getDependentResourceDeleteList(stmtIt.nextStatement(),model);        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testStmtForceDeleteWithNestedBNodes() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose [ \n" +            
            "    ex:a \"this is a bnode\"; \n" +
            "    ex:c ex:glasses65 ; \n" +
            "    ex:c [ " + 
            "           ex:a \"this is a nested bnode\" ] " +
            "] . \n" +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n"  +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
               
        StmtIterator stmtIt = model.listStatements(
        		model.createResource("http://example.com/bob"),
                model.createProperty("http://example.com/hasNose"),
                (RDFNode)null);
                
        List<Statement> deletes = 
        	DependentResourceDeleteJena.getDependentResourceDeleteList(stmtIt.nextStatement(),model);        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testResNormalDelete() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:hasNose ex:nose1 .  " ;     
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel ;
                    
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(        		
        				model.createResource("http://example.com/nose1"),model);        
        Model resultModel = model.remove(deletes);        
                
        //all statements should be deleted
        boolean same = resultModel.isIsomorphicWith( expectedModel );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same);   
    }
    
    @org.junit.Test
    public void testResSimpleForceDelete() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:hasNose ex:nose1 .   \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +                    	
        	" ex:hair23 ex:hasHairCount \"23\". " ;        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(        		
        				model.createResource("http://example.com/nose1"),model);        
        Model resultModel = model.remove(deletes);
                
        //all statements should be deleted
        boolean same = resultModel.isIsomorphicWith( expectedModel );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same);                
    }
    

    @org.junit.Test
    public void testResNonForceDelete() {
        String n3 = 
            prefixesN3 +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;        
        String expected = 
            prefixesN3 +                                	
        	" ex:hair23 ex:hasHairCount \"23\". " ;        	       
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(        		
        				model.createResource("http://example.com/nose1"),model);        
        Model resultModel = model.remove(deletes);
                
        //all statements should be deleted
        boolean same = resultModel.isIsomorphicWith( expectedModel );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same);      
    }

    @org.junit.Test
    public void testResNonForceDelete2() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;        
        String expected = 
            prefixesN3 +            
            nosePropIsDependentRel +
        	" ex:hair23 ex:hasHairCount \"23\". " ;        	       
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(        		
        				model.createResource("http://example.com/nose1"),model);        
        Model resultModel = model.remove(deletes);
                
        //all statements should be deleted
        boolean same = resultModel.isIsomorphicWith( expectedModel );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same);      
    }
    
    @org.junit.Test
    public void testResForceDeleteWithLiterals() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:hasHair " + isDependentRelation + 
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n"  ;        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createResource("http://example.com/nose1"),model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testResForceDeleteWithCycles() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
            " ex:nose1 ex:c ex:bob . \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:c ex:bob . \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            "ex:hasHair " + isDependentRelation +
            " ex:bob ex:a \"Bob\".   \n"  ;        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
       
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createResource("http://example.com/nose1"),model);
        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testResForceDeleteWithCycles2() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
            " ex:nose1 ex:c ex:nose1 . \n" +
            " ex:nose1 ex:c ex:bob . \n" ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel ;        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
        
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createResource("http://example.com/bob"),model);        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
    @org.junit.Test
    public void testResForceDeleteWithLinks() {
        String n3 = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:c ex:glasses65 . \n" +
            " ex:glasses65 ex:c ex:nose1 . \n" +
            " ex:glasses65 ex:a \"glasses 65\" ." ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n"  +
            " ex:glasses65 ex:a \"glasses 65\" ." ;        
        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
       
        List<Statement> deletes = DependentResourceDeleteJena.getDependentResourceDeleteList(
        		model.createResource("http://example.com/nose1"),model);        
        model.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( model );
        if( ! same ) printModels( expectedModel, model);
        Assert.assertTrue( same );
    }
    
//    @org.junit.Test
//    public void testResForceDeleteWithBNodes() {
//        String n3 = 
//            prefixesN3 +
//            nosePropIsDependentRel +
//            " ex:bob ex:a \"Bob\".   \n" +
//            " ex:bob ex:hasNose [ \n" +            
//            "    ex:a \"this is a bnode\"; \n" +
//            "    ex:c ex:glasses65 ] . \n" +
//            " ex:glasses65 ex:a \"glasses 65\" ." ;
//        
//        String expected = 
//            prefixesN3 +
//            nosePropIsDependentRel +
//            " ex:bob ex:a \"Bob\".   \n"  +
//            " ex:glasses65 ex:a \"glasses 65\" ." ;       
//        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
//        
//        StmtIterator stmtIt = model.listStatements(
//        		model.createResource("http://example.com/bob"),
//                model.createProperty("http://example.com/hasNose"),
//                (RDFNode)null);
//                
//        RDFNode bnode = stmtIt.nextStatement().getObject();
//        
//        List<Statement> deletes = 
//        	DependentResourceDeleteJena.getDependentResourceDeleteList(bnode,model);        
//        model.remove(deletes);        
//                        
//        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
//        boolean same = expectedModel.isIsomorphicWith( model );
//        if( ! same ) printModels( expectedModel, model);
//        Assert.assertTrue( same );
//    }
    
//    @org.junit.Test
//    public void testResForceDeleteWithNestedBNodes() {
//        String n3 = 
//            prefixesN3 +
//            nosePropIsDependentRel +
//            " ex:bob ex:a \"Bob\".   \n" +
//            " ex:bob ex:hasNose [ \n" +            
//            "    ex:a \"this is a bnode\"; \n" +
//            "    ex:c ex:glasses65 ; \n" +
//            "    ex:c [ " + 
//            "           ex:a \"this is a nested bnode\" ] " +
//            "] . \n" +
//            " ex:glasses65 ex:a \"glasses 65\" ." ;
//        
//        String expected = 
//            prefixesN3 +
//            nosePropIsDependentRel +
//            " ex:bob ex:a \"Bob\".   \n"  +
//            " ex:glasses65 ex:a \"glasses 65\" ." ;
//        
//        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");                                              
//               
//        StmtIterator stmtIt = model.listStatements(
//        		model.createResource("http://example.com/bob"),
//                model.createProperty("http://example.com/hasNose"),
//                (RDFNode)null);                
//        RDFNode bnode = stmtIt.nextStatement().getObject();
//        
//        List<Statement> deletes = 
//        	DependentResourceDeleteJena.getDependentResourceDeleteList(bnode,model);        
//        model.remove(deletes);        
//                        
//        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
//        boolean same = expectedModel.isIsomorphicWith( model );
//        if( ! same ) printModels( expectedModel, model);
//        Assert.assertTrue( same );
//    }

    
    @org.junit.Test
    public void testDeleteForChange() {
        String source = 
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
                        
        String retractions =     
        	"@prefix ex: <http://example.com/> . \n" +                    	        
        	" ex:bob ex:hasNose ex:nose1 . ";        
                
        Model sourceModel = (ModelFactory.createDefaultModel()).read(new StringReader(source), "", "N3");                                              
        Model additionsModel = (ModelFactory.createDefaultModel()); //no additions
        Model retractionsModel = (ModelFactory.createDefaultModel()).read(new StringReader(retractions), "", "N3");
        
        Model deletes = 
        	DependentResourceDeleteJena.getDependentResourceDeleteForChange(additionsModel, retractionsModel, sourceModel);                
        sourceModel.remove(retractionsModel);
        sourceModel.remove(deletes);        
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( sourceModel );
        if( ! same ) printModels( expectedModel, sourceModel);
        Assert.assertTrue( same );
    }  
    
    @org.junit.Test
    public void testDeleteForChangeWithReplace() {
        String source = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:jim ex:a \"Jim\".   \n" +
            " ex:bob ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
        
        String expected = 
            prefixesN3 +
            nosePropIsDependentRel +
            " ex:jim ex:a \"Jim\".   \n" +
            " ex:bob ex:a \"Bob\".   \n" +
            " ex:jim ex:hasNose ex:nose1 .   \n" +            
            " ex:nose1 ex:a \"this is a literal\". \n" +
            " ex:nose1 ex:b \"2343\" . \n" +
        	" ex:nose1 ex:hasHair ex:hair23. \n" +
        	" ex:hair23 ex:hasHairCount \"23\". " ;
            
        String additions =     
        	"@prefix ex: <http://example.com/> . \n" +                    	        
        	" ex:jim ex:hasNose ex:nose1 . ";
        
        String retractions =     
        	"@prefix ex: <http://example.com/> . \n" +                    	        
        	" ex:bob ex:hasNose ex:nose1 . ";        
                
        Model sourceModel = (ModelFactory.createDefaultModel()).read(new StringReader(source), "", "N3");                                              
        Model additionsModel = (ModelFactory.createDefaultModel()).read(new StringReader(additions), "", "N3");
        Model retractionsModel = (ModelFactory.createDefaultModel()).read(new StringReader(retractions), "", "N3");
        
        Model depDeletes = 
        	DependentResourceDeleteJena.getDependentResourceDeleteForChange(additionsModel, retractionsModel, sourceModel);                        
        sourceModel.remove(depDeletes);
        sourceModel.remove(retractionsModel);
        sourceModel.add(additionsModel);
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( sourceModel );
        if( ! same ) printModels( expectedModel, sourceModel);
        Assert.assertTrue( same );
    }

     
    @org.junit.Test
    public void testDeleteWithNonZeroInDegree() {
        /*
          This tests deleting a position context node from the organization side.
          Currently the required behavior is that the position context node not be
          deleted when the object property statement is deleted from the organization side.
        */
        String source = 
            prefixesN3 +            
            " ex:personHasPosition " + isDependentRelation +
            " ex:bob  ex:a \"Bob\".   \n" +
            " ex:orgP ex:a \"orgP\".   \n" +
            " ex:bob  ex:personHasPosition ex:position1 .   \n" +            
            " ex:orgP ex:positionInOrganization ex:position1 .   \n" +
            " ex:position1 ex:a \"This is Position1\". \n" +
            " ex:position1 ex:b \"2343\" . ";         	
        
        String expected = 
        	prefixesN3 +            
            " ex:personHasPosition " + isDependentRelation +
            " ex:bob  ex:a \"Bob\".   \n" +
            " ex:orgP ex:a \"orgP\".   \n" +
            " ex:bob  ex:personHasPosition ex:position1 .   \n" +                        
            " ex:position1 ex:a \"This is Position1\". \n" +
            " ex:position1 ex:hasOrgName \"org xyz\" . \n" +
            " ex:position1 ex:b \"2343\" . ";
        	
//            prefixesN3 +            
//            " ex:bob  ex:a \"Bob\".   \n" +
//            " ex:orgP ex:a \"orgP\".   \n" +
//            " ex:bob  ex:hasPosition ex:position1 .   \n" +            
//            " ex:position1 ex:a \"This is Position1\". \n" +
//            " ex:position1 ex:hasOrgName \"org xyz\" . \n" +
//            " ex:position1 ex:b \"2343\" . ";
            
        String additions =     
        	"@prefix ex: <http://example.com/> . \n" +        
            "@prefix xsd: <" + XSD.getURI() + "> . \n " +
        	" ex:position1 ex:hasOrgName \"org xyz\" . ";
        
        String retractions =     
        	"@prefix ex: <http://example.com/> . \n" +   
            "@prefix xsd: <" + XSD.getURI() + "> . \n " +
        	" ex:orgP ex:positionInOrganization ex:position1 . ";        
                
        Model sourceModel = (ModelFactory.createDefaultModel()).read(new StringReader(source), "", "N3");                                              
        Model additionsModel = (ModelFactory.createDefaultModel()).read(new StringReader(additions), "", "N3");
        Model retractionsModel = (ModelFactory.createDefaultModel()).read(new StringReader(retractions), "", "N3");
        
        Model depDeletes = 
        	DependentResourceDeleteJena.getDependentResourceDeleteForChange(additionsModel, retractionsModel, sourceModel);                        
        sourceModel.remove(depDeletes);
        sourceModel.remove(retractionsModel);
        sourceModel.add(additionsModel);
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( sourceModel );
        if( ! same ) printModels( expectedModel, sourceModel);
        Assert.assertTrue( same );
    }

    @org.junit.Test
    public void testDeleteWithNonZeroInDegree2() {
        /*
          This tests deleting a position context node from the organization side.
          Currently the required behavior is that the position context node not be
          deleted when the object property statement is deleted from the organization side.
        */
        String source = 
            prefixesN3 +            
            " ex:personHasPosition " + isDependentRelation +
            " ex:bob  ex:a \"Bob\".   \n" +
            " ex:orgP ex:a \"orgP\".   \n" +
            " ex:bob  ex:personHasPosition ex:position1 .   \n" +            
            " ex:orgP ex:positionInOrganization ex:position1 .   \n" +
            " ex:position1 ex:a \"This is Position1\". \n" +
            " ex:position1 ex:b \"2343\" . ";         	
        
        String expected = 
        	prefixesN3 +            
            " ex:personHasPosition " + isDependentRelation +
            " ex:bob  ex:a \"Bob\".   \n" +
            " ex:orgP ex:a \"orgP\".   \n" +
            " ex:bob  ex:personHasPosition ex:position1 .   \n" +
            " ex:position1 ex:a \"This is Position1\". \n" +
            " ex:position1 ex:b \"2343\" . ";        	
                   
        String retractions =     
        	"@prefix ex: <http://example.com/> . \n" +   
            "@prefix xsd: <" + XSD.getURI() + "> . \n " +
        	" ex:orgP ex:positionInOrganization ex:position1 . ";        
                
        Model sourceModel = (ModelFactory.createDefaultModel()).read(new StringReader(source), "", "N3");                                              
        Model additionsModel = ModelFactory.createDefaultModel(); //no additions
        Model retractionsModel = (ModelFactory.createDefaultModel()).read(new StringReader(retractions), "", "N3");
        
        Model depDeletes = 
        	DependentResourceDeleteJena.getDependentResourceDeleteForChange(additionsModel, retractionsModel, sourceModel);                        
        sourceModel.remove(depDeletes);
        sourceModel.remove(retractionsModel);
        sourceModel.add(additionsModel);
                        
        Model expectedModel = (ModelFactory.createDefaultModel()).read(new StringReader(expected), "", "N3");
        boolean same = expectedModel.isIsomorphicWith( sourceModel );
        if( ! same ) printModels( expectedModel, sourceModel);
        Assert.assertTrue( same );
    }
}
