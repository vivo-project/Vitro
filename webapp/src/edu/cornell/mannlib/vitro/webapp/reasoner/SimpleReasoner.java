/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Allows for real-time incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the (ABox or TBox) knowledge base. 
 */

public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	//private static final MyTempLogger log = new MyTempLogger();
	
	private OntModel tboxModel;             // asserted and inferred TBox axioms
	private OntModel aboxModel;             // ABox assertions
	private Model inferenceModel;           // ABox inferences
	private Model inferenceRebuildModel;    // work area for re-computing all ABox inferences
	private Model scratchpadModel;          // work area for re-computing all ABox inferences
	
	private static final String topObjectPropertyURI = "http://www.w3.org/2002/07/owl#topObjectProperty";
	private static final String bottomObjectPropertyURI = "http://www.w3.org/2002/07/owl#bottomObjectProperty";
	private static final String topDataPropertyURI = "http://www.w3.org/2002/07/owl#topDataProperty";
	private static final String bottomDataPropertyURI = "http://www.w3.org/2002/07/owl#bottomDataProperty";
	private static final String mostSpecificTypePropertyURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";
	
	private AnnotationProperty mostSpecificType = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createAnnotationProperty(mostSpecificTypePropertyURI);
	
	/**
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
	 * @param inferenceRebuildModel - output. This the model temporarily used when the whole ABox inference model is rebuilt
	 * @param inferenceScratchpadModel - output. This the model temporarily used when the whole ABox inference model is rebuilt
 	 */
	public SimpleReasoner(OntModel tboxModel, OntModel aboxModel, Model inferenceModel,
			              Model inferenceRebuildModel, Model scratchpadModel) {
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = inferenceRebuildModel;
		this.scratchpadModel = scratchpadModel;
		
	    aboxModel.register(this);
	}
	
	/**
	 * This constructor is used for the unit tests only
	 * 
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
 	 */
	public SimpleReasoner(OntModel tboxModel, OntModel aboxModel, Model inferenceModel) {
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = ModelFactory.createDefaultModel();
		this.scratchpadModel = ModelFactory.createDefaultModel();
	}
	
	/*
	 * Performs selected incremental ABox reasoning based
	 * on the addition of a new statement (aka assertion) 
	 * to the ABox.
	 */
	@Override
	public void addedStatement(Statement stmt) {

		try {
			if (stmt.getPredicate().equals(RDF.type)) {
			    addedABoxTypeAssertion(stmt, inferenceModel);
			    setMostSpecificTypes(stmt.getSubject(), inferenceModel);
			} 
	       /* uncomment this to enable subproperty/equivalent property inferencing. sjm222 5/13/2011	
			else {
			    addedABoxAssertion(stmt,inferenceModel);
			}
	       */
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding inferences: ", e);
		}
	}
	
	/*
	 * Performs selected incremental ABox reasoning based
	 * on the retraction of a statement (aka assertion)
	 * from the ABox.
	 */
	@Override
	public void removedStatement(Statement stmt) {
	
		try {
			if (stmt.getPredicate().equals(RDF.type)) {
			    removedABoxTypeAssertion(stmt, inferenceModel);
			    setMostSpecificTypes(stmt.getSubject(), inferenceModel);
			}
			/* uncomment this to enable subproperty/equivalent property inferencing. sjm222 5/13/2011
			else {
				removedABoxAssertion(stmt, inferenceModel);
			}
			*/
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while retracting inferences: ", e);
		}
	}

	/*
	 * Performs incremental selected ABox reasoning based
	 * on changes to the class or property hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, 
	 * rdfs:subPropertyOf and owl:equivalentProperty assertions
	 */
	public void addedTBoxStatement(Statement stmt) {
       addedTBoxStatement(stmt, inferenceModel);
	}
	
	public void addedTBoxStatement(Statement stmt, Model inferenceModel) {

		try {
			log.debug("added TBox assertion = " + stmt.toString());
			
			if ( stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) ) {
				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
			
				if ( stmt.getObject().isResource() && (stmt.getObject().asResource()).getURI() == null ) {
					log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
					return;
				}

				if ( stmt.getSubject().getURI() == null ) {
					log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
					return;
				}
				
				OntClass subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
				if (subject == null) {
					log.debug("didn't find subject class in the tbox: " + (stmt.getSubject()).getURI());
					return;
				}
				
				OntClass object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
				if (object == null) {
					log.debug("didn't find object class in the tbox: " + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
				if (stmt.getPredicate().equals(RDFS.subClassOf)) {
				   addedSubClass(subject,object,inferenceModel);
				} else {
					// equivalent class is the same as subclass in both directions
				   addedSubClass(subject,object,inferenceModel);
				   addedSubClass(object,subject,inferenceModel);
				}
			} 
			  /* uncomment this to enable sub property/equivalent property inferencing. sjm222 5/13/2011
			  else if (stmt.getPredicate().equals(RDFS.subPropertyOf) || stmt.getPredicate().equals(OWL.equivalentProperty)) {
				OntProperty subject = tboxModel.getOntProperty((stmt.getSubject()).getURI());
				OntProperty object = tboxModel.getOntProperty(((Resource)stmt.getObject()).getURI()); 
				
				if (stmt.getPredicate().equals(RDFS.subPropertyOf)) {
				   addedSubProperty(subject,object,inferenceModel);
				} else {
					// equivalent property is the same as subProperty in both directions
				   addedSubProperty(subject,object,inferenceModel);
				   addedSubProperty(object,subject,inferenceModel);
				}
			   }
			*/				
			
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding inference(s): ", e);
		}
	}

	/*
	 * Performs incremental selected ABox reasoning based
	 * on changes to the class or property hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, 
	 * rdfs:subPropertyOf and owl:equivalentProperty assertions
	 */
	public void removedTBoxStatement(Statement stmt) {
	
		try {
			log.debug("removed TBox assertion = " + stmt.toString());
			
			if ( stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) ) {
				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
				
				if ( stmt.getObject().isResource() && (stmt.getObject().asResource()).getURI() == null ) {
					log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
					return;
				}

				if ( stmt.getSubject().getURI() == null ) {
					log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
					return;
				}
												
				OntClass subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
				if (subject == null) {
					log.debug("didn't find subject class in the tbox: " + (stmt.getSubject()).getURI());
					return;
				}
				
				OntClass object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
				if (object == null) {
					log.debug("didn't find object class in the tbox: " + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
				if (stmt.getPredicate().equals(RDFS.subClassOf)) {
				   removedSubClass(subject,object,inferenceModel);
				} else {
					// equivalent class is the same as subclass in both directions
				   removedSubClass(subject,object,inferenceModel);
				   removedSubClass(object,subject,inferenceModel);
				}
			} 
			/* uncomment this to enable sub property / equivalent property inferencing. sjm222 5/13/2011.
			else if (stmt.getPredicate().equals(RDFS.subPropertyOf) || stmt.getPredicate().equals(OWL.equivalentProperty)) {
				OntProperty subject = tboxModel.getOntProperty((stmt.getSubject()).getURI());
				OntProperty object = tboxModel.getOntProperty(((Resource)stmt.getObject()).getURI()); 
				
				if (stmt.getPredicate().equals(RDFS.subPropertyOf)) {
				   removedSubProperty(subject,object);
				} else {
					// equivalent property is the same as subProperty in both directions
				   removedSubProperty(subject,object);
				   removedSubProperty(object,subject);
				}				
			}
			*/
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while removing inference(s): ", e);
		}
	}

	/*
	 * Performs incremental reasoning based on a new type assertion
	 * added to the ABox (assertion that an individual is of a certain
	 * type).
	 * 
	 * If it is added that B is of type A, then for each superclass of
	 * A assert that B is of that type.
	 * 
	 */
	public void addedABoxTypeAssertion(Statement stmt, Model inferenceModel) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {

			OntClass cls = null;
			
			if ( (stmt.getObject().asResource()).getURI() != null ) {
			    cls = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
			    
				if (cls != null) {
					
					List<OntClass> parents = (cls.listSuperClasses(false)).toList();		
					parents.addAll((cls.listEquivalentClasses()).toList());	
					Iterator<OntClass> parentIt = parents.iterator();
					
					while (parentIt.hasNext()) {
						OntClass parentClass = parentIt.next();
						
						// VIVO doesn't materialize statements that assert anonymous types
						// for individuals. Also, sharing an identical anonymous node is
						// not allowed in owl-dl. picklist population code looks at qualities
						// of classes not individuals.
						if (parentClass.isAnon()) continue;
						
						Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
						inferenceModel.enterCriticalSection(Lock.WRITE);
						try {
							if (!inferenceModel.contains(infStmt) && !infStmt.equals(stmt) ) {
							    //log.debug("Adding this inferred statement:  " + infStmt.toString() );
								inferenceModel.add(infStmt);
							}
						} finally {
							inferenceModel.leaveCriticalSection();
						}	
					}
				} else {
					log.debug("Didn't find target class (the object of the added rdf:type statement) in the TBox: " + ((Resource)stmt.getObject()).getURI());
				}
			} else {
				log.warn("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
				return;
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	/*
	 * Performs incremental property-based reasoning.
	 * 
	 * Materializes inferences based on the rdfs:subPropertyOf relationship. 
	 * If it is added that x propB y and propB is a sub-property of propA
	 * then add x propA y to the inference graph.
	 */
	public void addedABoxAssertion(Statement stmt, Model inferenceModel) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI()); 
			
			if (prop != null) {
				
				// not reasoning on properties in the OWL, RDF or RDFS namespace
				if ((prop.getNameSpace()).equals(OWL.NS) || 
					(prop.getNameSpace()).equals("http://www.w3.org/2000/01/rdf-schema#") ||
					(prop.getNameSpace()).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
					return;
				}
				
                //TODO: have trouble paramterizing the template with ? extends OntProperty
				List superProperties = prop.listSuperProperties(false).toList();
				superProperties.addAll(prop.listEquivalentProperties().toList());
				Iterator<OntProperty> superIt = superProperties.iterator();
				
				while (superIt.hasNext()) {
					OntProperty superProp = superIt.next();
					
					if ( !((prop.isObjectProperty() && superProp.isObjectProperty()) || (prop.isDatatypeProperty() && superProp.isDatatypeProperty())) ) {
						log.warn("sub-property and super-property do not have the same type. No inferencing will be performed. sub-property: " + prop.getURI() + " super-property:" + superProp.getURI());
						continue;
					}
					
					if (superProp.getURI().equals(topObjectPropertyURI) || superProp.getURI().equals(topDataPropertyURI)) {
						continue;
					}
					
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), superProp, stmt.getObject());
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (!inferenceModel.contains(infStmt) && !infStmt.equals(stmt) ) {
							inferenceModel.add(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}	
				}
			} else {
				log.debug("Didn't find target property (the predicate of the added statement) in the TBox: " + stmt.getPredicate().getURI());
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
	public void removedABoxTypeAssertion(Statement stmt, Model inferenceModel) {
		
		//log.debug("stmt = " + stmt.toString());
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		// convert this method to use generic resources - not get ontclass, not cls.listSuperClasses...
		// use model contains if want to log warning about type owl class
		
		try {
			
			OntClass cls = null;
			
			if ( (stmt.getObject().asResource()).getURI() != null ) {
			    cls = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
			    
				if (cls != null) {
					
					List<OntClass> parents = (cls.listSuperClasses(false)).toList();		
					parents.addAll((cls.listEquivalentClasses()).toList());
					Iterator<OntClass> parentIt = parents.iterator();
					
					while (parentIt.hasNext()) {
						OntClass parentClass = parentIt.next();
						
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
								//log.debug("Removing this inferred statement:  " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
								inferenceModel.remove(infStmt);
							}
						} finally {
							inferenceModel.leaveCriticalSection();
						}	
					}
				} else {
					log.debug("Didn't find target class (the object of the removed rdf:type statement) in the TBox: " + ((Resource)stmt.getObject()).getURI());
				}
			} else {
				log.warn("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
			}			
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}
	
	/*
	 * Performs incremental property-based reasoning.
	 * 
	 * Retracts inferences based on the rdfs:subPropertyOf relationship. 
	 * If it is removed that x propB y and propB is a sub-property of propA
	 * then remove x propA y from the inference graph UNLESS it that
	 * statement is otherwise entailed.
	 */
	public void removedABoxAssertion(Statement stmt, Model inferenceModel) {		
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI()); 
			
			if (prop != null) {
				
                //TODO: trouble parameterizing these templates with "? extends OntProperty"
				List superProperties = prop.listSuperProperties(false).toList();
				superProperties.addAll(prop.listEquivalentProperties().toList());
				Iterator<OntProperty> superIt = superProperties.iterator();
				
				while (superIt.hasNext()) {
					OntProperty superProp = superIt.next();
							
					if ( !((prop.isObjectProperty() && superProp.isObjectProperty()) || (prop.isDatatypeProperty() && superProp.isDatatypeProperty())) ) {
						   log.warn("sub-property and super-property do not have the same type. No inferencing will be performed. sub-property: " + prop.getURI() + " super-property:" + superProp.getURI());
						   return;
					}

					// if the statement is still entailed without the removed 
					// statement then don't remove it from the inferences
					if (entailedByPropertySubsumption(stmt.getSubject(), superProp, stmt.getObject())) continue;
					
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), superProp, stmt.getObject());
						
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (inferenceModel.contains(infStmt)) {
							inferenceModel.remove(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}	
				}
			} else {
				log.debug("Didn't find target predicate (the predicate of the removed statement) in the TBox: " + stmt.getPredicate().getURI());
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	// Returns true if it is entailed by class subsumption that
	// subject is of type cls; otherwise returns false.
	public boolean entailedType(Resource subject, OntClass cls) {
		
		//log.debug("subject = " + subject.getURI() + " class = " + cls.getURI());
		
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
	
	// Returns true if the statement is entailed by property subsumption 
	public boolean entailedByPropertySubsumption(Resource subject, OntProperty prop, RDFNode object) {
		
		aboxModel.enterCriticalSection(Lock.READ);
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			
			ExtendedIterator<? extends OntProperty> iter = prop.listSubProperties(false);
			
			while (iter.hasNext()) {
				OntProperty subProp = iter.next();
				Statement stmt = ResourceFactory.createStatement(subject, subProp, object);
				if (aboxModel.contains(stmt)) return true;
			}
			
			return false;
		} finally {
			aboxModel.leaveCriticalSection();
			tboxModel.leaveCriticalSection();
		}	
	}
	
	/*
	 * If it is added that B is a subClass of A, then for each
	 * individual that is typed as B, either in the ABox or in the
	 * inferred model, assert that it is of type A.
	 */
	public void addedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		
		log.debug("subClass = " + subClass.getURI() + " superClass = " + superClass.getURI());
		
		aboxModel.enterCriticalSection(Lock.WRITE);
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.addSubModel(aboxModel);
			unionModel.addSubModel(inferenceModel);
					
			StmtIterator iter = unionModel.listStatements((Resource) null, RDF.type, subClass);
	
			while (iter.hasNext()) {
				
				Statement stmt = iter.next();
				Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, superClass);
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				
				if (!inferenceModel.contains(infStmt)) {
					inferenceModel.add(infStmt);
					setMostSpecificTypes(infStmt.getSubject(), inferenceModel);
				} 
			}
		} finally {
			aboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	}
	
	/*
	 * If removed that B is a subclass of A, then for each individual
	 * that is of type B, either inferred or in the ABox, remove the
	 * assertion that it is of type A from the inferred model,
	 * UNLESS the individual is of some type C that is a subClass 
	 * of A (including A itself)
	 */
	public void removedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		
		log.debug("subClass = " + subClass.getURI() + ". superClass = " + superClass.getURI());

		aboxModel.enterCriticalSection(Lock.WRITE);
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.addSubModel(aboxModel);
			unionModel.addSubModel(inferenceModel);
					
			StmtIterator iter = unionModel.listStatements((Resource) null, RDF.type, subClass);
	
			while (iter.hasNext()) {
				
				Statement stmt = iter.next();
				Resource ind = stmt.getSubject();
				
				if (entailedType(ind,superClass)) continue;
				
				Statement infStmt = ResourceFactory.createStatement(ind, RDF.type, superClass);
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				
				if (inferenceModel.contains(infStmt)) {
					inferenceModel.remove(infStmt);
					setMostSpecificTypes(infStmt.getSubject(), inferenceModel);
				} 
			}
		} finally {
			aboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	}

	/*
	 * If it is added that B is a subProperty of A, then for each assertion
	 * involving predicate B, either in the ABox or in the inferred model
	 * assert the same relationship for predicate A
	 */
	public void addedSubProperty(OntProperty subProp, OntProperty superProp, Model inferenceModel) {
		
		log.debug("subProperty = " + subProp.getURI() + " superProperty = " + subProp.getURI());
		
		if ( !((subProp.isObjectProperty() && superProp.isObjectProperty()) || (subProp.isDatatypeProperty() && superProp.isDatatypeProperty())) ) {
		   log.warn("sub-property and super-property do not have the same type. No inferencing will be performed. sub-property: " + subProp.getURI() + " super-property:" + superProp.getURI());
		   return;
		}
		
		aboxModel.enterCriticalSection(Lock.READ);
		inferenceModel.enterCriticalSection(Lock.WRITE);
				
		try {			
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.addSubModel(aboxModel);
			unionModel.addSubModel(inferenceModel);
					
			StmtIterator iter = unionModel.listStatements((Resource) null, subProp, (RDFNode) null);
	
			while (iter.hasNext()) {
				
				Statement stmt = iter.next();
				Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), superProp, stmt.getObject());
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				
				if (!inferenceModel.contains(infStmt)) {
					inferenceModel.add(infStmt);
				} 
			}
		} finally {
			aboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	}

	/*
	 * If it is removed that B is a subProperty of A, then for each
	 * assertion involving predicate B, either in the ABox or in the
	 * inferred model, remove the same assertion involving predicate
	 * A from the inferred model, UNLESS the assertion is otherwise
	 * entailed by property subsumption.
	 */
	public void removedSubProperty(OntProperty subProp, OntProperty superProp) {
		
		log.debug("subProperty = " + subProp.getURI() + " superProperty = " + subProp.getURI());
		
		if ( !((subProp.isObjectProperty() && superProp.isObjectProperty()) || (subProp.isDatatypeProperty() && superProp.isDatatypeProperty())) ) {
			   log.warn("sub-property and super-property do not have the same type. No inferencing will be performed. sub-property: " + subProp.getURI() + " super-property:" + superProp.getURI());
			   return;
		}
		
		aboxModel.enterCriticalSection(Lock.READ);
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.addSubModel(aboxModel);
			unionModel.addSubModel(inferenceModel);
					
			StmtIterator iter = unionModel.listStatements((Resource) null, subProp, (RDFNode) null);
	
			while (iter.hasNext()) {
				
				Statement stmt = iter.next();
				
				// if the statement is entailed without the removed subPropertyOf 
				// relationship then don't remove it from the inferences
				if (entailedByPropertySubsumption(stmt.getSubject(), superProp, stmt.getObject())) continue;
				
				Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), superProp, stmt.getObject());
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				
				if (inferenceModel.contains(infStmt)) {
					inferenceModel.remove(infStmt);
				} 
			}
		} finally {
			aboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	}
		
	/*
     * Find the most specific types (classes) of an individual and
     * indicate them for the individual with the core:mostSpecificType
     * annotation.
	 */
	public void setMostSpecificTypes(Resource individual, Model inferenceModel) {
	
		inferenceModel.enterCriticalSection(Lock.WRITE);
		aboxModel.enterCriticalSection(Lock.READ);
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.addSubModel(aboxModel);
			unionModel.addSubModel(inferenceModel);
					
			List<OntClass> types = new ArrayList<OntClass>();
						
			StmtIterator stmtIter = unionModel.listStatements(individual, RDF.type, (RDFNode) null);
			
			while (stmtIter.hasNext()) {
				
				Statement stmt = stmtIter.next();
				
				if ( !stmt.getObject().isResource() ) {
					log.warn("The object of this rdf:type assertion is expected to be a resource: " + stmtString(stmt));
					continue;
				}
				
				OntClass ontClass = null;
				
				if ( (stmt.getObject().asResource()).getURI() != null ) {
				    ontClass = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
				} else {
					log.warn("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
					continue;
				}
				 
				if (ontClass == null) {
					log.debug("Didn't find target class (the object of the added rdf:type statement) in the TBox: " + (stmt.getObject().asResource()).getURI());
					continue;
				}
					
				if (ontClass.isAnon()) continue;
				
				types.add(ontClass); 
			}
	
			HashSet<String> typeURIs = new HashSet<String>();
			List<OntClass> types2 = new ArrayList<OntClass>();
			types2.addAll(types);
			
			Iterator<OntClass> typeIter = types.iterator();
			
			while (typeIter.hasNext()) {
			    OntClass type = typeIter.next();
			    			    
			    boolean add = true;
			    Iterator<OntClass> typeIter2 = types2.iterator();
			    while (typeIter2.hasNext()) {
			    	OntClass type2 = typeIter2.next();
			    				    	
			    	if (type.equals(type2)) { 
			    		continue;
			    	}
			    	
			    	if (type.hasSubClass(type2, false) && !type2.hasSubClass(type, false)) {
			    		add = false;
			    		break;
			    	}
			    }	
			    
			    if (add) {
			    	typeURIs.add(type.getURI());
			    	
		            Iterator<OntClass> eIter = type.listEquivalentClasses();
		                
		            while (eIter.hasNext()) {
		                OntClass equivClass = eIter.next();
		                if (equivClass.isAnon()) continue;
		                typeURIs.add(equivClass.getURI());
		            }    
			    }    	
			}
							
			setMostSpecificTypes(individual, typeURIs, inferenceModel);
			
		} finally {
			aboxModel.leaveCriticalSection();
			tboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	
	    return;	
	}
	
	public void setMostSpecificTypes(Resource individual, HashSet<String> typeURIs, Model inferenceModel) {
		
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
		    Model retractions = ModelFactory.createDefaultModel();
			// remove obsolete most-specific-type assertions
			StmtIterator iter = inferenceModel.listStatements(individual, mostSpecificType, (RDFNode) null);
			
			while (iter.hasNext()) {
				Statement stmt = iter.next();

				if ( !stmt.getObject().isResource() ) {
					log.warn("The object of this assertion is expected to be a resource: " + stmtString(stmt));
					continue;
				}
				
				if (!typeURIs.contains(stmt.getObject().asResource().getURI())) {
					retractions.add(stmt);
				}
			}
			
			inferenceModel.remove(retractions);
			
			// add new most-specific-type assertions 
			Iterator<String> typeIter = typeURIs.iterator();
			
			while (typeIter.hasNext()) {
				String typeURI = typeIter.next();
				Resource mstResource = ResourceFactory.createResource(typeURI);
				
				if (!inferenceModel.contains(individual, mostSpecificType, mstResource)) {
					inferenceModel.add(individual, mostSpecificType, mstResource);
				}
			}			
		} finally {
			inferenceModel.leaveCriticalSection();
		}
	
	    return;	
	}
	
	private boolean recomputing = false;
	
	/**
	 * Returns true if the reasoner is in the process of recomputing all
	 * inferences.
	 */
	public boolean isRecomputing() {
	    return recomputing;
	}
	
	/**
	 * Recompute all inferences.
	 */
	public synchronized void recompute() {
	    recomputing = true;
	    try {
	        recomputeABox();
	    } finally {
	        recomputing = false;
	    }
	}

	/*
	 * Recompute the entire ABox inference graph. The new 
	 * inference graph is built up in a separate model and
	 * then reconciled with the inference graph used by the
	 * application. The model reconciliation must be done
	 * without reading the whole inference models into 
	 * memory since we are supporting very large ABox 
	 * inference models.	  
	 */
	public synchronized void recomputeABox() {
		
		// recompute the inferences 
		inferenceRebuildModel.enterCriticalSection(Lock.WRITE);	
		aboxModel.enterCriticalSection(Lock.WRITE);	
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			inferenceRebuildModel.removeAll();
			StmtIterator iter = aboxModel.listStatements((Resource) null, RDF.type, (RDFNode) null);
			
			log.info("Computing class-based ABox inferences");
			while (iter.hasNext()) {				
				Statement stmt = iter.next();
				addedABoxTypeAssertion(stmt, inferenceRebuildModel);
				setMostSpecificTypes(stmt.getSubject(), inferenceRebuildModel);
			}
			
			log.info("Computing property-based ABox inferences");			
			iter = tboxModel.listStatements((Resource) null, RDFS.subPropertyOf, (RDFNode) null);
			int numStmts = 0;
			
			while (iter.hasNext()) {
				Statement stmt = iter.next();

				if (stmt.getSubject().getURI().equals(bottomObjectPropertyURI) || stmt.getSubject().getURI().equals(bottomDataPropertyURI) ||
					(stmt.getObject().isResource() && (stmt.getObject().asResource().getURI().equals(topObjectPropertyURI) || 
							                            stmt.getObject().asResource().getURI().equals(topDataPropertyURI))) ) {
				     continue;
				}

				if ( stmt.getSubject().equals(stmt.getObject()) ) {
				    continue;
				}

				addedTBoxStatement(stmt, inferenceRebuildModel);
				
				numStmts++;
                if ((numStmts % 500) == 0) {
                    log.info("Still computing property-based ABox inferences...");
                }
			}
			
			iter = tboxModel.listStatements((Resource) null, OWL.equivalentProperty, (RDFNode) null);

			while (iter.hasNext()) {
				Statement stmt = iter.next();

				if ( stmt.getSubject().equals(stmt.getObject()) ) {
				    continue;
				}

				addedTBoxStatement(stmt, inferenceRebuildModel);
				
				numStmts++;
                if ((numStmts % 500) == 0) {
                    log.info("Still computing property-based ABox inferences...");
                }
			}
		} catch (Exception e) {
			 log.error("Exception while recomputing ABox inference model", e);
			 inferenceRebuildModel.removeAll(); // don't do this in the finally, it's needed in the case
                                                // where there isn't an exception
			 return;
		} finally {
   			 aboxModel.leaveCriticalSection();
			 tboxModel.leaveCriticalSection();
			 inferenceRebuildModel.leaveCriticalSection();
		}			
		
		// reflect the recomputed inferences into the application inference
		// model.
		inferenceRebuildModel.enterCriticalSection(Lock.WRITE);
		scratchpadModel.enterCriticalSection(Lock.WRITE);
        log.info("Updating ABox inference model");
		// Remove everything from the current inference model that is not
		// in the recomputed inference model		
		try {
			inferenceModel.enterCriticalSection(Lock.READ);
			
			try {
				scratchpadModel.removeAll();
				StmtIterator iter = inferenceModel.listStatements();
				
				while (iter.hasNext()) {				
					Statement stmt = iter.next();
					if (!inferenceRebuildModel.contains(stmt)) {
					   scratchpadModel.add(stmt);
					}
				}
			} catch (Exception e) {
				log.error("Exception while reconciling the current and recomputed ABox inference models", e);
			} finally {
	            inferenceModel.leaveCriticalSection();
			}
			
			inferenceModel.enterCriticalSection(Lock.WRITE);
			try {
				inferenceModel.remove(scratchpadModel);
			} catch (Exception e){
				log.error("Exception while reconciling the current and recomputed ABox inference models", e);
			} finally {
				inferenceModel.leaveCriticalSection();
			}
			
			// Add everything from the recomputed inference model that is not already
			// in the current inference model to the current inference model.
			inferenceModel.enterCriticalSection(Lock.READ);
			
			try {
				scratchpadModel.removeAll();
				StmtIterator iter = inferenceRebuildModel.listStatements();
				
				while (iter.hasNext()) {				
					Statement stmt = iter.next();
					if (!inferenceModel.contains(stmt)) {
					   scratchpadModel.add(stmt);
					}
				}
			} catch (Exception e) {		
				log.error("Exception while reconciling the current and recomputed ABox inference models", e);
			} finally {
				inferenceModel.leaveCriticalSection();			
			}
			
			inferenceModel.enterCriticalSection(Lock.WRITE);
			try {
				inferenceModel.add(scratchpadModel);
			} catch (Exception e){
				log.error("Exception while reconciling the current and recomputed ABox inference models", e);
			} finally {
				inferenceModel.leaveCriticalSection();
			}
		} finally {
			inferenceRebuildModel.removeAll();
			scratchpadModel.removeAll();
			inferenceRebuildModel.leaveCriticalSection();
			scratchpadModel.leaveCriticalSection();			
		}
		
		log.info("ABox inference model updated");
	}
				
	public static SimpleReasoner getSimpleReasonerFromServletContext(ServletContext ctx) {
	    Object simpleReasoner = ctx.getAttribute("simpleReasoner");
	    
	    if (simpleReasoner instanceof SimpleReasoner) {
	        return (SimpleReasoner) simpleReasoner;
	    } else {
	        return null;
	    }
	}

	public static boolean isABoxReasoningAsynchronous(ServletContext ctx) {
	   return (getSimpleReasonerFromServletContext(ctx) == null);	
	}	
	
    public static String stmtString(Statement statement) {
    	return  " [subject = " + statement.getSubject().getURI() +
    			"] [property = " + statement.getPredicate().getURI() +
                "] [object = " + (statement.getObject().isLiteral() ? ((Literal)statement.getObject()).getLexicalForm() + " (Literal)"
                		                                          : ((Resource)statement.getObject()).getURI() + " (Resource)") + "]";	
    }    
}