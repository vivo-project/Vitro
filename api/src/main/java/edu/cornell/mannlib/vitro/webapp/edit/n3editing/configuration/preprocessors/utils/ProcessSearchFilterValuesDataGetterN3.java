/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.SEARCH_FILTER_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SearchFilterValuesDataGetter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

//Returns the appropriate n3 based on data getter
public class ProcessSearchFilterValuesDataGetterN3 extends ProcessDataGetterAbstract {
    private static String classType = "java:" + SearchFilterValuesDataGetter.class.getCanonicalName();
    public static String searchFilterVarBase = "filterUri";
    private Log log = LogFactory.getLog(ProcessSearchFilterValuesDataGetterN3.class);

    public ProcessSearchFilterValuesDataGetterN3() {
    }

    public List<String> retrieveN3Required(int counter) {
        return retrieveN3ForTypeAndFilter(counter);
    }

    public List<String> retrieveN3Optional(int counter) {
        return null;
    }

    public List<String> retrieveN3ForTypeAndFilter(int counter) {
        String n3ForType = getN3ForTypePartial(counter);
        String n3 = n3ForType + "; \n" + "<" + DisplayVocabulary.SEARCH_FILTER_VALUE + "> "
                + getN3VarName(searchFilterVarBase, counter) + " .";
        List<String> n3List = new ArrayList<String>();
        n3List.add(getPrefixes() + n3);
        return n3List;
    }

    public String getN3ForTypePartial(int counter) {
        String dataGetterVar = getDataGetterVar(counter);
        String classTypeVar = getN3VarName(classTypeVarBase, counter);
        String n3 = dataGetterVar + " a " + classTypeVar;
        return n3;
    }

    public List<String> retrieveLiteralsOnForm(int counter) {
        // no literals, just the class group URI
        List<String> literalsOnForm = new ArrayList<String>();
        return literalsOnForm;
    }

    public List<String> retrieveUrisOnForm(int counter) {
        List<String> urisOnForm = new ArrayList<String>();
        urisOnForm.add(getVarName("filterUri", counter));
        urisOnForm.add(getVarName(classTypeVarBase, counter));
        return urisOnForm;
    }

    public List<FieldVTwo> retrieveFields(int counter) {
        List<FieldVTwo> fields = new ArrayList<FieldVTwo>();
        fields.add(new FieldVTwo().setName(getVarName("filterUri", counter)));
        fields.add(new FieldVTwo().setName(getVarName(classTypeVarBase, counter)));
        return fields;
    }

    public List<String> getLiteralVarNamesBase() {
        return Arrays.asList();
    }

    public List<String> getUriVarNamesBase() {
        return Arrays.asList("filterUri", classTypeVarBase);
    }

    public String getClassType() {
        return classType;
    }

    public void populateExistingValues(String dataGetterURI, int counter, OntModel queryModel) {
        // First, put dataGetterURI within scope as well
        this.populateExistingDataGetterURI(dataGetterURI, counter);
        // Put in type
        this.populateExistingClassType(this.getClassType(), counter);
        // Sparql queries for values to be executed
        // And then placed in the correct place/literal or uri
        String querystr = getExistingValuesClassGroup(dataGetterURI);
        QueryExecution qe = null;
        try {
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, queryModel);
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource classGroupResource = qs.getResource("filterUri");
                // Put both literals in existing literals
                existingUriValues.put(this.getVarName(searchFilterVarBase, counter),
                        new ArrayList<String>(Arrays.asList(classGroupResource.getURI())));
            }
        } catch (Exception ex) {
            log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
        }
    }

    protected String getExistingValuesClassGroup(String dataGetterURI) {
        String query = getSparqlPrefix() + "\n" +
            "SELECT ?filterUri ?filterName WHERE {" +
            "<" + dataGetterURI + "> <" + SEARCH_FILTER_VALUE + "> ?filterUri .\n" +
            "?filterUri <" + VitroVocabulary.LABEL + "> ?filterName .\n" +
            "}";
        return query;
    }

    public ObjectNode getExistingValuesJSON(String dataGetterURI, OntModel queryModel, ServletContext context) {
        ObjectNode jObject = new ObjectMapper().createObjectNode();
        jObject.put("dataGetterClass", classType);
        jObject.put(classTypeVarBase, classType);
        getExistingSearchFilters(dataGetterURI, jObject, queryModel);
        return jObject;
    }

    private void getExistingSearchFilters(String dataGetterURI, ObjectNode jObject, OntModel queryModel) {
        String querystr = getExistingValuesClassGroup(dataGetterURI);
        QueryExecution qe = null;
        try {
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, queryModel);
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource filterUri = qs.getResource("filterUri");
                Literal name = qs.getLiteral("filterName");
                jObject.put("searchFilterUri", filterUri.getURI());
                jObject.put("searchFilterName", name.getLexicalForm());
            }
        } catch (Exception ex) {
            log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
        }
    }

}
