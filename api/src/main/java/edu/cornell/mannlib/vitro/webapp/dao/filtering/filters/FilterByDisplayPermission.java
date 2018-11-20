/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction.SOME_URI;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.EntityDisplayPermission;
import edu.cornell.mannlib.vitro.webapp.beans.*;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionRegistry;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectPropertyStatement;

import java.util.ArrayList;

/**
 * Filter the properties depending on what DisplayByRolePermission is on the
 * request. If no request, or no permission, use the Public permission.
 */
public class FilterByDisplayPermission extends VitroFiltersImpl {
	private static final Log log = LogFactory.getLog(FilterByDisplayPermission.class);

	private final Permission permission;

	private static Permission getDefaultPermission(ServletContext ctx) {
		if (ctx == null) {
			throw new NullPointerException("context may not be null.");
		}

		// Obtain the EntityDisplayPermission that has been registered for the public user
		WebappDaoFactory wadf = ModelAccess.on(ctx).getWebappDaoFactory();
		for (PermissionSet set : wadf.getUserAccountsDao().getAllPermissionSets()) {
			if (set.isForPublic()) {
				for (String permissionUri : set.getPermissionUris()) {
					Permission permission = PermissionRegistry.getRegistry(ctx).getPermission(permissionUri);
					if (permission instanceof EntityDisplayPermission) {
						return permission;
					}
				}
			}
		}

		// Can't find a public user with an EntityDisplayPermission
		return null;
	}

	/** Use the Public permission. */
	public FilterByDisplayPermission(ServletContext ctx) {
		this(getDefaultPermission(ctx));
	}

	/** Use the specified permission. */
	public FilterByDisplayPermission(Permission permission) {
		if (permission == null) {
			throw new NullPointerException("permission may not be null.");
		}

		this.permission = permission;

		setDataPropertyFilter(new DataPropertyFilterByPolicy());
		setObjectPropertyFilter(new ObjectPropertyFilterByPolicy());
		setDataPropertyStatementFilter(new DataPropertyStatementFilterByPolicy());
		setObjectPropertyStatementFilter(new ObjectPropertyStatementFilterByPolicy());
	}

	boolean checkAuthorization(RequestedAction whatToAuth) {
		boolean decision = permission.isAuthorized(new ArrayList<String>(), whatToAuth);
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
