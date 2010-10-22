package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Allows for instant incremental materialization of trivial RDFS-style 
 * ABox inferences as statements are added to the knowledge base.
 * 
 * @author bjl23
 *
 */
public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	
	private OntModel fullModel;
	private OntModel abox;
	private Model inferenceModel;

	/**
	 * 
	 * @param fullModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param baseModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
 	 */
	public SimpleReasoner(OntModel fullModel, OntModel abox, Model inferenceModel) {
		this.fullModel = fullModel;
		this.abox = abox;
		this.inferenceModel = inferenceModel;
	}
	
	@Override
	public void addedStatement(Statement stmt) {
		fullModel.enterCriticalSection(Lock.READ);
		try {
			log.debug("addedStatement: " + stmt.toString());
			if(stmt != null && stmt.getPredicate() != null)
			{
				OntProperty prop = fullModel.getOntProperty(stmt.getPredicate().getURI());
				if(prop != null)
				{
					log.debug("addedStatement: ontproperty is not null - get super properties");
					ExtendedIterator<? extends OntProperty> superIt = prop.listSuperProperties(false);
					while (superIt.hasNext()) {
						OntProperty ontProp = superIt.next();
						log.debug("addedStatement: ontProp uri is " + ontProp.getURI());
						Statement infStmt = ResourceFactory.createStatement(
								stmt.getSubject(), ontProp, stmt.getObject());
						inferenceModel.enterCriticalSection(Lock.WRITE);
						try {
							if (!inferenceModel.contains(infStmt)) {
								log.debug("Before adding inference stmt to inference Model " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
								inferenceModel.add(infStmt);
								log.debug("Added inference statement");
							}
						} finally {
							inferenceModel.leaveCriticalSection();
						}
					}
				} else {
					log.debug("addedStatement : Prop is null for " + stmt.getPredicate().getURI());
				}
			}
		} catch (Exception e) {
			// don't stop the edit if an exception is thrown
			log.error("Error adding trivial inferences: ", e);
		} finally {
			fullModel.leaveCriticalSection();
		}
	}
	
	@Override
	public void removedStatement(Statement stmt) {
		// Some of these retractions may get reasserted by the real reasoner
		// if there are more complex ways they are entailed
		log.debug("Removed statement: " + stmt.toString());
		fullModel.enterCriticalSection(Lock.READ);
		try {
			OntProperty prop = fullModel.getOntProperty(stmt.getPredicate().getURI());
			if(prop != null)
			{
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
			fullModel.leaveCriticalSection();
		}
	}
	
}
