/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public class IndividualView extends ViewObject {
    
    private static final Log log = LogFactory.getLog(IndividualView.class.getName());
    
    private static String URL = "/individual";
    
    private Individual individual;
    
    public IndividualView(Individual individual) {
        init(individual);
    }
    
    public IndividualView(String contextPath, Individual individual) {
        super(contextPath);
        init(individual);
    }
    
    private void init(Individual individual) {
        this.individual = individual;
    }
   
    
    public String getName() {
        return individual.getName();
    }
    
    public String getMoniker() {
        return individual.getMoniker();
    }
    
    public String getUri() {
        return individual.getURI();
    }
    
    // Or maybe getProfileUrl - there might be other kinds of urls
    // e.g., getEditUrl, getDeleteUrl - these would return the computations of PropertyEditLinks
    public String getProfileUrl() {
        return getContextPath() + URL + ""; // ADD IN the label from the individual's uri 
    }
    
    public String getCustomView() {
        // see code currently in entityList.ftl
        String customView = null;
        
        return customView;
    }


}
