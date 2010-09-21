/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder.ClassView;

public class IndividualTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(IndividualTemplateModel.class);
    
    private static final String PATH = Route.INDIVIDUAL.path();
    
    private Individual individual;
    
    public IndividualTemplateModel(Individual individual) {
        this.individual = individual;
    }
    
    /* These methods perform some manipulation of the data returned by the Individual methods */
// RY Individiual.getMoniker() was already trying to do this, but due to errors in the code it was not.
// That's fixed now.
//    public String getTagline() {
//        String tagline = individual.getMoniker();
//        return StringUtils.isEmpty(tagline) ? individual.getVClass().getName() : tagline;
//    }
    
    public String getProfileUrl() {
        String profileUrl = null;
        String individualUri = individual.getURI();
        
        URI uri = new URIImpl(individualUri);
        String namespace = uri.getNamespace();
        String defaultNamespace = vreq.getWebappDaoFactory().getDefaultNamespace();
        
        if (defaultNamespace.equals(namespace)) {
            profileUrl = getUrl(PATH + "/" + individual.getLocalName());
        } else {
            ParamMap params = new ParamMap("uri", individualUri);
            profileUrl = getUrl("/individual", params);
        }
        
        return profileUrl;
    }
    
    public String getVisualizationUrl() {
        return isPerson() ? getUrl(Route.VISUALIZATION.path(), "uri", getUri()) : null;
    }

    // RY We should not have references to a specific ontology in the vitro code!
    // Figure out how to move this out of here.
    // We could subclass IndividualTemplateModel in the VIVO code and add the isPerson()
    // and getVisualizationUrl() methods there, but we still need to know whether to
    // instantiate the IndividualTemplateModel or the VivoIndividualTemplateModel class.
    public boolean isPerson() {
        return individual.isVClass("http://xmlns.com/foaf/0.1/Person");        
    }

    public String getImageUrl() {
        String imageUrl = individual.getImageUrl();
        return imageUrl == null ? null : getUrl(imageUrl);
    }
    
    public String getThumbUrl() {
        String thumbUrl = individual.getThumbUrl();
        return thumbUrl == null ? null : getUrl(thumbUrl);
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
        return vf.findClassView(individual, vreq);
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

    /* These methods simply forward to the Individual methods. It would be desirable to implement a scheme
       for proxying or delegation so that the methods don't need to be simply listed here. 
       A Ruby-style method missing method would be ideal. 
       Update: DynamicProxy doesn't work because the proxied object is of type Individual, so we cannot
       declare new methods here that are not declared in the Individual interface. 
    */
    public String getName() {
        return individual.getName();
    }
    
    public String getMoniker() {
        return individual.getMoniker();
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
    
    public List<String> getKeywords() {
        return individual.getKeywords();
    }
    
}
