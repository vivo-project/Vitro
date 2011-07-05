/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder.ClassView;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class IndividualSearchResult extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(IndividualSearchResult.class);
    
    protected Individual individual;
    protected VitroRequest vreq;
       
    public IndividualSearchResult(Individual individual, VitroRequest vreq) {
        this.individual = individual;
        this.vreq = vreq;

    }

    private String getView(ClassView view) {
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
    
    /* Access methods for templates */

    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq);
    }    
    
    public String getName() {           
        return individual.getName();
    }
    
    public List<String> getMostSpecificTypes() {
        ObjectPropertyStatementDao opsDao = vreq.getWebappDaoFactory().getObjectPropertyStatementDao();
        return opsDao.getMostSpecificTypesForIndividual(individual.getURI());  
    }
    
    public String getSearchView() {        
        return getView(ClassView.SEARCH);
    }
    
}
