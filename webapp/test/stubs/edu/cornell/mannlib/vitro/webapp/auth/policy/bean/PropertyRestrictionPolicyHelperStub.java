/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * Allow the unit test to specify a variety of restrictions
 */
public class PropertyRestrictionPolicyHelperStub extends
		PropertyRestrictionPolicyHelper {

	/** Don't prohibit or restrict anything. */
	public static PropertyRestrictionPolicyHelper getInstance() {
		return getInstance(null, null);
	}


	/** Prohibit some namespaces. */
	public static PropertyRestrictionPolicyHelperStub getInstance(
			String[] restrictedNamespaces) {
		return getInstance(restrictedNamespaces, null);
	}

	/**
	 * Prohibit some namespaces and restrict some properties from modification
	 * by anybody.
	 */
	public static PropertyRestrictionPolicyHelperStub getInstance(
			String[] restrictedNamespaces, String[] restrictedProperties) {
		Set<String> namespaceSet = new HashSet<String>();
		if (restrictedNamespaces != null) {
			namespaceSet.addAll(Arrays.asList(restrictedNamespaces));
		}
		
		Map<String, RoleLevel> thresholdMap = new HashMap<String, RoleLevel>();
		if (restrictedProperties != null) {
			for (String prop : restrictedProperties) {
				thresholdMap.put(prop, RoleLevel.NOBODY);
			}
		}
		
		return new PropertyRestrictionPolicyHelperStub(namespaceSet, null,
				null, thresholdMap);
	}

	private PropertyRestrictionPolicyHelperStub(
			Set<String> modifyRestrictedNamespaces,
			Set<String> modifyPermittedExceptions,
			Map<String, RoleLevel> displayThresholds,
			Map<String, RoleLevel> modifyThresholds) {
		super(modifyRestrictedNamespaces, modifyPermittedExceptions,
				displayThresholds, modifyThresholds);
	}

}
