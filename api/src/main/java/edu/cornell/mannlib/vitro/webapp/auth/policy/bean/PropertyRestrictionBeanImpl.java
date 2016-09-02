/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import static edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which.MODIFY;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which.PUBLISH;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.impl.Util;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;

/**
 * On creation, populate a map of PropertyRestrictionLevels.
 * 
 * When a change is detected, update the map accordingly.
 * 
 * ------------------------------
 * 
 * How is authorization determined?
 * 
 * Resources are easy. If they aren't in a prohibited namespace, or are an
 * exception to the prohibition, they are accessible.
 * 
 * Properties are harder. The prohibited namespace and exceptions still apply,
 * but if we pass that test, then we check the threshold map.
 * 
 * When a test is made, we look for thresholds in the map. First we look for the
 * full key of domain-base-range, in case we are testing a faux property. Faux
 * properties are recorded in the map with the full key.
 * 
 * If we don't find the full key, then perhaps we are testing a faux property
 * that has no settings, or perhaps we are testing an object property. We look
 * for the partial key of null-base-null, which covers both of these cases,
 * since object properties (and data properties) are recorded the map with a
 * partial key.
 * 
 * Similarly, if we find a null threshold value in the full key, we look back to
 * the partial key for a threshold.
 * 
 * If we find no non-null threshold value then the property is unrestricted for
 * that feature.
 * 
 * -----------------------------
 * 
 * It would perhaps be a silly optimization, but if we find a key with 3 null
 * thresholds, we could remove it from the map without changing the behavior.
 */
public class PropertyRestrictionBeanImpl extends PropertyRestrictionBean {
	private static final Log log = LogFactory
			.getLog(PropertyRestrictionBeanImpl.class);

	private final Set<String> prohibitedNamespaces;
	private final Set<String> permittedExceptions;

	private final Map<FullPropertyKey, PropertyRestrictionLevels> thresholdMap = new ConcurrentHashMap<>();

	public PropertyRestrictionBeanImpl(Collection<String> prohibitedNamespaces,
			Collection<String> permittedExceptions, ContextModelAccess models) {
		Objects.requireNonNull(prohibitedNamespaces,
				"prohibitedNamespaces may not be null.");
		this.prohibitedNamespaces = Collections.unmodifiableSet(new TreeSet<>(
				prohibitedNamespaces));

		Objects.requireNonNull(permittedExceptions,
				"permittedExceptions may not be null.");
		this.permittedExceptions = Collections.unmodifiableSet(new TreeSet<>(
				permittedExceptions));

		Objects.requireNonNull(models, "models may not be null.");
		populateThresholdMap(models.getWebappDaoFactory());
	}

	private void populateThresholdMap(WebappDaoFactory wadf) {
		for (ObjectProperty oProp : wadf.getObjectPropertyDao()
				.getAllObjectProperties()) {
			addObjectPropertyToMap(oProp);
			for (FauxProperty fProp : wadf.getFauxPropertyDao()
					.getFauxPropertiesForBaseUri(oProp.getURI())) {
				addFauxPropertyToMap(fProp);
			}
		}
		for (DataProperty dProp : wadf.getDataPropertyDao()
				.getAllDataProperties()) {
			addDataPropertyToMap(dProp);
		}
	}

	private void addObjectPropertyToMap(ObjectProperty oProp) {
		FullPropertyKey key = new FullPropertyKey(oProp.getURI());
		PropertyRestrictionLevels levels = new PropertyRestrictionLevels(key,
				oProp.getHiddenFromDisplayBelowRoleLevel(),
				oProp.getProhibitedFromUpdateBelowRoleLevel(),
				oProp.getHiddenFromPublishBelowRoleLevel());
		thresholdMap.put(key, levels);
	}

	private void addFauxPropertyToMap(FauxProperty fProp) {
		FullPropertyKey key = new FullPropertyKey(fProp.getDomainURI(),
				fProp.getBaseURI(), fProp.getRangeURI());
		PropertyRestrictionLevels levels = new PropertyRestrictionLevels(key,
				fProp.getHiddenFromDisplayBelowRoleLevel(),
				fProp.getProhibitedFromUpdateBelowRoleLevel(),
				fProp.getHiddenFromPublishBelowRoleLevel());
		thresholdMap.put(key, levels);
	}

	private void addDataPropertyToMap(DataProperty dProp) {
		FullPropertyKey key = new FullPropertyKey(dProp.getURI());
		PropertyRestrictionLevels levels = new PropertyRestrictionLevels(key,
				dProp.getHiddenFromDisplayBelowRoleLevel(),
				dProp.getProhibitedFromUpdateBelowRoleLevel(),
				dProp.getHiddenFromPublishBelowRoleLevel());
		thresholdMap.put(key, levels);
	}

	@Override
	public boolean canDisplayResource(String resourceUri, RoleLevel userRole) {
		return (resourceUri != null) && (userRole != null);
	}

	@Override
	public boolean canModifyResource(String resourceUri, RoleLevel userRole) {
		if (resourceUri == null || userRole == null) {
			return false;
		}
		if (prohibitedNamespaces.contains(namespace(resourceUri))
				&& !permittedExceptions.contains(resourceUri)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canPublishResource(String resourceUri, RoleLevel userRole) {
		return (resourceUri != null) && (userRole != null);
	}

	@Override
	public boolean canDisplayPredicate(Property predicate, RoleLevel userRole) {
		if (predicate == null || predicate.getURI() == null) {
			return false;
		}
		return isAuthorized(userRole, getThreshold(predicate, DISPLAY));
	}

	@Override
	public boolean canModifyPredicate(Property predicate, RoleLevel userRole) {
		if (predicate == null || predicate.getURI() == null) {
			return false;
		}
		return isAuthorized(userRole, getPropertyModifyThreshold(predicate));
	}

	@Override
	public boolean canPublishPredicate(Property predicate, RoleLevel userRole) {
		if (predicate == null || predicate.getURI() == null) {
			return false;
		}
		return isAuthorized(userRole, getThreshold(predicate, PUBLISH));
	}

	@Override
	public void updateProperty(PropertyRestrictionLevels levels) {
		thresholdMap.put(levels.getKey(), levels);
	}

	private boolean isAuthorized(RoleLevel userRole, RoleLevel thresholdRole) {
		if (userRole == null) {
			return false;
		}
		if (thresholdRole == null) {
			return true;
		}
		return userRole.compareTo(thresholdRole) >= 0;
	}

	private RoleLevel getPropertyModifyThreshold(Property p) {
		if (prohibitedNamespaces.contains(namespace(p.getURI()))
				&& !permittedExceptions.contains(p.getURI())) {
			return RoleLevel.NOBODY;
		}
		return getThreshold(p, MODIFY);
	}

	private RoleLevel getThreshold(Property p, Which which) {
		RoleLevel qualifiedLevel = getThreshold(new FullPropertyKey(p), which);
		if (qualifiedLevel != null) {
			return qualifiedLevel;
		}

		RoleLevel bareLevel = getThreshold(new FullPropertyKey(p.getURI()),
				which);
		return bareLevel;
	}

	private RoleLevel getThreshold(FullPropertyKey key, Which which) {
		PropertyRestrictionLevels levels = thresholdMap.get(key);
		if (levels == null) {
			return null;
		} else {
			return levels.getLevel(which);
		}
	}

	private String namespace(String uri) {
		return uri.substring(0, Util.splitNamespaceXML(uri));
	}

	@Override
	public String toString() {
		SortedSet<FullPropertyKey> keys = new TreeSet<>(
				new Comparator<FullPropertyKey>() {
					@Override
					public int compare(FullPropertyKey o1, FullPropertyKey o2) {
						return o1.toString().compareTo(o2.toString());
					}
				});
		keys.addAll(thresholdMap.keySet());

		StringBuilder buffer = new StringBuilder();
		for (FullPropertyKey key : keys) {
			buffer.append(key + " " + thresholdMap.get(key).getLevel(DISPLAY)
					+ " " + thresholdMap.get(key).getLevel(MODIFY) + " "
					+ thresholdMap.get(key).getLevel(PUBLISH) + "\n");
		}
		return buffer.toString();

	}

}
