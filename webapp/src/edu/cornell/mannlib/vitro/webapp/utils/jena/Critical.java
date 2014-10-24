/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import static com.hp.hpl.jena.shared.Lock.READ;
import static com.hp.hpl.jena.shared.Lock.WRITE;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Use this in a try-with-resources block.
 * 
 * <pre>
 * try (Critical section = Critical.read(model)) {
 * }
 * </pre>
 */
public class Critical implements AutoCloseable {
	public static Critical read(Model model) {
		return new Critical(model, READ);
	}

	public static Critical write(Model model) {
		return new Critical(model, WRITE);
	}
	
	private final Model model;

	private Critical(Model model, boolean readLockRequested) {
		this.model = model;
		this.model.enterCriticalSection(readLockRequested);
	}

	@Override
	public void close() {
		this.model.leaveCriticalSection();
	}

}
