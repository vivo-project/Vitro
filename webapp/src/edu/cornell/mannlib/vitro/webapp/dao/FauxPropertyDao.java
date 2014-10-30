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
	FauxProperty getFauxPropertyFromConfigContextUri(String contextUri);

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

}
