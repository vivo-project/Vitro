/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.StringResultsMapping;

/**
 * A variation on SelectQueryDocument where the target field of the search
 * index document is specified in the query results.
 *
 * Each query should contain a ?uri variable, which will be replaced by the URI
 * of the individual.
 * 
 * Each query must return a ?targetField variable specifying the name of the 
 * search document field to be populated. 
 *
 * All of the other result fields in each row of each query will
 * be converted to strings and added to the field specified in ?targetField.
 *
 */
public class SelectQueryDocumentModifierDynamicTargetField
        extends SelectQueryDocumentModifier 
        implements DocumentModifier, ContextModelsUser {
    private static final Log log = LogFactory
            .getLog(SelectQueryDocumentModifierDynamicTargetField.class);
    
    private static final String TARGET_FIELD_VAR = "targetField";

    @Override
    /**
     * Grab the un-flattened query solution mappings and use the field name
     * specified in the TARGET_FIELD_VAR variable as the location to store the 
     * rest of the values.
     */
    public void modifyDocument(Individual ind, SearchInputDocument doc) {
        if (passesTypeRestrictions(ind)) {
            List<StringResultsMapping> values = getMappingsForQueries(ind);
            for(StringResultsMapping value : values) {
                for(Map<String, String> map : value.getListOfMaps()) {
                    String targetFieldName = map.get(TARGET_FIELD_VAR);
                    if(targetFieldName == null) {
                        log.error(label + " select query must return variable "
                            + TARGET_FIELD_VAR + " to specify document field"
                                    + " in which to store remaining values");
                    } else {
                        for(String key : map.keySet()) {
                            if(!TARGET_FIELD_VAR.equals(key)) {
                                doc.addField(targetFieldName, map.get(key));
                                if(log.isDebugEnabled()) {
                                    log.debug("Added field " + targetFieldName
                                            + " value " + map.get(key) + " to "
                                            + ind.getURI());
                                }
                            }                                                       
                        }
                    }
                }
            }
        }
    }

    protected List<StringResultsMapping> getMappingsForQueries(Individual ind) {
        List<StringResultsMapping> list = new ArrayList<>();
        for (String query : queries) {
            list.add(getQueryResults(query, ind));
        }
        return list;
    }

    protected StringResultsMapping getQueryResults(String query, Individual ind) {
        try {
            QueryHolder queryHolder = new QueryHolder(query).bindToUri("uri",
                    ind.getURI());
            StringResultsMapping mapping = createSelectQueryContext(rdfService,
                    queryHolder).execute().toStringFields();
            log.debug(label + " query: '" + query + "' returns " + mapping);
            return mapping;
        } catch (Throwable t) {
            log.error("problem while running query '" + query + "'", t);
            return StringResultsMapping.EMPTY;
        }
    }

}
