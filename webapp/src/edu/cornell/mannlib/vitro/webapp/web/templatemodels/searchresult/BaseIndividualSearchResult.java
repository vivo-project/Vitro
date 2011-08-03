/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.searchresult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder.ClassView;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class BaseIndividualSearchResult extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseIndividualSearchResult.class);
    
    protected final VitroRequest vreq;
    protected final Individual individual;
       
    public BaseIndividualSearchResult(Individual individual, VitroRequest vreq) {
        this.vreq = vreq;
        this.individual = individual;
    }

    protected String getView(ClassView view) {
        ViewFinder vf = new ViewFinder(view);
        return vf.findClassView(individual, vreq);
    }
    
    public static List<IndividualSearchResult> getIndividualTemplateModels(List<Individual> individuals, VitroRequest vreq) {
        List<IndividualSearchResult> models = new ArrayList<IndividualSearchResult>(individuals.size());
        for (Individual individual : individuals) {
          models.add(new IndividualSearchResult(individual, vreq));
        }  
        return models;
    }
    
    /* Template properties */

    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq);
    }    
    
    public String getName() {           
        return individual.getName();
    }
    
    public Collection<String> getMostSpecificTypes() {
        ObjectPropertyStatementDao opsDao = vreq.getWebappDaoFactory().getObjectPropertyStatementDao();
        Map<String, String> types = opsDao.getMostSpecificTypesInClassgroupsForIndividual(individual.getURI()); 
        return types.values();  
    }
    
    public String getSearchView() {        
        return getView(ClassView.SEARCH);
    }
    
    public String getSnippet() {        
        return individual.getSearchSnippet();
    }
    
}
