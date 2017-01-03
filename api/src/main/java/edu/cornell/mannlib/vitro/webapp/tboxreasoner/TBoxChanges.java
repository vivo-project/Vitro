/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/**
 * Accumulate changes to the TBox as they arrive. Then make them available to
 * the TBox reasoner.
 */
public class TBoxChanges {
	private final List<Statement> addedStatements = Collections
			.synchronizedList(new ArrayList<Statement>());

	private final List<Statement> removedStatements = Collections
			.synchronizedList(new ArrayList<Statement>());

	private final List<String> deletedDataPropertyUris = Collections
			.synchronizedList(new ArrayList<String>());

	private final List<String> deletedObjectPropertyUris = Collections
			.synchronizedList(new ArrayList<String>());

	// ----------------------------------------------------------------------
	// These methods are called when populating the changeSet. They must be
	// thread-safe.
	// ----------------------------------------------------------------------

	public void addStatement(Statement stmt) {
		addedStatements.add(stmt);
	}

	public void removeStatement(Statement stmt) {
		removedStatements.add(stmt);
	}

	public void deleteDataProperty(Statement stmt) {
		Resource subject = stmt.getSubject();
		if (subject.isURIResource()) {
			deletedDataPropertyUris.add(subject.getURI());
		}
	}

	public void deleteObjectProperty(Statement stmt) {
		Resource subject = stmt.getSubject();
		if (subject.isURIResource()) {
			deletedObjectPropertyUris.add(subject.getURI());
		}
	}

	// ----------------------------------------------------------------------
	// These methods are called when processing the changeSet. By that time, it
	// is owned and accessed by a single thread.
	// ----------------------------------------------------------------------

	public boolean isEmpty() {
		return addedStatements.isEmpty() && removedStatements.isEmpty()
				&& deletedDataPropertyUris.isEmpty()
				&& deletedObjectPropertyUris.isEmpty();
	}

	public List<Statement> getAddedStatements() {
		return addedStatements;
	}

	public List<Statement> getRemovedStatements() {
		return removedStatements;
	}

	public List<String> getDeletedDataPropertyUris() {
		return deletedDataPropertyUris;
	}

	public List<String> getDeletedObjectPropertyUris() {
		return deletedObjectPropertyUris;
	}

	@Override
	public String toString() {
		return "TBoxChanges[addedStatements=" + addedStatements
				+ ", removedStatements=" + removedStatements
				+ ", deletedDataPropertyUris=" + deletedDataPropertyUris
				+ ", deletedObjectPropertyUris=" + deletedObjectPropertyUris
				+ "]";
	}

}
