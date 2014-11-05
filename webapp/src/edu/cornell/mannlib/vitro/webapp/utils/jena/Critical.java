/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import static com.hp.hpl.jena.shared.Lock.READ;
import static com.hp.hpl.jena.shared.Lock.WRITE;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.AbstractOntModelDecorator;

/**
 * AutoCloseable helper classes for using models in a try-with-resources block.
 */
public abstract class Critical {
	/**
	 * Use this when you have a bare model, and you want to control a critical
	 * section.
	 * 
	 * <pre>
	 * try (Critical.Section section = Critical.Section.read(model)) {
	 *     ...
	 * }
	 * </pre>
	 */
	public static class Section implements AutoCloseable {
		public static Section read(Model model) {
			return new Section(model, READ);
		}

		public static Section write(Model model) {
			return new Section(model, WRITE);
		}

		private final Model model;

		private Section(Model model, boolean readLockRequested) {
			this.model = model;
			this.model.enterCriticalSection(readLockRequested);
		}

		@Override
		public void close() {
			this.model.leaveCriticalSection();
		}
	}

	/**
	 * Use this when you have a bare OntModelSelector. It returns
	 * LockableOntModels, which can then be locked in a critical section.
	 * 
	 * <pre>
	 * LockingOntModelSelector lockingOms = new LockingOntModelSelector(oms);
	 * </pre>
	 */
	public static class LockingOntModelSelector {
		private final OntModelSelector oms;

		public LockingOntModelSelector(OntModelSelector oms) {
			this.oms = oms;
		}

		public LockableOntModel getDisplayModel() {
			return new LockableOntModel(oms.getDisplayModel());
		}

		public LockableOntModel getTBoxModel() {
			return new LockableOntModel(oms.getTBoxModel());
		}
	}

	/**
	 * Returned by the LockingOntModelSelector, or it can be wrapped around a
	 * bare OntModel.
	 * 
	 * <pre>
	 * try (LockedOntModel m = lockingOms.getDisplayModel.read()) {
	 *    ...
	 * }
	 * </pre>
	 */
	public static class LockableOntModel {
		private final OntModel ontModel;

		public LockableOntModel(OntModel ontModel) {
			this.ontModel = ontModel;
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

	/**
	 * A simple OntModel, except that it can only be created by locking a
	 * LockableOntModel. It is AutoCloseable, but the close method has been
	 * hijacked to simply release the lock, and not to actually close the
	 * wrapped model. 
	 */
	public static class LockedOntModel extends AbstractOntModelDecorator
			implements AutoCloseable {

		private LockedOntModel(OntModel m) {
			super(m);
		}

		/**
		 * Just unlocks the model. Doesn't actually close it, because we may
		 * want to use it again.
		 */
		@Override
		public void close() {
			super.leaveCriticalSection();
		}
	}

}
