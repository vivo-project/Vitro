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

public abstract class BaseListedIndividual extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseListedIndividual.class);

    protected Individual individual;
    protected VitroRequest vreq;    
    
    public BaseListedIndividual(Individual individual, VitroRequest vreq) {
        this.individual = individual;
        this.vreq = vreq;
    }
    
    public static List<ListedIndividual> getIndividualTemplateModels(List<Individual> individuals, VitroRequest vreq) {
        List<ListedIndividual> models = new ArrayList<ListedIndividual>(individuals.size());
        for (Individual individual : individuals) {
          models.add(new ListedIndividual(individual, vreq));
        }  
        return models;
    }
    
    /* Access methods for templates */
    
    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq);
    }    

    public String getImageUrl() {
        String imageUrl = individual.getImageUrl();
        return imageUrl == null ? null : getUrl(imageUrl);
    }
    
    public String getThumbUrl() {
        String thumbUrl = individual.getThumbUrl();
        return thumbUrl == null ? null : getUrl(thumbUrl);
    } 
    
    public String getName() {           
        return individual.getName();
    }

    public String getUri() {
        return individual.getURI();
    }    

    
}
