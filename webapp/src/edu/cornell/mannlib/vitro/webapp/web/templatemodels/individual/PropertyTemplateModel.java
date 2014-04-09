/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyTemplateModel.class); 
    
    protected final VitroRequest vreq;
    protected final String subjectUri;
    protected final Property property;
    protected final String propertyUri;
    protected String domainUri;
    protected String rangeUri;
    private final String localName;

    protected Map<String, Object> verboseDisplay;
    protected String addUrl;
    
    private String name;


       
    PropertyTemplateModel(Property property, Individual subject, VitroRequest vreq) {
        this.vreq = vreq;
        subjectUri = subject.getURI(); 
        this.property = property;
        propertyUri = property.getURI();
        localName = property.getLocalName();
        setVerboseDisplayValues(property);
        addUrl = "";
        
        // Do in subclass constructor. The label has not been set on the property, and the
        // means of getting the label differs between object and data properties.
        // this.name = property.getLabel();
    }
    
    protected void setVerboseDisplayValues(Property property) {  
        
        // No verbose display for vitro and vitro public properties.
        // This models previous behavior. In theory the verbose display can be provided, 
        // but we may not want to give anyone access to these properties, since the
        // application is dependent on them.
        String namespace = property.getNamespace();        
        if (VitroVocabulary.vitroURI.equals(namespace) 
            || VitroVocabulary.VITRO_PUBLIC.equals(namespace)) {
            return;
        }
        
        Boolean verboseDisplayValue = 
            (Boolean) vreq.getSession().getAttribute("verbosePropertyDisplay");

        if ( ! Boolean.TRUE.equals(verboseDisplayValue))  {
            return;
        }
        
		if (!PolicyHelper.isAuthorizedForActions(vreq,
				SimplePermission.SEE_VERBOSE_PROPERTY_INFORMATION.ACTIONS)) {
            return;
        }
        
        verboseDisplay = new HashMap<String, Object>();
        
        RoleLevel roleLevel = property.getHiddenFromDisplayBelowRoleLevel();
        String roleLevelLabel = roleLevel != null ? roleLevel.getDisplayLabel() : "";
        verboseDisplay.put("displayLevel", roleLevelLabel);

        roleLevel = property.getProhibitedFromUpdateBelowRoleLevel();
        roleLevelLabel = roleLevel != null ? roleLevel.getUpdateLabel() : "";
        verboseDisplay.put("updateLevel", roleLevelLabel);   
        
        roleLevel = property.getHiddenFromPublishBelowRoleLevel();
        roleLevelLabel = roleLevel != null ? roleLevel.getDisplayLabel() : "";
        verboseDisplay.put("publishLevel", roleLevelLabel);   
        
        verboseDisplay.put("localName", property.getLocalNameWithPrefix());
        verboseDisplay.put("displayRank", getPropertyDisplayTier(property));
       
        String editUrl = UrlBuilder.getUrl(getPropertyEditRoute(), "uri", property.getURI());
        verboseDisplay.put("propertyEditUrl", editUrl);
        
        if(isFauxProperty(property)) {
            verboseDisplay.put("fauxProperty", "true");
        } 
    }
    
    private boolean isFauxProperty(Property prop) {
        if(!(prop instanceof ObjectProperty)) {
            return false;
        }
        ObjectPropertyDao opDao = vreq.getWebappDaoFactory().getObjectPropertyDao();
        ObjectProperty baseProp = opDao.getObjectPropertyByURI(prop.getURI());
        if(baseProp == null) {
            return false;
        }
        ObjectProperty possibleFaux = (ObjectProperty) prop;
        if (possibleFaux.getDomainPublic() == null) {
            return (baseProp.getDomainPublic() != null);            
        } else {
            return !possibleFaux.getDomainPublic().equals(baseProp.getDomainPublic());
        }
    }
    
    protected abstract int getPropertyDisplayTier(Property p);
    protected abstract Route getPropertyEditRoute();
    
    protected void setName(String name) {
        this.name = name;
    }
    
    public String toString() {
        return String.format("%s on %s",
                             propertyUri != null ? propertyUri : "null Prop URI",
                             subjectUri != null ? subjectUri : "null Sub URI" );
    }

    /* Template properties */
    
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
    
    public String getRangeUri() {
        return rangeUri;
    }
    
    public String getDomainUri() {
        return domainUri;
    }
    
    public String getAddUrl() {
        //log.info("addUrl=" + addUrl);
        return (addUrl != null) ? addUrl : "";
    }
    
    public Map<String, Object> getVerboseDisplay() {
        return verboseDisplay;
    } 
}
