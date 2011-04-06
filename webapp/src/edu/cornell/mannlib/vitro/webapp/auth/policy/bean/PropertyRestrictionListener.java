/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

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
		Property p = (Property) oldObj;
		if (eitherRoleChanged(p.getHiddenFromDisplayBelowRoleLevel(),
				p.getProhibitedFromUpdateBelowRoleLevel(), null, null)) {
			log.debug("rebuilding the PropertyRestrictionPolicyHelper after deletion");
			createAndSetBean();
		}
	}

	/**
	 * If the inserted property has a non-null restriction, rebuild the bean.
	 */
	@Override
	public void doInserted(Object newObj, EditProcessObject epo) {
		Property p = (Property) newObj;
		if (eitherRoleChanged(null, null,
				p.getHiddenFromDisplayBelowRoleLevel(),
				p.getProhibitedFromUpdateBelowRoleLevel())) {
			log.debug("rebuilding the PropertyRestrictionPolicyHelper after insertion");
			createAndSetBean();
		}
	}

	/**
	 * If the updated property has changed its restrictions, rebuild the bean.
	 */
	@Override
	public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
		Property oldP = (Property) oldObj;
		Property newP = (Property) newObj;
		if (eitherRoleChanged(oldP.getHiddenFromDisplayBelowRoleLevel(),
				oldP.getProhibitedFromUpdateBelowRoleLevel(),
				newP.getHiddenFromDisplayBelowRoleLevel(),
				newP.getProhibitedFromUpdateBelowRoleLevel())) {
			log.debug("rebuilding the PropertyRestrictionPolicyHelper after update");
			createAndSetBean();
		}
	}

	private boolean eitherRoleChanged(RoleLevel oldDisplayRole,
			RoleLevel oldUpdateRole, RoleLevel newDisplayRole,
			RoleLevel newUpdateRole) {
		return (!isTheSame(oldDisplayRole, newDisplayRole))
				|| (!isTheSame(oldUpdateRole, newUpdateRole));
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
		OntModel model = (OntModel) ctx.getAttribute("jenaOntModel");
		PropertyRestrictionPolicyHelper bean = PropertyRestrictionPolicyHelper
				.createBean(model);
		PropertyRestrictionPolicyHelper.setBean(ctx, bean);
	}
}
