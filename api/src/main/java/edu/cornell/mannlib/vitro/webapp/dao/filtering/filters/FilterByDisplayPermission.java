/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction.SOME_URI;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.*;
import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionRegistry;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectPropertyStatement;

/**
 * Filter the properties depending on what DisplayByRolePermission is on the
 * request. If no request, or no permission, use the Public permission.
 */
public class FilterByDisplayPermission extends VitroFiltersImpl {
	private static final Log log = LogFactory.getLog(FilterByDisplayPermission.class);

	private final String permissionSetUri;

	// Set a default permission set uri
	private static final String defaultPermissionSetUri = "";

	private static String getPermissionSetFromRequest(HttpServletRequest req) {
		if (req == null) {
			throw new NullPointerException("request may not be null.");
		}

		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
		for (String uri : HasPermissionSet.getPermissionSetUris(ids)) {
			return uri;
		}

		return defaultPermissionSetUri;
	}

	/** Get the DisplayByRolePermission from the request, or use Public. */
	public FilterByDisplayPermission(HttpServletRequest req) {
		this(getPermissionSetFromRequest(req));
	}

	/** Use the Public permission. */
	public FilterByDisplayPermission(ServletContext ctx) {
		this(defaultPermissionSetUri);
	}

	/** Use the specified permission. */
	public FilterByDisplayPermission(String uri) {
		if (uri == null) {
			throw new NullPointerException("permission may not be null.");
		}

		this.permissionSetUri = uri;

		setDataPropertyFilter(new DataPropertyFilterByPolicy());
		setObjectPropertyFilter(new ObjectPropertyFilterByPolicy());
		setDataPropertyStatementFilter(new DataPropertyStatementFilterByPolicy());
		setObjectPropertyStatementFilter(new ObjectPropertyStatementFilterByPolicy());
	}

	boolean checkAuthorization(RequestedAction whatToAuth) {
//		boolean decision = permission.isAuthorized(whatToAuth);
		// TODO Add decision check
		boolean decision = false;
		log.debug("decision is " + decision);
		return decision;
	}

	/**
	 * Private Classes
	 */

	private class DataPropertyFilterByPolicy extends UnaryFunctor<DataProperty, Boolean> {
		@Override
		public Boolean fn(DataProperty dp) {
			return checkAuthorization(new DisplayDataProperty(dp));
		}
	}

	private class ObjectPropertyFilterByPolicy extends UnaryFunctor<ObjectProperty, Boolean> {
		@Override
		public Boolean fn(ObjectProperty op) {
			return checkAuthorization(new DisplayObjectProperty(op));
		}
	}

	private class DataPropertyStatementFilterByPolicy extends UnaryFunctor<DataPropertyStatement, Boolean> {
		@Override
		public Boolean fn(DataPropertyStatement dps) {
			return checkAuthorization(new DisplayDataPropertyStatement(dps));
		}
	}

	private class ObjectPropertyStatementFilterByPolicy extends UnaryFunctor<ObjectPropertyStatement, Boolean> {
		@Override
		public Boolean fn(ObjectPropertyStatement ops) {
			String subjectUri = ops.getSubjectURI();
			ObjectProperty predicate = getOrCreateProperty(ops);
			String objectUri = ops.getObjectURI();
			return checkAuthorization(new DisplayObjectPropertyStatement(subjectUri, predicate, objectUri));
		}

		/**
		 * It would be nice if every ObjectPropertyStatement held a real
		 * ObjectProperty. If it doesn't, we do the next best thing, but it
		 * won't recognize any applicable Faux properties.
		 */
		private ObjectProperty getOrCreateProperty(ObjectPropertyStatement ops) {
			if (ops.getProperty() != null) {
				return ops.getProperty();
			}
			if (ops.getPropertyURI() ==  null) {
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
