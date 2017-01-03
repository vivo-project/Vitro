/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.CloseEvent;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Creates an in-memory model, with a listener that keeps the external model in
 * sync with it.
 */
public class MemoryMappedModel extends AbstractModelDecorator {
	private static final Log log = LogFactory.getLog(MemoryMappedModel.class);

	private final String toString;

	public MemoryMappedModel(Model externalModel, String name) {
		super(VitroModelFactory.createModel());
		super.add(externalModel);
		super.register(new ModelSynchronizer(externalModel, name));

		this.toString = "MemoryMappedModel[wrapping "
				+ ToString.modelToString(externalModel) + "]";
	}

	@Override
	public String toString() {
		return this.toString;
	}

	// ----------------------------------------------------------------------
	// The listener
	// ----------------------------------------------------------------------

	/**
	 * Simple change listener to keep a model (the 'synchronizee') in synch with
	 * the model with which it is registered.
	 */
	public class ModelSynchronizer implements ModelChangedListener {
		private Model m;
		private String hash;

		public ModelSynchronizer(Model synchronizee, String name) {
			this.m = synchronizee;
			this.hash = Integer.toHexString(this.hashCode());
			log.debug(String.format("create: %s, wraps %s(%s) as %s", hash,
					this.m.getClass().getName(),
					Integer.toHexString(this.m.hashCode()), name));
		}

		@Override
		public void addedStatement(Statement s) {
			log.debug(hash + " addedStatement" + s);
			m.add(s);
		}

		@Override
		public void addedStatements(Statement[] statements) {
			log.debug(hash + " addedStatements: " + statements.length);
			m.add(statements);
		}

		@Override
		public void addedStatements(List<Statement> statements) {
			log.debug(hash + " addedStatements: " + statements.size());
			m.add(statements);
		}

		@Override
		public void addedStatements(StmtIterator statements) {
			if (log.isDebugEnabled()) {
				Set<Statement> set = statements.toSet();
				log.debug(hash + " addedStatements: " + set.size());
				m.add(new StmtIteratorImpl(set.iterator()));
			} else {
				m.add(new StmtIteratorImpl(statements));
			}
		}

		@Override
		public void addedStatements(Model model) {
			log.debug(hash + " addedStatements: " + model.size());
			m.add(model);
		}

		@Override
		public void notifyEvent(Model model, Object event) {
			if (event instanceof CloseEvent) {
				m.close();
			}
		}

		@Override
		public void removedStatement(Statement s) {
			log.debug(hash + " removedStatement" + s);
			m.remove(s);
		}

		@Override
		public void removedStatements(Statement[] statements) {
			log.debug(hash + " removedStatements: " + statements.length);
			m.remove(statements);
		}

		@Override
		public void removedStatements(List<Statement> statements) {
			log.debug(hash + " removedStatements: " + statements.size());
			m.remove(statements);
		}

		@Override
		public void removedStatements(StmtIterator statements) {
			if (log.isDebugEnabled()) {
				Set<Statement> set = statements.toSet();
				log.debug(hash + " removedStatements: " + set.size());
				m.remove(new StmtIteratorImpl(set.iterator()));
			} else {
				m.remove(new StmtIteratorImpl(statements));
			}
		}

		@Override
		public void removedStatements(Model model) {
			log.debug(hash + " removedStatements: " + model.size());
			m.remove(model);
		}

	}

}
