/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.view.ViewFinder.ClassView;

public class IndividualView extends ViewObject {
    
    private static final Log log = LogFactory.getLog(IndividualView.class.getName());
    
    private static final String PATH = Route.INDIVIDUAL.path();
    
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
    
    // Return link to individual's profile page.
    // There may be other urls associated with the individual. E.g., we might need 
    // getEditUrl(), getDeleteUrl() to return the links computed by PropertyEditLinks.
    // RY **** Need to account for everything in URLRewritingHttpServlet
    public String getProfileUrl() {
        return getUrl(PATH + "/" + individual.getLocalName());
    }
        
    public String getSearchView() {        
        return getView(ClassView.SEARCH);
    }
    
    public String getShortView() {        
        return getView(ClassView.SHORT);
    }
    
    public String getDisplayView() {        
        return getView(ClassView.DISPLAY);
    }
    
    private String getView(ClassView view) {
        ViewFinder vf = new ViewFinder(view);
        return vf.findClassView(individual, context);
    }
    
    public Link getPrimaryLink() {
        Link primaryLink = null;
        String anchor = individual.getAnchor();
        String url = individual.getUrl();
        if (anchor != null && url != null) {
            primaryLink = new Link();
            primaryLink.setAnchor(individual.getAnchor());
            primaryLink.setUrl(individual.getUrl());           
        } 
        return primaryLink;
    }
    
    public List<Link> getLinks() {
        List<Link> additionalLinks = individual.getLinksList();
        List<Link> links = new ArrayList<Link>(additionalLinks.size()+1);
        Link primaryLink = getPrimaryLink();
        if (primaryLink != null) {
            links.add(primaryLink);
        }        
        links.addAll(additionalLinks);
        return links;      
    }

}
