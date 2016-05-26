/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.searchresult;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.web.ViewFinder;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IndividualSearchResult extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(IndividualSearchResult.class);

    private static Class resultClass = IndividualSearchResult.class;

    protected final VitroRequest vreq;
    protected final Individual individual;

    public IndividualSearchResult(Individual individual, VitroRequest vreq) {
        this.vreq = vreq;
        this.individual = individual;
    }

    protected String getView(ViewFinder.ClassView view) {
        ViewFinder vf = new ViewFinder(view);
        return vf.findClassView(individual, vreq);
    }

    public static List<IndividualSearchResult> getIndividualTemplateModels(List<Individual> individuals, VitroRequest vreq) {
        List<IndividualSearchResult> models = new ArrayList<IndividualSearchResult>(individuals.size());
        for (Individual individual : individuals) {
//            models.add(new IndividualSearchResult(individual, vreq));

            try {
                Constructor ctor = resultClass.getDeclaredConstructor(Individual.class, VitroRequest.class);
                models.add((IndividualSearchResult)ctor.newInstance(individual, vreq));
            } catch (NoSuchMethodException e) {
                log.error("Unable to create IndividualSearchResult", e);
            } catch (InstantiationException e) {
                log.error("Unable to create IndividualSearchResult", e);
            } catch (IllegalAccessException e) {
                log.error("Unable to create IndividualSearchResult", e);
            } catch (InvocationTargetException e) {
                log.error("Unable to create IndividualSearchResult", e);
            }
        }
        return models;
    }

    /* Template properties */

    public String getUri() {
        return individual.getURI();
    }

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

    public String getSnippet() {
        return individual.getSearchSnippet();
    }

    protected static void registerResultClass(Class clazz) {
        if (IndividualSearchResult.class.isAssignableFrom(clazz)) {
            resultClass = clazz;
        }
    }
}