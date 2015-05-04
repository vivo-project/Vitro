/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;

/**
 * The threshold levels for operations on a given property.
 * 
 * This is based on the assumption that the FullPropertyKey is sufficient to
 * distinguish all properties. An object property and a data property may not
 * share the same key. A faux property must have a different key from any object
 * property.
 */
public class PropertyRestrictionLevels {
	private final FullPropertyKey key;
	private final RoleLevel displayThreshold;
	private final RoleLevel modifyThreshold;
	private final RoleLevel publishThreshold;

	public enum Which {
		DISPLAY, MODIFY, PUBLISH
	}

	public PropertyRestrictionLevels(FullPropertyKey key,
			RoleRestrictedProperty p) {
		this(key, p.getHiddenFromDisplayBelowRoleLevel(), p
				.getProhibitedFromUpdateBelowRoleLevel(), p
				.getHiddenFromPublishBelowRoleLevel());
	}

	public PropertyRestrictionLevels(FullPropertyKey key,
			RoleLevel displayThreshold, RoleLevel modifyThreshold,
			RoleLevel publishThreshold) {
		this.key = key;
		this.displayThreshold = displayThreshold;
		this.modifyThreshold = modifyThreshold;
		this.publishThreshold = publishThreshold;
	}

	public FullPropertyKey getKey() {
		return key;
	}

	public RoleLevel getLevel(Which which) {
		if (which == null) {
			return null;
		} else {
			switch (which) {
			case DISPLAY:
				return displayThreshold;
			case MODIFY:
				return modifyThreshold;
			default:
				return publishThreshold;
			}
		}
	}

	@Override
	public String toString() {
		return "PropertyRestrictionLevels[key=" + key + ", display="
				+ displayThreshold + ", modify=" + modifyThreshold
				+ ", publish=" + publishThreshold + "]";
	}

}
