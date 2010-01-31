/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class DependentResourceDeleteJena {
    
	public static List<Statement> getDependentResourceDeleteList ( Statement stmt, Model model){
		if( model == null ) throw new IllegalArgumentException("model must not be null.");
		return getDependentResourceDeleteList(stmt, model, new HashSet<String>());		
	}
	
	public static List<Statement> getDependentResourceDeleteList (RDFNode node, Model model){
		if( model == null ) throw new IllegalArgumentException("model must not be null.");
		return getDependentResourceDeleteList(node, model, new HashSet<String>());
	}
	
	public static Model getDependentResourceDeleteForChange( Model assertions, Model retractions, Model sourceModel){
		if( sourceModel == null || retractions == null || assertions == null)
			 throw new IllegalArgumentException("model must not be null.");
				
		List<Statement> removedStmts = getRemovedStmts( assertions, retractions);

		/* Get ride of any statements that seem to be a change */
		List<Statement>changedStmts = getChangedStmts( assertions, retractions);
		ListIterator <Statement>removed = removedStmts.listIterator();
		while( removed.hasNext()){
			Statement removedStmt = removed.next();
			ListIterator <Statement>changed = changedStmts.listIterator();			
			while( changed.hasNext()){
				Statement changedStmt = changed.next();
				if( removedStmt.equals(changedStmt) ){
					removed.remove();					
				}
			}
		}
		
		List<Statement> dependentStmts = new LinkedList<Statement>();
		for(Statement stmt: removedStmts){
			dependentStmts.addAll( getDependentResourceDeleteList(stmt, sourceModel));
		}		
		Model outModel = ModelFactory.createDefaultModel();
		outModel.add(dependentStmts);
		return outModel;
	}

	/**
	 * Find all statements where for a given statement in the assertions, 
	 * there is at least one statement in the retractions that has 
	 * the same predicate and object. */	 
	@SuppressWarnings("unchecked")
	private static List<Statement> getChangedStmts(Model assertions, Model retractions){
		List<Statement> changedStmts = new LinkedList<Statement>();		
		StmtIterator it = assertions.listStatements();
		while(it.hasNext()){
			Statement assertionStmtStatement = it.nextStatement();
			if( assertionStmtStatement.getObject().canAs( Resource.class )){
				Resource asserObj = (Resource) assertionStmtStatement.getObject().as(Resource.class);			
				StmtIterator retractionStmts = 
					retractions.listStatements(
							(Resource)null, 
							retractions.createProperty(assertionStmtStatement.getPredicate().getURI()),
							retractions.createResource(asserObj.getURI()));
				if( retractionStmts != null ){						
					if( retractionStmts.hasNext())
						changedStmts.add( assertionStmtStatement);				
					while(retractionStmts.hasNext()){
						changedStmts.add( retractionStmts.nextStatement() );
					}
				}
				retractionStmts.close();
			}
			it.close();
		}		
		return changedStmts;
	}
	
    private static List<Statement> getRemovedStmts(Model assertions, Model retractions) {
    	List<Statement> toRemove = new LinkedList<Statement>();
    	StmtIterator iter = retractions.listStatements();
    	while(iter.hasNext()){
    		Statement stmt = iter.nextStatement();
    		if( stmt.getObject() != null && ! stmt.getObject().isLiteral() && ! assertions.contains(stmt))
    			toRemove.add( stmt );    				
    	}
    	iter.close();
		return toRemove;
	}

	private static List<Statement> getDependentResourceDeleteList ( Statement stmt, Model model, Set<String> visitedUris){                        
        if( stmt == null ) 
            return Collections.emptyList();        
        
        List<Statement> toDelete = new LinkedList<Statement>();
        RDFNode obj = stmt.getObject();
        if( obj.isLiteral() ){
            toDelete.add(stmt);                        
        }else if( obj.isResource() ){        	
            Resource res = (Resource)obj.as(Resource.class);
            String id = res.isAnon()?res.getId().toString():res.getURI();
            toDelete.add(stmt);                            
            if(!visitedUris.contains(id) && isStubResource(res, model)  ){
            	visitedUris.add(id);
            	for( Statement stubStmt : getAllStatements(res, model)){                        	
            		toDelete.addAll( getDependentResourceDeleteList(stubStmt, model,visitedUris));                        	
            	}                         
            }                
        }    
        return toDelete;
    }

    private static List<Statement> getDependentResourceDeleteList (RDFNode node, Model model, Set<String> visitedUris){
        if( node == null ) 
            return Collections.emptyList();        
        
        List<Statement> toDelete = new LinkedList<Statement>();
        
        if( node.isLiteral() ){
        	//Literals are ignored
        }else if( node.isResource() ){        	
            Resource res = (Resource)node.as(Resource.class);
            String id = res.isAnon()?res.getId().toString():res.getURI();                            
            if(!visitedUris.contains(id) && isStubResource(res, model)  ){
            	visitedUris.add(id);
            	for( Statement stubStmt : getAllStatements(res, model)){                        	
            		toDelete.addAll( getDependentResourceDeleteList(stubStmt, model,visitedUris) );                        	
            	}                         
            }                
        }    
        return toDelete;    	
    }
    
    private static boolean isStubResource(Resource res, Model model){
    	//boolean q = res.hasProperty(RDF.type, model.createProperty(VitroVocabulary.DEPENDENT_RESOURCE));
    	boolean q = model.contains(res, RDF.type,model.createProperty(VitroVocabulary.DEPENDENT_RESOURCE));
    	return q;                       
    }
    
    private static List<Statement> getAllStatements(Resource res, Model model){
    	List<Statement> deleteUs = new LinkedList<Statement>();
    	StmtIterator it = model.listStatements(null, null, res);
    	while( it.hasNext()){
    		deleteUs.add( it.nextStatement() );
    	}
    	
    	it = model.listStatements(res, null,(RDFNode) null);
    	while(it.hasNext()){
    		deleteUs.add( it.nextStatement());
    	}
    	
    	it = model.listStatements(res, null, null, null);
    	while(it.hasNext()){
    		deleteUs.add( it.nextStatement());
    	}
    
    	return deleteUs;
    }

    
}
