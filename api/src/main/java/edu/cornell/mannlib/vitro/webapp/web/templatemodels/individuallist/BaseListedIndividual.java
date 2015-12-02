/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class BaseListedIndividual extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseListedIndividual.class);

    protected final Individual individual;  
    protected final VitroRequest vreq;
    
    public BaseListedIndividual(Individual individual, VitroRequest vreq) {
        this.vreq = vreq;
        this.individual = individual;
    }
    
    public static List<ListedIndividual> getIndividualTemplateModels(List<Individual> individuals, VitroRequest vreq) {
        List<ListedIndividual> models = new ArrayList<ListedIndividual>(individuals.size());
        for (Individual individual : individuals) {
          models.add(new ListedIndividual(individual, vreq));
        }  
        return models;
    }
    
    /* Template properties */
    
    public String getProfileUrl() {
        return cleanURIForDisplay( UrlBuilder.getIndividualProfileUrl(individual, vreq) );
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
        return cleanTextForDisplay( individual.getName() );
    }

    public String getUri() {
        return cleanURIForDisplay( individual.getURI() );
    }  
    
    public List<String> getMostSpecificTypes() {
        ObjectPropertyStatementDao opsDao = vreq.getWebappDaoFactory().getObjectPropertyStatementDao();
        Map<String, String> types = opsDao.getMostSpecificTypesInClassgroupsForIndividual(individual.getURI()); 
        List<String> typeLabels = new ArrayList<String>(types.size());
        String displayedType = (String) vreq.getAttribute("displayType");
        for (String type : types.keySet()) {
            // Don't display a mostSpecificType that is the same as the type being displayed on the page
            if ( ! type.equals(displayedType) ) {
                typeLabels.add(types.get(type));
            }
        }
        return typeLabels;
    }

    
}
