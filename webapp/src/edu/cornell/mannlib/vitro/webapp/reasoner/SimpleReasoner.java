package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Allows for instant incremental materialization (or retraction) of RDFS-
 * style class-and-property-subsumption-based ABox inferences as statements
 * are added to (or removed from) the knowledge base.
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

		log.debug("stmt = " + stmt.toString());
		
		if (stmt == null || stmt.getSubject() == null || stmt.getPredicate() == null || stmt.getObject() == null) { 
		    return;
		}
		
		if (stmt.getPredicate().equals(RDF.type)) {
		   materializeTypes(stmt);
		}
		
		//TODO: should this be left commented out? materializeProperties(stmt);
	}
	
	@Override
	public void removedStatement(Statement stmt) {

		log.debug("Removed statement: " + stmt.toString());
		retractTypes(stmt);
		//TODO: should this be left commented out? retractProperties(stmt);
	}

	public void materializeTypes(Statement stmt) {
		
		// TODO: is it ok to check RDF.type against this property instead of the one by the same
		// name in the TBox?
		// would there be statements with subject or predicate of null? Original code was checking
		// getPredicate for null but not checking getObject for null?
		// What if the class (object) is a restriction?
		
		if (stmt == null || stmt.getSubject() == null || stmt.getPredicate() == null || stmt.getObject() == null ||
			!stmt.getPredicate().equals(RDF.type)) {  
			  return;
		}
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			// Assuming the object is a resource and not a literal. The catch clause below will catch it if not.
			OntClass cls = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI());
			if (cls != null) {
				ExtendedIterator<OntClass> superIt = cls.listSuperClasses(false);
				while (superIt.hasNext()) {
					OntClass parentCls = superIt.next();
					Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentCls);
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
				log.debug("Didn't find Class of the added rdf.TYPE statement in the TBox: " + ((Resource)stmt.getObject()).getURI());
			}
		} catch (Exception e) {
			// don't stop the edit if an exception is thrown
			log.error("Exception wihle adding incremental inferences: ", e);
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	public void materializeProperties(Statement stmt) {

		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			if(stmt != null && stmt.getPredicate() != null) {
				OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI());
				if (prop != null) {
					
					ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
					while (superIt.hasNext()) {
						OntProperty parentProp = superIt.next();
						log.debug("parentProp uri is " + parentProp.getURI());
						Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), parentProp, stmt.getObject());
						inferenceModel.enterCriticalSection(Lock.WRITE);
						try {
							if (!inferenceModel.contains(infStmt)) {
								log.debug("Materializing property statement: " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
								inferenceModel.add(infStmt);
							}
						} finally {
							inferenceModel.leaveCriticalSection();
						}
					}
				} else {
					log.debug("Didn't find predicate of the added statement in the TBox: " + stmt.getPredicate().getURI());
				}
			}
		} catch (Exception e) {
			// don't stop the edit if an exception is thrown
			log.error("Exception while adding incremental inferences: ", e);
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}
	
	public void retractTypes(Statement stmt) {
	
		tboxModel.enterCriticalSection(Lock.READ);
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI());
			if(prop != null) {
				ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
				while (superIt.hasNext()) {
					OntProperty ontProp = superIt.next();
					Statement infStmt = ResourceFactory.createStatement(
							stmt.getSubject(), ontProp, stmt.getObject());
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (inferenceModel.contains(infStmt)) {
							log.debug("RemovedStmt:Inference model contains statement and will no remove");
							inferenceModel.remove(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}
				}
			} else {
				log.debug("Prop for " + stmt.getPredicate().getURI() + " is null so didn't execute removal");
			}
		} catch (Exception e) {
			// don't stop the edit if an exception is thrown
			log.error("Removed stmt: Error removing trivial inferences: ", e);
			log.debug(e.getMessage());
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	public void retractProperties(Statement stmt) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		try {
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI());
			if(prop != null) {
				ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
				while (superIt.hasNext()) {
					OntProperty ontProp = superIt.next();
					Statement infStmt = ResourceFactory.createStatement(
							stmt.getSubject(), ontProp, stmt.getObject());
					inferenceModel.enterCriticalSection(Lock.WRITE);
					try {
						if (inferenceModel.contains(infStmt)) {
							log.debug("RemovedStmt:Inference model contains statement and will no remove");
							inferenceModel.remove(infStmt);
						}
					} finally {
						inferenceModel.leaveCriticalSection();
					}
				}
			} else {
				log.debug("Prop for " + stmt.getPredicate().getURI() + " is null so didn't execute removal");
			}
		} catch (Exception e) {
			// don't stop the edit if an exception is thrown
			log.error("Removed stmt: Error removing trivial inferences: ", e);
			log.debug(e.getMessage());
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

}
