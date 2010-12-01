/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {
    
    protected Property property;
    
    PropertyTemplateModel(Property propertry) {
        this.property = property;
    }
    
    protected Property getProperty() {
        return property;
    }
    
    protected String getUri() {
        return property.getURI();
    }
    
    /* Access methods for templates */
    
    public String getAddLink() {
        return null;
    }
    
    public abstract String getType();
 
}
