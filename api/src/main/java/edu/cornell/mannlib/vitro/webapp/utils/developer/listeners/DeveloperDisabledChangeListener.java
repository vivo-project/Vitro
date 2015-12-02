/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer.listeners;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * If a particular developer flag is NOT set to true, this is transparent.
 * 
 * Set the flag and this becomes opaque, passing no events through.
 */
public class DeveloperDisabledChangeListener implements ChangeListener {
	private final ChangeListener inner;
	private final Key disablingKey;

	public DeveloperDisabledChangeListener(ChangeListener inner,
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
	public void addedStatement(String serializedTriple, String graphURI) {
		if (isEnabled()) {
			inner.addedStatement(serializedTriple, graphURI);
		}
	}

	@Override
	public void removedStatement(String serializedTriple, String graphURI) {
		if (isEnabled()) {
			inner.removedStatement(serializedTriple, graphURI);
		}
	}

	@Override
	public void notifyEvent(String graphURI, Object event) {
		if (isEnabled()) {
			inner.notifyEvent(graphURI, event);
		}
	}

}
