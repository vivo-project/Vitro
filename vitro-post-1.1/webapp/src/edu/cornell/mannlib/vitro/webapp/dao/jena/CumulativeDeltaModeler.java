/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This ModelChangedListener will accumulate all statements added to
 * or removed from the audited model in an additions model and a 
 * retractions model.  Note that this class attempts to be clever.
 * If a statement is retracted that is already in the additions model,
 * the statement will be removed from the additions model and not added
 * to the retractions model.
 * in this object.
 * @author bjl23
 *
 */
public class CumulativeDeltaModeler extends StatementListener {

	private Model additionsModel;
	private Model retractionsModel;
	
	public CumulativeDeltaModeler() {
		this.additionsModel = ModelFactory.createDefaultModel();
		this.retractionsModel = ModelFactory.createDefaultModel();
	}
	
	public CumulativeDeltaModeler(Model model) {
		this();
		model.register(this);
	}
	
	public CumulativeDeltaModeler(OntModel ontModel) {
		this();
		ontModel.getBaseModel().register(this);
	}
	
	/**
	 * Return a model containing all statements added to the attached model
	 * @return additionsModel
	 */
	public Model getAdditions() {
		return additionsModel;
	}
	
	/**
	 * Return a model containing all statements retracted from the attached model
	 * @return retractionsModel
	 */
	public Model getRetractions() {
		return retractionsModel;
	}
	
	@Override
	public void addedStatement(Statement s) {
		if (retractionsModel.contains(s)) {
			retractionsModel.remove(s);
		} else {
			additionsModel.add(s);
		}
	}
	
	@Override
	public void removedStatement(Statement s) {
		if (additionsModel.contains(s)) {
			additionsModel.remove(s);
		} else {
			retractionsModel.add(s);
		}
	}
	
}
