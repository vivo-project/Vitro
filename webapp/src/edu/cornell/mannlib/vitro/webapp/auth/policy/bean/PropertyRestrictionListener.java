/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;

/**
 * Add this ChangeListener to your EditProcessObject when modifying the
 * ontology, and we will refresh the PropertyRestrictionPolicyHelper bean as
 * appropriate.
 */
public class PropertyRestrictionListener implements ChangeListener {
	private static final Log log = LogFactory
			.getLog(PropertyRestrictionListener.class);

	/**
	 * If the deleted property had a non-null restriction, rebuild the bean.
	 */
	@Override
	public void doDeleted(Object oldObj, EditProcessObject epo) {
		if (oldObj instanceof RoleRestrictedProperty) {
			RoleRestrictedProperty p = (RoleRestrictedProperty) oldObj;
			FullPropertyKey key = new FullPropertyKey(p);
			updateLevels(new PropertyRestrictionLevels(key, p));
		} else {
			log.warn("Not an instance of RoleRestrictedProperty: " + oldObj);
		}
	}

	/**
	 * Update the inserted property.
	 */
	@Override
	public void doInserted(Object newObj, EditProcessObject epo) {
		if (newObj instanceof RoleRestrictedProperty) {
			RoleRestrictedProperty p = (RoleRestrictedProperty) newObj;
			FullPropertyKey key = new FullPropertyKey(p);
			updateLevels(new PropertyRestrictionLevels(key, p));
		} else {
			log.warn("Not an instance of RoleRestrictedProperty: " + newObj);
		}
	}

	/**
	 * Update the changed property.
	 */
	@Override
	public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
		if (newObj instanceof RoleRestrictedProperty) {
			RoleRestrictedProperty newP = (RoleRestrictedProperty) newObj;
			FullPropertyKey key = new FullPropertyKey(newP);
			updateLevels(new PropertyRestrictionLevels(key, newP));
		} else {
			log.warn("Not instances of RoleRestrictedProperty: " + oldObj
					+ ", " + newObj);
		}
	}

	private void updateLevels(PropertyRestrictionLevels levels) {
		PropertyRestrictionBean.getBean().updateProperty(levels);
	}
}
