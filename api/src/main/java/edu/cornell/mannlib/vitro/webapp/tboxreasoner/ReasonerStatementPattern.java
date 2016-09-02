/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.Lock;

/**
 * For now, this only models Object Properties.
 * 
 * It should be easy to add Data Property patterns by making this abstract and
 * creating two concrete subclasses.
 */
public class ReasonerStatementPattern {
	public static final ReasonerStatementPattern ANY_OBJECT_PROPERTY = new ReasonerStatementPattern(
			null, null, null);

	public static ReasonerStatementPattern objectPattern(Property predicate) {
		return new ReasonerStatementPattern(null, predicate, null);
	}

	public static ReasonerStatementPattern objectPattern(Statement stmt) {
		if (!stmt.getObject().isResource()) {
			throw new IllegalArgumentException(
					"Object of stmt must be a resource.");
		}
		return new ReasonerStatementPattern(stmt.getSubject(),
				stmt.getPredicate(), stmt.getObject().asResource());
	}

	/**
	 * Any or all of these may be null, which acts as a wild card.
	 */
	private final Resource subject;
	private final Property predicate;
	private final Resource object;
	private final String toString;

	private ReasonerStatementPattern(Resource subject, Property predicate,
			Resource object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.toString = buildToString();
	}

	public Property getPredicate() {
		return predicate;
	}

	/**
	 * All fields must match, either by being equal, or by being a wild card.
	 */
	public boolean matches(ReasonerStatementPattern that) {
		boolean sMatch = this.subject == null || that.subject == null
				|| this.subject.equals(that.subject);
		boolean pMatch = this.predicate == null || that.predicate == null
				|| this.predicate.equals(that.predicate);
		boolean oMatch = this.object == null || that.object == null
				|| this.object.equals(that.object);
		return sMatch && pMatch && oMatch;
	}

	/**
	 * Get a list of statements from this model that match this pattern.
	 */
	public List<Statement> matchStatementsFromModel(Model m) {
		m.enterCriticalSection(Lock.READ);
		try {
			return m.listStatements(subject, predicate, object).toList();
		} finally {
			m.leaveCriticalSection();
		}
	}

	public String buildToString() {
		return "ReasonerStatementPattern[subject="
				+ (subject == null ? "*" : subject.toString()) + ", predicate="
				+ (predicate == null ? "*" : predicate.toString())
				+ ", object=" + (object == null ? "*" : object.toString())
				+ "]";
	}

	@Override
	public String toString() {
		return toString;
	}

}
