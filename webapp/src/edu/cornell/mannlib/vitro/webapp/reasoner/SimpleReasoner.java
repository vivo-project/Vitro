package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Allows for instant incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the knowledge base.
 *  
 */

public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	
	private OntModel tboxModel;
	private OntModel aboxModel;
	private Model inferenceModel;

	/**
	 * 
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
 	 */
	public SimpleReasoner(OntModel tboxModel, OntModel aboxModel, Model inferenceModel) {
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
	}
	
	@Override
	public void addedStatement(Statement stmt) {

		try {
			log.debug("stmt = " + stmt.toString());
			
			if (stmt.getPredicate().equals(RDF.type)) {
			   materializeTypes(stmt);
			}
			
			//TODO: make a configuration option controlling whether properties are materialized
			//materializeProperties(stmt);
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding incremental inferences: ", e);
		}
	}
	
	@Override
	public void removedStatement(Statement stmt) {
	
		try {
			log.debug("stmt = " + stmt.toString());
			
			if (stmt.getPredicate().equals(RDF.type)) {
			   retractTypes(stmt);
			}
			
			//TODO: make a configuration option controlling whether properties are materialized
			//retractProperties(stmt);
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while retracting inferences: ", e);
		}
	}
	
	public void materializeTypes(Statement stmt) {
				
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntClass cls = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
			
			if (cls != null) {
				ExtendedIterator<OntClass> superIt = cls.listSuperClasses(false);
				while (superIt.hasNext()) {
					OntClass parentClass = superIt.next();
					
					if (parentClass.isAnon()) continue;
					
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (!inferenceModel.contains(infStmt)) {
							log.debug("Adding this inferred statement:  " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
							inferenceModel.add(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}	
				}
			} else {
				//TODO: I could even take out this check and let it go to the exception, if you think it's better
				log.debug("Didn't find target class (the object of the added rdf:type statement) in the TBox: " + ((Resource)stmt.getObject()).getURI());
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	public void materializeProperties(Statement stmt) {

		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI());
			
			if (prop != null) {	
				ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
			
				while (superIt.hasNext()) {
					OntProperty parentProp = superIt.next();
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), parentProp, stmt.getObject());
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (!inferenceModel.contains(infStmt)) {
							log.debug("Adding inferred statement: " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
							inferenceModel.add(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}
				}
				
			} else {
				log.debug("Didn't find predicate of the added statement in the TBox: " + stmt.getPredicate().getURI());
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}
	
	public void retractTypes(Statement stmt) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntClass cls = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
			
			if (cls != null) {
				ExtendedIterator<OntClass> superIt = cls.listSuperClasses(false);
				while (superIt.hasNext()) {
					OntClass parentClass = superIt.next();
					
					if (parentClass.isAnon()) continue;  
					
					if (entailedType(stmt.getSubject(),parentClass)) continue;          // if a type is still entailed without the
					                                                                    // removed statement, then don't remove it
					                                                                    // from the inferences
					
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
						
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (inferenceModel.contains(infStmt)) {
							log.debug("Removing this inferred statement:  " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
							inferenceModel.remove(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}	
				}
			} else {
				log.debug("Didn't find target class (the object of the removed rdf:type statement) in the TBox: " + ((Resource)stmt.getObject()).getURI());
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	public void retractProperties(Statement stmt) {
		
	}

	//TODO:  should I move this out of this method to be just inline code, for performance reasons?
	//TODO:  by the time removedStatement is called, can I assume that that statement will not be
	//       found in the ABox when I query it? (the "contains" below).
	//TODO:  will cls be in the list of child classes returned? If so, I have to check for it.
	public boolean entailedType(Resource subject, OntClass cls) {
		
		// Returns true if it is entailed by class subsumption that
		// subject is of type cls.
		
		aboxModel.enterCriticalSection(Lock.READ);
		
		try {
			ExtendedIterator<OntClass> iter = cls.listSubClasses(false);
			while (iter.hasNext()) {
				
				OntClass childClass = iter.next();
				Statement stmt = ResourceFactory.createStatement(subject, RDF.type, childClass);
				if (aboxModel.contains(stmt)) return true;
			}
			
			return false;
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
}
