/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyTemplateModel.class); 
    
    private String name;
    private String localName;
    protected String propertyUri;
    protected Map<String, Object> verboseDisplay = null;
    
    // For editing
    protected String subjectUri = null;
    protected boolean addAccess = false;
    
    PropertyTemplateModel(Property property, Individual subject, EditingPolicyHelper policyHelper, VitroRequest vreq) {
        propertyUri = property.getURI();
        localName = property.getLocalName();        
        log.debug("Local name for property " + propertyUri + ": " + localName);
        setVerboseDisplayValues(property, vreq);
        // Do in subclass constructor. The label has not been set on the property, and the
        // means of getting the label differs between object and data properties.
        // this.name = property.getLabel();
        
        if (policyHelper != null) {
            subjectUri = subject.getURI();            
        }
    }
    
    protected void setVerboseDisplayValues(Property property, VitroRequest vreq) {  
        // No verbose display for these properties
        if (GroupedPropertyList.VITRO_PROPS_TO_ADD_TO_LIST.contains(property)) {
            return;
        }
        Boolean verboseDisplayFlag = (Boolean) vreq.getSession().getAttribute("verbosePropertyDisplay");
        if ( ! Boolean.TRUE.equals(verboseDisplayFlag))  {
            return;
        }
        
        LoginStatusBean loginStatusBean = LoginStatusBean.getBean(vreq);
        if (! loginStatusBean.isLoggedInAtLeast(LoginStatusBean.CURATOR)) {
            return;
        }
        
        verboseDisplay = new HashMap<String, Object>();
        verboseDisplay.put("displayLevel", property.getHiddenFromDisplayBelowRoleLevel().getLabel());
        verboseDisplay.put("updateLevel", property.getProhibitedFromUpdateBelowRoleLevel().getLabel());   
        verboseDisplay.put("localName", property.getLocalNameWithPrefix());
        verboseDisplay.put("displayTier", getPropertyDisplayTier(property));
        
        UrlBuilder urlBuilder = new UrlBuilder(vreq.getPortal());
        String editUrl = urlBuilder.getPortalUrl(getPropertyEditRoute(), "uri", property.getURI());
        verboseDisplay.put("propertyEditUrl", editUrl);
    }
    
    protected abstract Object getPropertyDisplayTier(Property p);
    protected abstract Route getPropertyEditRoute();
    
    protected void setName(String name) {
        this.name = name;
    }
    
    
    /* Access methods for templates */
    
    public abstract String getType();
    
    public String getName() {
        return name;
    }

    public String getLocalName() {
        return localName;
    }
    
    public String getUri() {
        return propertyUri;
    }
    
    public abstract String getAddUrl();
    
    public Map<String, Object> getVerboseDisplay() {
        return verboseDisplay;
    }
 
}
