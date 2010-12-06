/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder.ClassView;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class IndividualTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(IndividualTemplateModel.class);
    
    private static final String PATH = Route.INDIVIDUAL.path();
    
    protected Individual individual;
    protected VitroRequest vreq;
    protected UrlBuilder urlBuilder;
    
    public IndividualTemplateModel(Individual individual, VitroRequest vreq) {
        this.individual = individual;
        this.vreq = vreq;
        // Needed for getting portal-sensitive urls. Remove if multi-portal support is removed.
        this.urlBuilder = new UrlBuilder(vreq.getPortal());
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
        WebappDaoFactory wadf = vreq.getWebappDaoFactory();
        String defaultNamespace = wadf.getDefaultNamespace();
                
        if (defaultNamespace.equals(namespace)) {
            profileUrl = getUrl(PATH + "/" + individual.getLocalName());
        } else {
            List<String> externallyLinkedNamespaces = wadf.getApplicationDao().getExternallyLinkedNamespaces();
            if (externallyLinkedNamespaces.contains(namespace)) {
                log.debug("Found externally linked namespace " + namespace);
                profileUrl = namespace + "/" + individual.getLocalName();
            } else {
                ParamMap params = new ParamMap("uri", individualUri);
                profileUrl = getUrl("/individual", params);
            }
        }
        
        return profileUrl;
    }
    
    public String getVisualizationUrl() {
        return isPerson() ? getUrl(Route.VISUALIZATION.path(), "uri", getUri()) : null;
    }

    public String getImageUrl() {
        String imageUrl = individual.getImageUrl();
        return imageUrl == null ? null : getUrl(imageUrl);
    }
    
    public String getThumbUrl() {
        String thumbUrl = individual.getThumbUrl();
        return thumbUrl == null ? null : getUrl(thumbUrl);
    } 
    
    public String getLinkedDataUrl() {
        String defaultNamespace = vreq.getWebappDaoFactory().getDefaultNamespace();
        String uri = getUri();
        return uri.startsWith(defaultNamespace) ? uri + "/" + getLocalName() + ".rdf" : null;
    }
    
    public String getEditUrl() {
        return urlBuilder.getPortalUrl(Route.INDIVIDUAL_EDIT, "uri", getUri());
    }

    // RY We should not have references to a specific ontology in the vitro code!
    // Figure out how to move this out of here.
    // We could subclass IndividualTemplateModel in the VIVO code and add the isPerson()
    // and getVisualizationUrl() methods there, but we still need to know whether to
    // instantiate the IndividualTemplateModel or the VivoIndividualTemplateModel class.
    public boolean isPerson() {
        return individual.isVClass("http://xmlns.com/foaf/0.1/Person");        
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

    public static List<IndividualTemplateModel> getIndividualTemplateModelList(List<Individual> individuals, VitroRequest vreq) {
        List<IndividualTemplateModel> models = new ArrayList<IndividualTemplateModel>(individuals.size());
        for (Individual individual : individuals) {
          models.add(new IndividualTemplateModel(individual, vreq));
        }  
        return models;
    }

    public List<PropertyGroupTemplateModel> getPropertyList() {
        PropertyListBuilder propListBuilder = new PropertyListBuilder(individual, vreq);
        return propListBuilder.getPropertyList();
    }
    
    /* These methods simply forward to the methods of the wrapped individual. It would be desirable to 
     * implement a scheme for proxying or delegation so that the methods don't need to be simply listed here. 
     * A Ruby-style method missing method would be ideal. 
     * Update: DynamicProxy doesn't work because the proxied object is of type Individual, so we cannot
     * declare new methods here that are not declared in the Individual interface. 
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
    
    public String getKeywordString() {
        // Since this is a display method, the implementation should be moved out of IndividualImpl to here.
        return individual.getKeywordString();
    }
    
    public String getLocalName() {
        return individual.getLocalName();
    }
    

    
}
