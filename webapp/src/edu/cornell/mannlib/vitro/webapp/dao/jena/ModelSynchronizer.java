/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.CloseEvent;

/**
 * Simple change listener to keep a model (the 'synchronizee') in synch with the model with which it is registered.
 * @author bjl23
 *
 */
public class ModelSynchronizer implements ModelChangedListener {

	private Model m;
	
	public ModelSynchronizer (Model synchronizee) {
		this.m = synchronizee;
	}
		
	public void addedStatement(Statement arg0) {
		m.add(arg0);
	}

	public void addedStatements(Statement[] arg0) {
		m.add(arg0);
	}

	
	public void addedStatements(List arg0) {
		m.add(arg0);
	}

	
	public void addedStatements(StmtIterator arg0) {
		m.add(arg0);
	}

	
	public void addedStatements(Model arg0) {
		m.add(arg0);
	}

	
	public void notifyEvent(Model arg0, Object arg1) {
		if ( arg1 instanceof CloseEvent ) {
			m.close();
		}
	}

	
	public void removedStatement(Statement arg0) {
		m.remove(arg0);
	}

	
	public void removedStatements(Statement[] arg0) {
		m.remove(arg0);
	}
	
	public void removedStatements(List arg0) {
		m.remove(arg0);
	}

	
	public void removedStatements(StmtIterator arg0) {
		m.remove(arg0);
	}

	
	public void removedStatements(Model arg0) {
		m.remove(arg0);
	}
	
}
