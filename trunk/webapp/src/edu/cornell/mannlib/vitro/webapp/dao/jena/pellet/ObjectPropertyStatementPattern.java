/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class ObjectPropertyStatementPattern {

	private Resource subject = null;
	private Property predicate = null;
	private Resource object = null;
	
	public ObjectPropertyStatementPattern(Resource subject, Property predicate, Resource object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public Resource getSubject() {
		return this.subject;
	}
	public Property getPredicate() {
		return this.predicate;
	}
	public Resource getObject() {
		return this.object;
	}
	
	public boolean matches(ObjectPropertyStatementPattern p2) {
		boolean sMatch = false;
		boolean pMatch = false;
		boolean oMatch = false;
		if (this.getSubject() == null || p2.getSubject()==null) {
			sMatch = true; // (this.getSubject() == null && p2.getSubject() == null);
		} else {
			sMatch = (this.getSubject().equals(p2.getSubject()));
		}
		if (this.getPredicate() == null || p2.getPredicate()==null) {
			pMatch = true; // (this.getPredicate() == null && p2.getPredicate() == null);
		} else {
			pMatch = (this.getPredicate().equals(p2.getPredicate()));
		}
		if (this.getObject() == null || p2.getObject()==null) {
			oMatch = true ; // (this.getObject() == null && p2.getObject() == null);
		} else {
			oMatch = (this.getObject().equals(p2.getObject()));
		}
		return (sMatch && pMatch && oMatch);
	}
	
}
