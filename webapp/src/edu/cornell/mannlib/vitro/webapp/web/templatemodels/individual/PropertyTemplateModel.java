/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyTemplateModel.class); 
    
    private String name;
    protected String propertyUri;
    
    // For editing
    protected String subjectUri = null;
    protected boolean addAccess = false;
    
    PropertyTemplateModel(Property property, Individual subject, EditingPolicyHelper policyHelper) {
        // Do in subclass constructor. The label has not been set on the property, and the
        // means of getting the label differs between object and data properties.
        // this.name = property.getLabel();
        propertyUri = property.getURI();
        
        if (policyHelper != null) {
            subjectUri = subject.getURI();            
        }
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    protected String getUri() {
        return propertyUri;
    }
        
    
    /* Access methods for templates */
    
    public abstract String getType();
    
    public String getName() {
        return name;
    }


    public abstract String getAddUrl();
 
}
