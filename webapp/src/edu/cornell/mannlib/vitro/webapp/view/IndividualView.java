/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.Routes;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class IndividualView extends ViewObject {
    
    private static final Log log = LogFactory.getLog(IndividualView.class.getName());
    
    private static final String URL = Routes.INDIVIDUAL;
    
    private Individual individual;
    
    public IndividualView(Individual individual) {
        this.individual = individual;
    }
    
    public String getName() {
        return individual.getName();
    }
    
    // RY However, the moniker should undergo p:process but the class name shouldn't! 
    // So, it needs to be callable from Java.
    public String getTagline() {
        String tagline = individual.getMoniker();
        return StringUtils.isEmpty(tagline) ? individual.getVClass().getName() : tagline;
    }
    
    public String getUri() {
        return individual.getURI();
    }
    
    // Or maybe getProfileUrl - there might be other kinds of urls
    // e.g., getEditUrl, getDeleteUrl - these would return the computations of PropertyEditLinks
    // RY **** Need to account for everything in URLRewritingHttpServlet
    public String getProfileUrl() {
        return getUrl("/individual/" + individual.getLocalName());
    }
    
    public String getRenderer() {
        // TODO
        // iterate through class hierarchy looking for a custom renderer. If none, use
        // default individual renderer. template will just do an include on individual.renderer
        // Use individual.getVClasses() - this is the class hierarchy
        // Question: what order are they returned in ? If from specific to general, break out of the iteration as soon as we find one.
        return null;
    }
    
    public String getCustomView() {
        // see code currently in entityList.ftl
        String customView = null;
        
        return customView;
    }
    
    public Object getProperty(String propertyName) {
        return new Object();
    }


}
