/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;

/**
 * A minimal implementation of the ObjectPropertyStatementDao
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class ObjectPropertyStatementDaoStub implements
		ObjectPropertyStatementDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private static class CanonicalObjectPropertyStatement implements
			ObjectPropertyStatement {
		final String s;
		final String p;
		final String o;

		CanonicalObjectPropertyStatement(String s, String p, String o) {
			if (s == null) {
				throw new NullPointerException(
						"statment subject may not be null.");
			}
			if (p == null) {
				throw new NullPointerException(
						"statement predicate may not be null.");
			}
			if (o == null) {
				throw new NullPointerException(
						"statement object may not be null.");
			}
			this.s = s;
			this.p = p;
			this.o = o;
		}

		CanonicalObjectPropertyStatement(ObjectPropertyStatement stmt) {
			this(stmt.getSubjectURI(), stmt.getPropertyURI(), stmt
					.getObjectURI());
		}

		@Override
		public String getSubjectURI() {
			return s;
		}

		@Override
		public void setSubjectURI(String subjectURI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getObjectURI() {
			return o;
		}

		@Override
		public void setObjectURI(String objectURI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Individual getSubject() {
			return null;
		}

		@Override
		public void setSubject(Individual subject) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectProperty getProperty() {
			return null;
		}

		@Override
		public void setProperty(ObjectProperty property) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Individual getObject() {
			return null;
		}

		@Override
		public void setObject(Individual object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPropertyURI() {
			return p;
		}

		@Override
		public void setPropertyURI(String URI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public PropertyInstance toPropertyInstance() {
			return null;
		}

		public boolean matches(ObjectPropertyStatement stmt) {
			String otherS = stmt.getSubjectURI();
			String otherP = stmt.getPropertyURI();
			String otherO = stmt.getObjectURI();
			return ((otherS == null) || otherS.equals(s))
					&& ((otherP == null) || otherP.equals(p))
					&& ((otherO == null) || otherO.equals(o));
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (!other.getClass().equals(this.getClass())) {
				return false;
			}
			CanonicalObjectPropertyStatement that = (CanonicalObjectPropertyStatement) other;
			return this.s.equals(that.s) && this.o.equals(that.o)
					&& this.p.equals(that.p);
		}

		@Override
		public int hashCode() {
			return this.s.hashCode() ^ this.o.hashCode() ^ this.p.hashCode();
		}

	}

	Set<CanonicalObjectPropertyStatement> statements = new HashSet<CanonicalObjectPropertyStatement>();

	public void addObjectPropertyStatement(String s, String p, String o) {
		statements.add(new CanonicalObjectPropertyStatement(s, p, o));
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public int insertNewObjectPropertyStatement(ObjectPropertyStatement stmt) {
		if (stmt == null) {
			throw new NullPointerException("objPropertyStmt may not be null.");
		}
		statements.add(new CanonicalObjectPropertyStatement(stmt));
		return 0;
	}

	@Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements(
			ObjectPropertyStatement stmt) {
		List<ObjectPropertyStatement> list = new ArrayList<ObjectPropertyStatement>();
		for (CanonicalObjectPropertyStatement cStmt : statements) {
			if (cStmt.matches(stmt)) {
				list.add(cStmt);
			}
		}
		return list;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void deleteObjectPropertyStatement(
			ObjectPropertyStatement objPropertyStmt) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.deleteObjectPropertyStatement() not implemented.");
	}

	@Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements(
			ObjectProperty objectProperty) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.getObjectPropertyStatements() not implemented.");
	}

	@Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements(
			ObjectProperty objectProperty, int startIndex, int endIndex) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.getObjectPropertyStatements() not implemented.");
	}

	@Override
	public Individual fillExistingObjectPropertyStatements(Individual entity) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.fillExistingObjectPropertyStatements() not implemented.");
	}

	@Override
	public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
			String subjectUri, String propertyUri, String objectKey,
			String domainUri, String rangeUri, String query,
			Set<String> constructQueries, String sortDir) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.getObjectPropertyStatementsForIndividualByProperty() not implemented.");
	}

	@Override
	public Map<String, String> getMostSpecificTypesInClassgroupsForIndividual(
			String subjectUri) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.getMostSpecificTypesInClassgroupsForIndividual() not implemented.");
	}

	@Override
	public void resolveAsFauxPropertyStatement(ObjectPropertyStatement stmt) {
		throw new RuntimeException(
				"ObjectPropertyStatementDaoStub.resolveAsFauxPropertyStatement() not implemented.");
	}

}
