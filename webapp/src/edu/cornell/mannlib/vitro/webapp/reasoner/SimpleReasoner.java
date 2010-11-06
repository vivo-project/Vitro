package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Allows for instant incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the (ABox or TBox) knowledge base.
 *  
 */

public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	//private static final MyTempLogger log = new MyTempLogger();
	
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
	
	/*
	 * Performs incremental selected ABox reasoning based
	 * on a new type assertion (assertion that an individual
	 * is of a certain type) added to the ABox.
	 * 
	 */
	@Override
	public void addedStatement(Statement stmt) {

		try {
			
			if (stmt.getPredicate().equals(RDF.type)) {
			   addedType(stmt);
			}

		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding incremental inferences: ", e);
		}
	}
	
	/*
	 * Performs incremental selected ABox reasoning based
	 * on a removed type assertion (assertion that an individual
	 * is of a certain type) from the ABox.
	 * 
	 */
	@Override
	public void removedStatement(Statement stmt) {
	
		try {
			
			if (stmt.getPredicate().equals(RDF.type)) {
			   removedType(stmt);
			}

		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while retracting inferences: ", e);
		}
	}
	
	/*
	 * Performs incremental selected ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles subclassOf and equivalentClass assertions
	 * 
	 */
	public void addedTBoxStatement(Statement stmt) {

		try {
			
			if ( !(stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(RDFS.subClassOf) ) ) return;
			  
			log.debug("stmt = " + stmt.toString());
			
			OntClass subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
			OntClass object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
			
			if (stmt.getPredicate().equals(RDFS.subClassOf)) {
			   addedSubClass(subject,object);
			} else {
				// equivalent class is the same as subclass in both directions
			   addedSubClass(subject,object);
			   addedSubClass(object,subject);
			}

		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding incremental inferences: ", e);
		}
	}

	/*
	 * Performs incremental selected ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles subclassOf and equivalentClass assertions
	 * 
	 */
	public void removedTBoxStatement(Statement stmt) {
	
		try {
			
			if ( !(stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(RDFS.subClassOf) ) ) return;
			  
			log.debug("stmt = " + stmt.toString());
			
			OntClass subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
			OntClass object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
			
			if (stmt.getPredicate().equals(RDFS.subClassOf)) {
			   removedSubClass(subject,object);
			} else {
				// equivalent class is the same as subclass in both directions
			   removedSubClass(subject,object);
			   removedSubClass(object,subject);
			}

		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while removing incremental inferences: ", e);
		}
	}
	
	
	/*
	 * If it is added that B is of type A, then for each superclass of
	 * A assert that B is of that type.
	 * 
	 */
	public void addedType(Statement stmt) {

		log.debug("stmt = " + stmt.toString());
		
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

	/*
	 * If it is removed that B is of type A, then for each superclass of A remove
	 * the inferred statement that B is of that type UNLESS it is otherwise entailed
	 * that B is of that type.
	 * 
	 */
	public void removedType(Statement stmt) {
		
		log.debug("stmt = " + stmt.toString());
		
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


	// Returns true if it is entailed by class subsumption that subject is
	// of type cls; otherwise returns false.
	public boolean entailedType(Resource subject, OntClass cls) {
		
		log.debug("subject = " + subject.getURI() + " class = " + cls.getURI());
		
		aboxModel.enterCriticalSection(Lock.READ);
		tboxModel.enterCriticalSection(Lock.READ);
		
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
			tboxModel.leaveCriticalSection();
		}	
	}
	
	/*
	 * If added that B is a subclass of A, then find all individuals
	 * that are typed as B, either in the ABox or in the inferred model
	 * and assert that they are of type A.
	 */
	public void addedSubClass(OntClass subClass, OntClass superClass) {
		
		log.debug("subClass = " + subClass.getURI() + " superClass = " + superClass.getURI());
		
		aboxModel.enterCriticalSection(Lock.READ);
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.add(aboxModel);
			unionModel.add(inferenceModel);
					
			StmtIterator iter = unionModel.listStatements((Resource) null, RDF.type, subClass);
	
			while (iter.hasNext()) {
				
				Statement stmt = iter.next();
				Resource ind = unionModel.getResource(stmt.getSubject().getURI());
				Statement infStmt = ResourceFactory.createStatement(ind, RDF.type, superClass);
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				
				if (!inferenceModel.contains(infStmt)) {
					log.debug("Adding this inferred statement:  " + infStmt.toString() );
					inferenceModel.add(infStmt);
				} 
			}
		} finally {
			aboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	}
	
	/*
	 * If removed that B is a subclass of A, then for each individual
	 * that is of type B, either inferred or in the ABox, then
	 * remove the inferred assertion that it is of type A,
	 * UNLESS the individual is of some type C that is 
	 * a subClass of A (including A itself)
	 * 
	 */
	public void removedSubClass(OntClass subClass, OntClass superClass) {
		
		log.debug("subClass = " + subClass.getURI() + ". superClass = " + superClass.getURI());

		aboxModel.enterCriticalSection(Lock.READ);
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.add(aboxModel);
			unionModel.add(inferenceModel);
					
			StmtIterator iter = unionModel.listStatements((Resource) null, RDF.type, subClass);
	
			while (iter.hasNext()) {
				
				Statement stmt = iter.next();
				Resource ind = unionModel.getResource(stmt.getSubject().getURI());
				
				if (entailedType(ind,superClass)) continue;
				
				Statement infStmt = ResourceFactory.createStatement(ind, RDF.type, superClass);
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				
				if (inferenceModel.contains(infStmt)) {
					log.debug("Removing this inferred statement:  " + infStmt.toString() );
					inferenceModel.remove(infStmt);
				} 
			}
		} finally {
			aboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	}

	// The following three methods aren't currently called; the default behavior of VIVO is to not materialize such inferences.
	public void addedProperty(Statement stmt) {

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
	
	public void removedProperty(Statement stmt) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI());
			
			if (prop != null) {	
				ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
			
				while (superIt.hasNext()) {
					OntProperty parentProp = superIt.next();
					
					if (entailedStmt(stmt.getSubject(),parentProp,stmt.getObject() )) continue;    // if the statement is still entailed 
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
	
	// Returns true if the statement is entailed by property subsumption 
	public boolean entailedStmt(Resource subject, OntProperty prop, RDFNode object) {
		
		aboxModel.enterCriticalSection(Lock.READ);
		
		try {
			
			ExtendedIterator<? extends OntProperty> iter = prop.listSubProperties(false);
			
			while (iter.hasNext()) {
				
				OntProperty childProp = iter.next();
				
				Statement stmt = ResourceFactory.createStatement(subject, childProp, object);
				if (aboxModel.contains(stmt)) return true;
			}
			
			return false;
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
  }