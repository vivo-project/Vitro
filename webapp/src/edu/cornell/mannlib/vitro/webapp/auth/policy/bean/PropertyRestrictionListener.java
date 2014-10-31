/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

/**
 * Add this ChangeListener to your EditProcessObject when modifying the
 * ontology, and we will refresh the PropertyRestrictionPolicyHelper bean as
 * appropriate.
 */
public class PropertyRestrictionListener implements ChangeListener {
	private static final Log log = LogFactory
			.getLog(PropertyRestrictionListener.class);

	private final ServletContext ctx;

	public PropertyRestrictionListener(ServletContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * If the deleted property had a non-null restriction, rebuild the bean.
	 */
	@Override
	public void doDeleted(Object oldObj, EditProcessObject epo) {
		if (oldObj instanceof RoleRestrictedProperty) {
			RoleRestrictedProperty p = (RoleRestrictedProperty) oldObj;
			if (anyRoleChanged(p.getHiddenFromDisplayBelowRoleLevel(),
					p.getProhibitedFromUpdateBelowRoleLevel(),
					p.getHiddenFromPublishBelowRoleLevel(), null, null, null)) {
				log.debug("rebuilding the PropertyRestrictionPolicyHelper after deletion");
				createAndSetBean();
			}
		} else {
			log.warn("Not an instance of RoleRestrictedProperty: " + oldObj);
		}
	}

	/**
	 * If the inserted property has a non-null restriction, rebuild the bean.
	 */
	@Override
	public void doInserted(Object newObj, EditProcessObject epo) {
		if (newObj instanceof RoleRestrictedProperty) {
			RoleRestrictedProperty p = (RoleRestrictedProperty) newObj;
			if (anyRoleChanged(null, null, null,
					p.getHiddenFromDisplayBelowRoleLevel(),
					p.getProhibitedFromUpdateBelowRoleLevel(),
					p.getHiddenFromPublishBelowRoleLevel())) {
				log.debug("rebuilding the PropertyRestrictionPolicyHelper after insertion");
				createAndSetBean();
			}
		} else {
			log.warn("Not an instance of RoleRestrictedProperty: " + newObj);
		}
	}

	/**
	 * If the updated property has changed its restrictions, rebuild the bean.
	 */
	@Override
	public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
		if (oldObj instanceof RoleRestrictedProperty
				&& newObj instanceof RoleRestrictedProperty) {
			RoleRestrictedProperty oldP = (RoleRestrictedProperty) oldObj;
			RoleRestrictedProperty newP = (RoleRestrictedProperty) newObj;
			if (anyRoleChanged(oldP.getHiddenFromDisplayBelowRoleLevel(),
					oldP.getProhibitedFromUpdateBelowRoleLevel(),
					oldP.getHiddenFromPublishBelowRoleLevel(),
					newP.getHiddenFromDisplayBelowRoleLevel(),
					newP.getProhibitedFromUpdateBelowRoleLevel(),
					newP.getHiddenFromPublishBelowRoleLevel())) {
				log.debug("rebuilding the PropertyRestrictionPolicyHelper after update");
				createAndSetBean();
			}
		} else {
			log.warn("Not instances of RoleRestrictedProperty: " + oldObj
					+ ", " + newObj);
		}
	}

	private boolean anyRoleChanged(RoleLevel oldDisplayRole,
			RoleLevel oldUpdateRole, RoleLevel oldPublishRole,
			RoleLevel newDisplayRole, RoleLevel newUpdateRole,
			RoleLevel newPublishRole) {
		return (!isTheSame(oldDisplayRole, newDisplayRole))
				|| (!isTheSame(oldUpdateRole, newUpdateRole))
				|| (!isTheSame(oldPublishRole, newPublishRole));
	}

	private boolean isTheSame(RoleLevel oldRole, RoleLevel newRole) {
		if ((oldRole == null) && (newRole == null)) {
			return true;
		} else if ((oldRole == null) || (newRole == null)) {
			return false;
		} else {
			return oldRole.compareTo(newRole) == 0;
		}
	}

	private void createAndSetBean() {
		OntModel model = ModelAccess.on(ctx).getOntModel();
		Model displayModel = ModelAccess.on(ctx).getOntModel(DISPLAY);
		PropertyRestrictionPolicyHelper bean = PropertyRestrictionPolicyHelper
				.createBean(model, displayModel);
		PropertyRestrictionPolicyHelper.setBean(ctx, bean);
	}
}
