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
    private String uri;
    protected Property property; // needed to get the edit links
    
    PropertyTemplateModel(Property property) {
        this.name = property.getLabel();
        // Do in subclass constructor. The label has not been set on the property, and getting the 
        // label differs between object and data properties.
        // this.name = property.getLabel();
        this.uri = property.getURI();
        this.property = property;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    /* Access methods for templates */
    
    public abstract String getType();
    
    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }
    
    public abstract String getAddLink();
    
    public abstract String getEditLink();
    
    public abstract String getDeleteLink();
 
}
