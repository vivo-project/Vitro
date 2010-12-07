/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyTemplateModel.class); 
    
    private String name;
    protected Property property;
    
    PropertyTemplateModel(Property property) {
        this.name = property.getLabel();
        this.property = property;
    }
    
    /* Access methods for templates */
    
    public abstract String getType();
    
    public String getName() {
        return name;
    }
    
    public abstract String getAddLink();
    
    public abstract String getEditLink();
    
    public abstract String getDeleteLink();
 
}
