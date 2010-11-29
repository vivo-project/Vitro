/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {
    
    // Not sure whether we need the property or the uri.
    protected String predicateUri = null; 
    
    PropertyTemplateModel(String predicateUri) {
        this.predicateUri = predicateUri;
    }
    
    public String getAddLink() {
        return null;
    }
 
}
