/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.IndividualFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputField;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JSONFilesLoader;

public class JsonFacetsModifier implements DocumentModifier {
	
    private static final Log log = LogFactory.getLog(JsonFacetsModifier.class);

    @Override
    public void modifyDocument(Individual individual, SearchInputDocument doc) {
        indexIndividual(individual, doc);
    }

    @Override
    public void shutdown() {
        // do nothing.
    }

    public static synchronized SearchInputDocument addFacetsOrder(String keyText, SearchInputDocument doc) {

        if (keyText.contains("_facets")) {

            String keyTextOrder = keyText.replace("_facets", "_facetsOrder");

            Collection<Object> cc = doc.getField(keyText).getValues();

            Object keyTextOrderValue = null;

            if (cc.size() > 0) {
                keyTextOrderValue = cc.iterator().next();
            }

            BaseSearchInputField searchInputFieldkeyTextOrder = (BaseSearchInputField) doc.getField(keyTextOrder);
            if (searchInputFieldkeyTextOrder != null) {
                searchInputFieldkeyTextOrder.setValue(keyTextOrderValue);
                doc.addField(searchInputFieldkeyTextOrder);
            } else {
                doc.addField(keyTextOrder, (Object) keyTextOrderValue);
            }

        }

        return doc;

    }

    public static synchronized SearchInputDocument addFacetsCount(String keyText, SearchInputDocument doc) {
        String keyTextCount = keyText.replace("_facets", "_facetsCount");

        Collection<Object> cc = doc.getField(keyText).getValues();
        int keyTextSize = cc.size();

        BaseSearchInputField searchInputFieldkeyTextCount = (BaseSearchInputField) doc.getField(keyTextCount);
        if (searchInputFieldkeyTextCount != null) {
            searchInputFieldkeyTextCount.setValue(keyTextSize);
            doc.addField(searchInputFieldkeyTextCount);
        } else {
            doc.addField(keyTextCount, (Object) keyTextSize);
        }

        return doc;
    }

    public static SearchInputDocument indexIndividualFacet(String value, SearchInputDocument doc, String solr) {

        String key = solr.replaceAll(" ", "-") + "_facets";

        SearchInputField searchInputField = doc.getField(key);

        boolean exists = false;
        if (searchInputField != null) {
            Collection<Object> solrdocValues = searchInputField.getValues();
            for (Object s : solrdocValues) {
                if (s.equals(value)) {
                    exists = true;
                }
            }
        }
        if (!exists) {
            doc.addField(key, value);
            doc = addFacetsCount(key, doc);
            doc = addFacetsOrder(key, doc);
        }
        return doc;
    }

    public static SearchInputDocument indexIndividualUriFacet(String valueUri, String prop, String value,
            SearchInputDocument doc, String solr) {

        String key = solr.replaceAll(" ", "-") + "_facets";
        String keyUri = solr.replaceAll(" ", "-") + "_uri_facets";

        // facets
        SearchInputField searchInputField = doc.getField(keyUri);
        boolean exists = false;
        if (searchInputField != null) {
            Collection<Object> solrdocValues = searchInputField.getValues();
            for (Object s : solrdocValues) {
                if (s.equals(value)) {
                    exists = true;
                }
            }
        }
        if (!exists) {
            doc.addField(key, value);
            doc.addField(keyUri, valueUri);
            doc = addFacetsCount(key, doc);
            doc = addFacetsOrder(key, doc);
        }

        return doc;

    }

    public static SearchInputDocument indexIndividualData(Individual rootIndividual, Individual ind, String prop,
            String regex, String regexreplace, SearchInputDocument doc, String solr) throws JSONException {

        List<String> dataValues = ind.getDataValues(prop);

        for (String value : dataValues) {

            if (regex != null) {
                Matcher m = Pattern.compile(regex).matcher(value);
                if (m.matches()) {
                    value = m.replaceAll(regexreplace);
                }
            }
            log.debug("            solr doc: "+value);
            if (rootIndividual.getURI().equals(ind.getURI())) {
                doc = indexIndividualFacet(value, doc, solr);
            } else {
                doc = indexIndividualUriFacet(ind.getURI(), prop, value, doc, solr);
            }

        }

        return doc;
    }

    public static SearchInputDocument indexIndividualRegexpUri(Individual rootIndividual, Individual ind, String prop,
            String regex, String regexreplace, SearchInputDocument doc, String solr) throws JSONException {

        Matcher m = Pattern.compile(regex).matcher(ind.getURI());
        if (m.matches()) {
            String value = m.replaceAll(regexreplace);
            log.debug("            solr doc: "+value);
            if (rootIndividual.getURI().equals(ind.getURI())) {
                doc = indexIndividualFacet(value, doc, solr);
            } else {
                doc = indexIndividualUriFacet(ind.getURI(), ind.getURI(), value, doc, solr);
            }
        }

        return doc;
    }

    public static List<Individual> getIndividualObjects(Individual ind, String propr, String qualifiedBy)
            throws JSONException {

        log.debug("    search for propr: " + propr + " qualifiedBy " + qualifiedBy);

        List<ObjectPropertyStatement> objectPropertyStatements = ind.getObjectPropertyStatements(propr);
        List<Individual> objectList = new ArrayList<Individual>();
        for (ObjectPropertyStatement op : objectPropertyStatements) {

            if (op.getProperty() instanceof ObjectProperty) {

                Individual individual = op.getObject();

                List<VClass> vclassL = individual.getVClasses();

                boolean qualifiedByBoolean = false;
                
                if (qualifiedBy == null) {
                    qualifiedByBoolean = true;
                }else {
                
                for (VClass classiLev2 : vclassL) {
                    if (classiLev2.getURI().equals(qualifiedBy)) {
                        qualifiedByBoolean = true;
                    }
                }
                
                }

                if (qualifiedByBoolean) {
                    log.debug("        related ojb: " + op.getObjectURI());
                    objectList.add(individual);
                }

            }
        }

        return objectList;
    }

    public static SearchInputDocument indexIndividualObjectOrData(Individual rootIndividual, Individual individual,
            FacetConfig fLev1, SearchInputDocument doc, String solr) throws JSONException {

        if (fLev1.pivotsIndexProperty == null) {
            // no more nested props

            if (fLev1.configContextFor != null) {
                doc = indexIndividualData(rootIndividual, individual, fLev1.configContextFor, fLev1.regexp,
                        fLev1.regexpreplace, doc, solr);
            } else {

                if (fLev1.regexp != null) {
                    doc = indexIndividualRegexpUri(rootIndividual, individual, fLev1.configContextFor, fLev1.regexp,
                            fLev1.regexpreplace, doc, solr);
                } else {
                    log.error("json config error ");
                }
            }

        } else {

            // get nested
            List<Individual> objListLev1 = getIndividualObjects(individual, fLev1.configContextFor, fLev1.qualifiedBy);

            for (Individual indsdbLev1 : objListLev1) {
                FacetConfig fLev2 = new FacetConfig(fLev1.pivotsIndexProperty);
                doc = indexIndividualObjectOrData(rootIndividual, indsdbLev1, fLev2, doc, solr);
            }
        }
        return doc;
    }

    public static String getValue(JSONObject jo, String key) {
        String value = null;
        if (jo.has(key))
            value = jo.getString(key);
        return value;
    }

    public static SearchInputDocument indexIndividual(Individual ind, SearchInputDocument doc) throws JSONException {

        JSONArray indxProp = JSONFilesLoader.pivotsIndexProperty;

        for (int j = 0; j < indxProp.length(); j++) {

            JSONObject jo = indxProp.getJSONObject(j);
            String qualifiedByDomain = getValue(jo, "qualifiedByDomain");
            String solr = getValue(jo, "solr");

            FacetConfig fLev1 = new FacetConfig(jo);

            IndividualSDB indsdb = ((IndividualSDB) ((IndividualFiltering) ind).get_innerIndividual());
            List<VClass> vclassL = indsdb.getVClasses();

            boolean qualifiedByDomainBoolean = false;

            for (VClass classi : vclassL) {
                if (classi.getURI().equals(qualifiedByDomain)) {
                    qualifiedByDomainBoolean = true;
                }
            }

            if (qualifiedByDomainBoolean) {
                log.debug("\nJsonFacetsModifier, individual: " + ind.getURI() + ", Domain: " + qualifiedByDomain+ ", prop: "+fLev1.configContextFor);
                doc = indexIndividualObjectOrData(indsdb, indsdb, fLev1, doc, solr);
            }

        }

        return doc;
    }

}
