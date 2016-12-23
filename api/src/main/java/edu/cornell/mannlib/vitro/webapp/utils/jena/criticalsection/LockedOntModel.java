/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.AbstractOntModelDecorator;

/**
 * A simple OntModel, except that it can only be created by locking a
 * LockableOntModel. It is AutoCloseable, but the close method has been hijacked
 * to simply release the lock, and not to actually close the wrapped model.
 */
public class LockedOntModel extends AbstractOntModelDecorator implements
		AutoCloseable {

	/**
	 * Can only be created by LockableOntModel.
	 */
	LockedOntModel(OntModel m) {
		super(m);
	}

	/**
	 * Just unlocks the model. Doesn't actually close it, because we may want to
	 * use it again.
	 */
	@Override
	public void close() {
		super.leaveCriticalSection();
	}
}
