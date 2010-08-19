/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * This class handles deletion of resources based on the annotation vitro:dependentResourceAnnot.
 * 
 * For example, take a graph like:
 * 
 *   ex:bob  ex:hasPositionHistory ex:positionHistory23 .
 *   ex:positionHistory23 ex:hasTitle "position 23 was great" .
 *   ex:hasPositionHistory vitro.dependentResourceAnnot "true"^^xsd:boolean .
 *   
 * When the statement ex:bob ex:hasPositionHistory ex:positioinHisroty23 is
 * deleted, then everything about ex:positionHistory23 should be deleted because  
 * ex:hasPositionHistory is a dependent resource property.  This will be done 
 * recursively. 
 *  
 * @author bdc34
 *
 */
public class DependentResourceDeleteJena {
    
	public static List<Statement> getDependentResourceDeleteList ( Statement stmt, Model model){
		if( model == null ) throw new IllegalArgumentException("model must not be null.");
		return getDependentResourceDeleteList(stmt, model, new HashSet<String>(), false);		
	}
	
	public static List<Statement> getDependentResourceDeleteList(Resource deleteMe, Model sourceModel) {
		List<Statement> deletes = new LinkedList<Statement>();				 
		for( Statement stmt : getAllStatements(deleteMe, sourceModel)){
			deletes.add(stmt);
			deletes.addAll(getDependentResourceDeleteList(stmt, sourceModel, new HashSet<String>(), false));
		}
		return deletes;		
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

	private static List<Statement> getDependentResourceDeleteList ( Statement stmt, Model model, Set<String> visitedUris, boolean perviousWasDependentResource ){                        
        if( stmt == null ) 
            return Collections.emptyList();        
        
        List<Statement> toDelete = new LinkedList<Statement>();
        toDelete.add(stmt);        
        RDFNode obj = stmt.getObject();

        if( ( obj.canAs(Resource.class) && isPredicateDependencyRelation(stmt.getPredicate(), model) )
        	|| ( obj.isAnon() && perviousWasDependentResource ) ){                	
    		Resource res = (Resource)obj.as(Resource.class);
    		String id = res.isAnon()?res.getId().toString():res.getURI();
                                
    		if( !visitedUris.contains(id) ){
    			visitedUris.add(id);
    			for( Statement stubStmt : getAllStatements(res, model)){                        	
    				toDelete.addAll( getDependentResourceDeleteList(stubStmt, model,visitedUris,true));                        	
    			}                         
    		}                        	  
        }
        return toDelete;
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

    private static boolean isPredicateDependencyRelation( Property predicate , Model model){
    	return model.containsLiteral(
    			model.getResource(predicate.getURI()), 
    			model.createProperty(VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT), 
    			true);
    }


    
}
