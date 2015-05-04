/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * When the ConfigurationBeanLoader creates an instance of this class, it will
 * call this method, supplying the RDF models for the current HTTP request.
 */
public interface RequestModelsUser {
	void setRequestModels(RequestModelAccess models);
}
