/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionBean;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * Is the user authorized to publish properties that are marked as restricted to
 * a certain "Role Level"?
 */
public class PublishByRolePermission extends Permission {
	private static final Log log = LogFactory
			.getLog(PublishByRolePermission.class);

	public static final String NAMESPACE = "java:"
			+ PublishByRolePermission.class.getName() + "#";

	private final String roleName;
	private final RoleLevel roleLevel;

	public PublishByRolePermission(String roleName, RoleLevel roleLevel) {
		super(NAMESPACE + roleName);

		if (roleName == null) {
			throw new NullPointerException("role may not be null.");
		}
		if (roleLevel == null) {
			throw new NullPointerException("roleLevel may not be null.");
		}

		this.roleName = roleName;
		this.roleLevel = roleLevel;
	}

	@Override
	public boolean isAuthorized(RequestedAction whatToAuth) {
		boolean result;

		if (whatToAuth instanceof PublishDataProperty) {
			result = isAuthorized((PublishDataProperty) whatToAuth);
		} else if (whatToAuth instanceof PublishObjectProperty) {
			result = isAuthorized((PublishObjectProperty) whatToAuth);
		} else if (whatToAuth instanceof PublishDataPropertyStatement) {
			result = isAuthorized((PublishDataPropertyStatement) whatToAuth);
		} else if (whatToAuth instanceof PublishObjectPropertyStatement) {
			result = isAuthorized((PublishObjectPropertyStatement) whatToAuth);
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
	 * The user may publish this data property if they are allowed to publish
	 * its predicate.
	 */
	private boolean isAuthorized(PublishDataProperty action) {
		String predicateUri = action.getDataProperty().getURI();
		return canPublishPredicate(new Property(predicateUri));
	}

	/**
	 * The user may publish this object property if they are allowed to publish
	 * its predicate.
	 */
	private boolean isAuthorized(PublishObjectProperty action) {
		return canPublishPredicate(action.getObjectProperty());
	}

	/**
	 * The user may publish this data property if they are allowed to publish
	 * its subject and its predicate.
	 */
	private boolean isAuthorized(PublishDataPropertyStatement action) {
		String subjectUri = action.getSubjectUri();
		String predicateUri = action.getPredicateUri();
		return canPublishResource(subjectUri)
				&& canPublishPredicate(new Property(predicateUri));
	}

	/**
	 * The user may publish this data property if they are allowed to publish
	 * its subject, its predicate, and its object.
	 */
	private boolean isAuthorized(PublishObjectPropertyStatement action) {
		String subjectUri = action.getSubjectUri();
		Property predicate = action.getPredicate();
		String objectUri = action.getObjectUri();
		return canPublishResource(subjectUri) && canPublishPredicate(predicate)
				&& canPublishResource(objectUri);
	}

	private boolean canPublishResource(String resourceUri) {
		return PropertyRestrictionBean.getBean().canPublishResource(
				resourceUri, this.roleLevel);
	}

	private boolean canPublishPredicate(Property predicate) {
		return PropertyRestrictionBean.getBean().canPublishPredicate(predicate,
				this.roleLevel);
	}

	@Override
	public String toString() {
		return "PublishByRolePermission['" + roleName + "']";
	}

}
