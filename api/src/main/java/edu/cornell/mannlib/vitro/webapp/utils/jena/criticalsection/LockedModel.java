/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.AbstractModelDecorator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * A model that is easy to use in a try-with-resources code block. It can only
 * be created by locking a LockableModel.
 * 
 * <pre>
 * try (LockedModel m = lockableModel.read()) {
 *    ...
 * }
 * </pre>
 * 
 * The close method has been hijacked to simply release the lock, and not to
 * actually close the wrapped model.
 */
public class LockedModel extends AbstractModelDecorator implements
		AutoCloseable {
	/**
	 * Should only be created by LockableModel.
	 */
	LockedModel(Model m) {
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
