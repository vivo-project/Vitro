/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;

public class ChildVClassesWithParent implements FieldOptions {

    private static final String LEFT_BLANK = "";
    String fieldName;
    String classUri;
    String defaultOptionLabel = null;
    private  I18nBundle i18n;

    public ChildVClassesWithParent(String classUri) throws Exception {
        super();

        if (classUri==null || classUri.equals(""))
            throw new Exception ("vclassUri not set");

        this.classUri = classUri;
    }

    public ChildVClassesWithParent setDefaultOption(String label){
        this.defaultOptionLabel = label;
        return this;
    }

/*
 * UQAM
 * This method is polymorphism of getOptions(EditConfigurationVTwo editConfig,String fieldName, WebappDaoFactory wDaoFact)
 * for the internationalization of word "other" in the scroling list of personHasAdvisorRelationship.ftl
 */
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig,
            String fieldName,
            VitroRequest vreq) throws Exception {
//            WebappDaoFactory wDaoFact) throws Exception {
    	this.i18n = I18n.bundle(vreq);
        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();
        // first test to see whether there's a default "leave blank" value specified with the literal options
        if ( ! StringUtils.isEmpty( defaultOptionLabel ) ){
            optionsMap.put(LEFT_BLANK, defaultOptionLabel);
        }
        String other_i18n = i18n.text("other");
        // first character in capital
        optionsMap.put(classUri, other_i18n.substring(0, 1).toUpperCase() + other_i18n.substring(1));
        WebappDaoFactory wDaoFact = vreq.getWebappDaoFactory();
        VClassDao vclassDao = wDaoFact.getVClassDao();
        List<String> subClassList = vclassDao.getAllSubClassURIs(classUri);
        if (subClassList != null && subClassList.size() > 0) {
            for (String subClassUri : subClassList) {
                VClass subClass = vclassDao.getVClassByURI(subClassUri);
                if (subClass != null && !OWL.Nothing.getURI().equals(subClassUri)) {
                    optionsMap.put(subClassUri, subClass.getName().trim());
                }
            }
        }
       return optionsMap;
    }

    public Comparator<String[]> getCustomComparator() {
    	return null;
    }

    /*
     * (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldOptions#getOptions(edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo, java.lang.String, edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory)
     *  UQAM this version is a not internationalized use this method (see line below) more often
     *  getOptions(EditConfigurationVTwo editConfig, String fieldName, VitroRequest vreq)
     */
    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig,
            String fieldName,
            WebappDaoFactory wDaoFact) throws Exception {

        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();
        // first test to see whether there's a default "leave blank" value specified with the literal options
        if ( ! StringUtils.isEmpty( defaultOptionLabel ) ){
            optionsMap.put(LEFT_BLANK, defaultOptionLabel);
        }

        optionsMap.put(classUri, "Other");

        VClassDao vclassDao = wDaoFact.getVClassDao();
        List<String> subClassList = vclassDao.getAllSubClassURIs(classUri);
        if (subClassList != null && subClassList.size() > 0) {
            for (String subClassUri : subClassList) {
                VClass subClass = vclassDao.getVClassByURI(subClassUri);
                if (subClass != null && !OWL.Nothing.getURI().equals(subClassUri)) {
                    optionsMap.put(subClassUri, subClass.getName().trim());
                }
            }
        }
       return optionsMap;
    }


}
