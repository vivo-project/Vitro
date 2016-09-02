/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection;

import java.util.Objects;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

/**
 * Makes it easy to use a Jena Model in a try-with-resources block. At the end
 * of the block, the close() method will not close the model, but will merely
 * release the lock.
 * 
 * Wraps around a bare Model. Cannot be used without locking.
 * 
 * <pre>
 * try (LockedModel m = new LockableModel(model).read()) {
 *    ...
 * }
 * </pre>
 */
public class LockableModel {
	private final Model model;

	public LockableModel(Model model) {
		this.model = Objects.requireNonNull(model, "model may not be null.");
	}

	public LockedModel read() {
		model.enterCriticalSection(Lock.READ);
		return new LockedModel(model);
	}

	public LockedModel write() {
		model.enterCriticalSection(Lock.WRITE);
		return new LockedModel(model);
	}
}
