/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;

/**
 * Utility methods for fetching, storing and manipulating FauxProperty objects.
 */
public interface FauxPropertyDao {

	/**
	 * Get all of the FauxProperties that are based on this URI.
	 * 
	 * @return May return an empty list. Never returns null.
	 */
	List<FauxProperty> getFauxPropertiesForBaseUri(String uri);

	/**
	 * If the display model contains a ConfigContext with this URI, get the
	 * FauxProperty that it describes.
	 * 
	 * @return May return null.
	 */
	FauxProperty getFauxPropertyFromContextUri(String contextUri);

	/**
	 * If the display model contains a ConfigContext based on these URIs, get
	 * the FauxProperty that it describes. May return null.
	 * 
	 * @param domainUri
	 *            May be null, but then this will only match a ConfigContext
	 *            that has no qualifiedByDomain property.
	 * @param baseUri
	 *            Object of configContextFor property. May not be null.
	 * @param rangeUri
	 *            Object of qualifiedBy property. May not be null.
	 * @return May return null.
	 */
	FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri);

	/**
	 * Creates a new FauxProperty in the display model.
	 * 
	 * By "a new FauxProperty", we mean a new ConfigContext and a new
	 * ObjectPropertyDisplayConfig linked to it.
	 * 
	 * @throws IllegalStateException
	 *             if fp does not have null values for contextUri and configUri,
	 *             or if a FauxProperty already exists with this combination of
	 *             domain, base, and range URIs.
	 * @throws IllegalArgumentException
	 *             if fp is not internally consistent.
	 */
	void insertFauxProperty(FauxProperty fp);

	/**
	 * Updates the properties of this FauxProperty in the display model.
	 * 
	 * By "this FauxProperty", we mean the ConfigContext and
	 * ObjectPropertyDisplayConfig whose URIs are stored in this FauxProperty.
	 * 
	 * @throws IllegalStateException
	 *             if the display model contains no such individuals. If you
	 *             want to create a new FauxProperty instance, you should be
	 *             using insertFauxProperty() instead.
	 * @throws IllegalArgumentException
	 *             if fp is not internally consistent.
	 */
	void updateFauxProperty(FauxProperty fp);

	/**
	 * Delete this FauxProperty from the display model.
	 * 
	 * Delete any ConfigContext that is based on the constraints in this
	 * FauxProperty, and any ObjectPropertyDisplayConfigs that depend on that
	 * ConfigContext.
	 * 
	 * If no such ConfigContext is found, no error is raised.
	 * 
	 * No check is made to see whether the ObjectPropertyDisplayConfig matches
	 * the settings on this FauxProperty.
	 */
	void deleteFauxProperty(FauxProperty fp);
}
