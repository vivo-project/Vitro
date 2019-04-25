/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction.SOME_URI;
import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Ask the current policies whether we can show these things to the user.
 */
public class HideFromDisplayByPolicyFilter extends VitroFiltersImpl {
	private static final Log log = LogFactory
			.getLog(HideFromDisplayByPolicyFilter.class);

	private final IdentifierBundle idBundle;
	private final PolicyIface policy;

	public HideFromDisplayByPolicyFilter(IdentifierBundle idBundle,
			PolicyIface policy) {
		if (idBundle == null) {
			throw new NullPointerException("idBundle may not be null.");
		}
		if (policy == null) {
			throw new NullPointerException("policy may not be null.");
		}

		this.idBundle = idBundle;
		this.policy = policy;

		setDataPropertyFilter(new DataPropertyFilterByPolicy());
		setObjectPropertyFilter(new ObjectPropertyFilterByPolicy());
		setDataPropertyStatementFilter(new DataPropertyStatementFilterByPolicy());
		setObjectPropertyStatementFilter(new ObjectPropertyStatementFilterByPolicy());
	}

	boolean checkAuthorization(RequestedAction whatToAuth) {
		PolicyDecision decision = policy.isAuthorized(idBundle, whatToAuth);
		log.debug("decision is " + decision);

		if (decision != null) {
			if (decision.getAuthorized() == Authorization.AUTHORIZED) {
				return true;
			}
		}
		return false;
	}

	private class DataPropertyFilterByPolicy extends
			UnaryFunctor<DataProperty, Boolean> {
		@Override
		public Boolean fn(DataProperty dp) {
			return checkAuthorization(new DisplayDataProperty(dp));
		}
	}

	private class ObjectPropertyFilterByPolicy extends
			UnaryFunctor<ObjectProperty, Boolean> {
		@Override
		public Boolean fn(ObjectProperty op) {
			return checkAuthorization(new DisplayObjectProperty(op));
		}
	}

	private class DataPropertyStatementFilterByPolicy extends
			UnaryFunctor<DataPropertyStatement, Boolean> {
		@Override
		public Boolean fn(DataPropertyStatement dps) {
			return checkAuthorization(new DisplayDataPropertyStatement(dps));
		}
	}

	private class ObjectPropertyStatementFilterByPolicy extends
			UnaryFunctor<ObjectPropertyStatement, Boolean> {
		@Override
		public Boolean fn(ObjectPropertyStatement ops) {
			String subjectUri = ops.getSubjectURI();
			ObjectProperty predicate = getOrCreateProperty(ops);
			String objectUri = ops.getObjectURI();
			return checkAuthorization(new DisplayObjectPropertyStatement(
					subjectUri, predicate, objectUri));
		}

		/**
		 * It would be nice if every ObjectPropertyStatement held a real
		 * ObjectProperty. If it doesn't, we do the next best thing, but it
		 * won't recognize any applicaable Faux properties.
		 */
		private ObjectProperty getOrCreateProperty(ObjectPropertyStatement ops) {
			if (ops.getProperty() != null) {
				return ops.getProperty();
			}
			if (ops.getPropertyURI() == null) {
				return null;
			}
			ObjectProperty op = new ObjectProperty();
			op.setURI(ops.getPropertyURI());
			op.setDomainVClassURI(SOME_URI);
			op.setRangeVClassURI(SOME_URI);
			return op;
		}

	}
}
