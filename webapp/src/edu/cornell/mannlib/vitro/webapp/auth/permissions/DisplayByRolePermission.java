/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Is the user authorized to display properties that are marked as restricted to
 * a certain "Role Level"?
 */
public class DisplayByRolePermission extends Permission {
	private static final Log log = LogFactory
			.getLog(DisplayByRolePermission.class);

	public static final String NAMESPACE = "java:"
			+ DisplayByRolePermission.class.getName() + "#";

	private final String roleName;
	private final RoleLevel roleLevel;
	private final ServletContext ctx;

	public DisplayByRolePermission(String roleName, RoleLevel roleLevel,
			ServletContext ctx) {
		super(NAMESPACE + roleName);

		if (roleName == null) {
			throw new NullPointerException("role may not be null.");
		}
		if (roleLevel == null) {
			throw new NullPointerException("roleLevel may not be null.");
		}
		if (ctx == null) {
			throw new NullPointerException("context may not be null.");
		}

		this.roleName = roleName;
		this.roleLevel = roleLevel;
		this.ctx = ctx;
	}

	@Override
	public boolean isAuthorized(RequestedAction whatToAuth) {
		boolean result;

		if (whatToAuth instanceof DisplayDataProperty) {
			result = isAuthorized((DisplayDataProperty) whatToAuth);
		} else if (whatToAuth instanceof DisplayObjectProperty) {
			result = isAuthorized((DisplayObjectProperty) whatToAuth);
		} else if (whatToAuth instanceof DisplayDataPropertyStatement) {
			result = isAuthorized((DisplayDataPropertyStatement) whatToAuth);
		} else if (whatToAuth instanceof DisplayObjectPropertyStatement) {
			result = isAuthorized((DisplayObjectPropertyStatement) whatToAuth);
		} else {
			result = false;
		}

		if (result) {
			log.debug(this + " authorizes " + whatToAuth);
		} else {
			log.debug(this + " does not authorize " + whatToAuth);
		}

		return result;
	}

	/**
	 * The user may see this data property if they are allowed to see its
	 * predicate.
	 */
	private boolean isAuthorized(DisplayDataProperty action) {
		String predicateUri = action.getDataProperty().getURI();
		return canDisplayPredicate(predicateUri);
	}

	/**
	 * The user may see this object property if they are allowed to see its
	 * predicate.
	 */
	private boolean isAuthorized(DisplayObjectProperty action) {
		String predicateUri = action.getObjectProperty().getURI();
		return canDisplayPredicate(predicateUri);
	}

	/**
	 * The user may see this data property if they are allowed to see its
	 * subject and its predicate.
	 */
	private boolean isAuthorized(DisplayDataPropertyStatement action) {
		DataPropertyStatement stmt = action.getDataPropertyStatement();
		String subjectUri = stmt.getIndividualURI();
		String predicateUri = stmt.getDatapropURI();
		return canDisplayResource(subjectUri)
				&& canDisplayPredicate(predicateUri);
	}

	/**
	 * The user may see this data property if they are allowed to see its
	 * subject, its predicate, and its object.
	 */
	private boolean isAuthorized(DisplayObjectPropertyStatement action) {
		ObjectPropertyStatement stmt = action.getObjectPropertyStatement();
		String subjectUri = stmt.getSubjectURI();
		String predicateUri = stmt.getPropertyURI();
		String rangeUri = (stmt.getProperty() == null) ? null 
		        : stmt.getProperty().getRangeVClassURI();
		String objectUri = stmt.getObjectURI();
		return canDisplayResource(subjectUri)
				&& canDisplayPredicate(predicateUri, rangeUri)
				&& canDisplayResource(objectUri);
	}

	private boolean canDisplayResource(String resourceUri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canDisplayResource(
				resourceUri, this.roleLevel);
	}
	
	private boolean canDisplayPredicate(String predicateUri) {
	    return canDisplayPredicate(predicateUri, null);
	}

	private boolean canDisplayPredicate(String predicateUri, String rangeUri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx)
				.canDisplayPredicate(predicateUri, rangeUri, this.roleLevel);
	}

	@Override
	public String toString() {
		return "DisplayByRolePermission['" + roleName + "']";
	}

}
