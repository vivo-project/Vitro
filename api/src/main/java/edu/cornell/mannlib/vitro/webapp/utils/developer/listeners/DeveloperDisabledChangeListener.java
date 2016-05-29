/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer.listeners;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * If a particular developer flag is NOT set to true, this is transparent.
 * 
 * Set the flag and this becomes opaque, passing no events through.
 */
public class DeveloperDisabledChangeListener extends StatementListener 
        implements ModelChangedListener {
	private final ModelChangedListener inner;
	private final Key disablingKey;

	public DeveloperDisabledChangeListener(ModelChangedListener inner,
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
	public void addedStatement(Statement stmt) {
		if (isEnabled()) {
			inner.addedStatement(stmt);
		}
	}

	@Override
	public void removedStatement(Statement stmt) {
		if (isEnabled()) {
			inner.removedStatement(stmt);
		}
	}

	@Override
	public void notifyEvent(Model model, Object event) {
		if (isEnabled()) {
			inner.notifyEvent(model, event);
		}
	}

}
