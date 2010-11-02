package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Allows for instant incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the (ABox or TBox) knowledge base.
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
			System.out.println("stmt = " + stmt.toString());
			
			if (stmt.getPredicate().equals(RDF.type)) {
			   materializeTypes(stmt);
			}

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
					
					// VIVO doesn't materialize statements that assert anonymous types
					// for individuals. Also, sharing an identical anonymous node is
					// not allowed in owl-dl. picklist population code looks at qualities
					// of classes not individuals.
					if (parentClass.isAnon()) continue;
					
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (!inferenceModel.contains(infStmt)) {
							log.debug("Adding this inferred statement:  " + infStmt.toString() );
							inferenceModel.add(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}	
				}
			} else {
				log.debug("Didn't find target class (the object of the added rdf:type statement) in the TBox: " + ((Resource)stmt.getObject()).getURI());
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
					
					// VIVO doesn't materialize statements that assert anonymous types
					// for individuals. Also, sharing an identical anonymous node is
					// not allowed in owl-dl. picklist population code looks at qualities
					// of classes not individuals.
					if (parentClass.isAnon()) continue;  
					
					if (entailedType(stmt.getSubject(),parentClass)) continue;    // if a type is still entailed without the
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

	// The following two methods aren't called currently; current default behavior of VIVO is to not materialize such inferences.
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
	public void retractProperties(Statement stmt) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI());
			
			if (prop != null) {	
				ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
			
				while (superIt.hasNext()) {
					OntProperty parentProp = superIt.next();
					
					if (entailed(stmt.getSubject(),parentProp,stmt.getObject() )) continue;    // if the statement is still entailed 
					                                                                           // don't remove it from the inference graph.
                                                                                
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), parentProp, stmt.getObject());
					
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (inferenceModel.contains(infStmt)) {
							log.debug("Removing inferred statement: " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
							inferenceModel.remove(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}
				}
				
			} else {
				log.debug("Didn't find predicate of the removed statement in the TBox: " + stmt.getPredicate().getURI());
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}	
	}

	// Returns true if it is entailed by class subsumption that subject is
	// of type cls; otherwise returns false.
	public boolean entailedType(Resource subject, OntClass cls) {
		
		aboxModel.enterCriticalSection(Lock.READ);
		
		try {
			ExtendedIterator<OntClass> iter = cls.listSubClasses(false);
			while (iter.hasNext()) {
				
				OntClass childClass = iter.next();
				
				//TODO: do I need this?
				if (childClass.equals(cls)) continue;
				
				Statement stmt = ResourceFactory.createStatement(subject, RDF.type, childClass);
				if (aboxModel.contains(stmt)) return true;
			}
			
			return false;
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
	
	// Returns true if the statement is entailed by property subsumption 
	public boolean entailed(Resource subject, OntProperty prop, RDFNode object) {
		
		aboxModel.enterCriticalSection(Lock.READ);
		
		try {
			
			ExtendedIterator<? extends OntProperty> iter = prop.listSubProperties(false);
			
			while (iter.hasNext()) {
				
				OntProperty childProp = iter.next();
				
				//TODO: do I need this?
				if (childProp.equals(prop)) continue;
				
				Statement stmt = ResourceFactory.createStatement(subject, childProp, object);
				if (aboxModel.contains(stmt)) return true;
			}
			
			return false;
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
}
