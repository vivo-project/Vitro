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
    
    /* These methods perform some manipulation of the data returned by the Individual methods */
    public String getTagline() {
        String tagline = individual.getMoniker();
        return StringUtils.isEmpty(tagline) ? individual.getVClass().getName() : tagline;
    }
    
    // Return link to individual's profile page.
    // There may be other urls associated with the individual. E.g., we might need 
    // getEditUrl(), getDeleteUrl() to return the links computed by PropertyEditLinks.
    // RY **** Need to account for everything in URLRewritingHttpServlet
    // Currently this is incorrect for individuals that are not in the default namespace (e.g., geographic individuals).
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
    
    // RY Here we really want to return List<LinkView> objects. Instead of writing the LinkView class by hand,
    // it would be better to use an alternate FreeMarker BeanWrapper that creates a read-only template data object.
    // That would also be used to return the lists of object properties and data properties of the individual.
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

    /* These methods simply forward to the Individual methods. It would be desirable to implement a scheme
       for proxying or delegation so that the methods don't need to be simply listed here. 
       A Ruby-style method missing method would be ideal. */
    public String getName() {
        return individual.getName();
    }

    public String getUri() {
        return individual.getURI();
    }
    
    public String getDescription() {
        return individual.getDescription();
    }
    
    public String getBlurb() {
        return individual.getBlurb();
    }
    
    public String getCitation() {
        return individual.getBlurb();
    }
    
    public List<String> getKeywords() {
        return individual.getKeywords();
    }
    
    public String getImageFile() {
        return individual.getImageFile();
    }
    
    public String getImageThumb() {
        return individual.getImageThumb();
    }
}
