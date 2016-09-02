/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection;

import java.util.Objects;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.shared.Lock;

/**
 * Makes it easy to use a Jena OntModel in a try-with-resources block. At the
 * end of the block, the close() method will not close the model, but will
 * merely release the lock.
 * 
 * Returned by the LockableOntModelSelector, or it can be wrapped around a bare
 * OntModel. Cannot be used without locking.
 * 
 * <pre>
 * try (LockedOntModel m = lockableOms.getDisplayModel.read()) {
 *    ...
 * }
 * </pre>
 * 
 * or
 * 
 * <pre>
 * try (LockedOntModel m = new LockableOntModel(ontModel).read()) {
 *    ...
 * }
 * </pre>
 */
public class LockableOntModel {
	private final OntModel ontModel;

	public LockableOntModel(OntModel ontModel) {
		this.ontModel = Objects.requireNonNull(ontModel,
				"ontModel may not be null.");
	}

	public LockedOntModel read() {
		ontModel.enterCriticalSection(Lock.READ);
		return new LockedOntModel(ontModel);
	}

	public LockedOntModel write() {
		ontModel.enterCriticalSection(Lock.WRITE);
		return new LockedOntModel(ontModel);
	}
}
