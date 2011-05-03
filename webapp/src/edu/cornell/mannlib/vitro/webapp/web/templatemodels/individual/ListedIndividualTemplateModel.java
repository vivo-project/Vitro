/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder.ClassView;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class ListedIndividualTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(ListedIndividualTemplateModel.class);
    
    private static final String PATH = Route.INDIVIDUAL.path();
    
    protected Individual individual;
    protected VitroRequest vreq;
    protected UrlBuilder urlBuilder;
    
    
    public ListedIndividualTemplateModel(Individual individual, VitroRequest vreq) {
        this.individual = individual;
        this.vreq = vreq;
        // Needed for getting portal-sensitive urls. Remove if multi-portal support is removed.
        this.urlBuilder = new UrlBuilder(vreq.getAppBean());
    }
    
    public static List<ListedIndividualTemplateModel> getIndividualTemplateModelList(List<Individual> individuals, VitroRequest vreq) {
        List<ListedIndividualTemplateModel> models = new ArrayList<ListedIndividualTemplateModel>(individuals.size());
        for (Individual individual : individuals) {
          models.add(new ListedIndividualTemplateModel(individual, vreq));
        }  
        return models;
    }
    
    private String getView(ClassView view) {
        ViewFinder vf = new ViewFinder(view);
        return vf.findClassView(individual, vreq);
    }
    
    /* Access methods for templates */
    
    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq.getWebappDaoFactory());
    }    

    public String getImageUrl() {
        String imageUrl = individual.getImageUrl();
        return imageUrl == null ? null : getUrl(imageUrl);
    }
    
    public String getThumbUrl() {
        String thumbUrl = individual.getThumbUrl();
        return thumbUrl == null ? null : getUrl(thumbUrl);
    } 
    
    public Link getPrimaryLink() {
        Link primaryLink = null;
        String anchor = individual.getAnchor();
        String url = individual.getUrl();
        if ( !(StringUtils.isEmpty(anchor)) && !(StringUtils.isEmpty(url)) ) {
            primaryLink = new Link();
            primaryLink.setAnchor(anchor);
            primaryLink.setUrl(url);           
        } 
        return primaryLink;
    }
    
    public List<Link> getAdditionalLinks() {
        return individual.getLinksList(); // returns an empty list, but not null
    }
    
    public List<Link> getLinks() {
        List<Link> additionalLinks = getAdditionalLinks();  // returns an empty list, but not null
        List<Link> links = new ArrayList<Link>(additionalLinks.size()+1);
        Link primaryLink = getPrimaryLink();
        if (primaryLink != null) {
            links.add(primaryLink);
        }        
        for (Link link : additionalLinks) {
            // Hide malformed links from the template to make things easier
            if ( !(StringUtils.isEmpty(link.getAnchor())) && !(StringUtils.isEmpty(link.getUrl())) ) {
                links.add(link);
            }
        }
        return links;  // returns an empty list, but not null   
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
    
    public String getSearchView() {        
        return getView(ClassView.SEARCH);
    }
    
}
