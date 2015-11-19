/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection;

import java.util.Objects;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

/**
 * Makes it easy to use a Jena OntModel with a try-with-resources block. If you
 * have access to an OntModelSelector, you can wrap it and then use it obtain
 * LockableOntModels.
 * 
 * <pre>
 * LockableOntModelSelector lockableOms = new LockableOntModelSelector(oms);
 *  
 * try (LockedOntModel m = lockableOms.getDisplayModel.read()) {
 *    ...
 * }
 * </pre>
 */
public class LockableOntModelSelector {
	private final OntModelSelector oms;

	public LockableOntModelSelector(OntModelSelector oms) {
		this.oms = Objects.requireNonNull(oms, "oms may not be null.");
	}

	public LockableOntModel getFullModel() {
		return new LockableOntModel(oms.getFullModel());
	}

	public LockableOntModel getABoxModel() {
		return new LockableOntModel(oms.getABoxModel());
	}

	public LockableOntModel getTBoxModel() {
		return new LockableOntModel(oms.getTBoxModel());
	}

	public LockableOntModel getApplicationMetadataModel() {
		return new LockableOntModel(oms.getApplicationMetadataModel());
	}

	public LockableOntModel getUserAccountsModel() {
		return new LockableOntModel(oms.getUserAccountsModel());
	}

	public LockableOntModel getDisplayModel() {
		return new LockableOntModel(oms.getDisplayModel());
	}

}
