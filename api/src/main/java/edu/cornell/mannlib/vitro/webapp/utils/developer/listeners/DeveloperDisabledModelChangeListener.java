/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer.listeners;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * If a particular developer flag is NOT set to true, this is transparent.
 * 
 * Set the flag and this becomes opaque, passing no events through.
 */
public class DeveloperDisabledModelChangeListener implements
		ModelChangedListener {
	private final ModelChangedListener inner;
	private final Key disablingKey;

	public DeveloperDisabledModelChangeListener(ModelChangedListener inner,
			Key disablingKey) {
		this.inner = inner;
		this.disablingKey = disablingKey;
	}

	private boolean isEnabled() {
		return !DeveloperSettings.getInstance().getBoolean(disablingKey);
	}

	// ----------------------------------------------------------------------
	// Delegated methods.
	// ----------------------------------------------------------------------

	@Override
	public void addedStatement(Statement s) {
		if (isEnabled()) {
			inner.addedStatement(s);
		}
	}

	@Override
	public void addedStatements(Statement[] statements) {
		if (isEnabled()) {
			inner.addedStatements(statements);
		}
	}

	@Override
	public void addedStatements(List<Statement> statements) {
		if (isEnabled()) {
			inner.addedStatements(statements);
		}
	}

	@Override
	public void addedStatements(StmtIterator statements) {
		if (isEnabled()) {
			inner.addedStatements(statements);
		}
	}

	@Override
	public void addedStatements(Model m) {
		if (isEnabled()) {
			inner.addedStatements(m);
		}
	}

	@Override
	public void removedStatement(Statement s) {
		if (isEnabled()) {
			inner.removedStatement(s);
		}
	}

	@Override
	public void removedStatements(Statement[] statements) {
		if (isEnabled()) {
			inner.removedStatements(statements);
		}
	}

	@Override
	public void removedStatements(List<Statement> statements) {
		if (isEnabled()) {
			inner.removedStatements(statements);
		}
	}

	@Override
	public void removedStatements(StmtIterator statements) {
		if (isEnabled()) {
			inner.removedStatements(statements);
		}
	}

	@Override
	public void removedStatements(Model m) {
		if (isEnabled()) {
			inner.removedStatements(m);
		}
	}

	@Override
	public void notifyEvent(Model m, Object event) {
		if (isEnabled()) {
			inner.notifyEvent(m, event);
		}
	}

}
