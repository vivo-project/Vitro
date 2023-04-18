package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.LABEL_DISPLAY_SUFFIX;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.LABEL_SORT_SUFFIX;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.DATE_RANGE_SUFFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.SimpleOrderedMap;

public class SolrFieldInitializer {

    private static Log log = LogFactory.getLog(SolrFieldInitializer.class);

    private static final String DATE_RANGE_FIELD_TYPE = "dateRange";

    static void initializeFields(SolrClient queryEngine, ConcurrentUpdateSolrClient updateEngine) throws Exception {
        initializeStringFields(queryEngine, updateEngine);
        initializeDateRangeFields(queryEngine, updateEngine);
    }

    private static void initializeStringFields(SolrClient queryEngine, ConcurrentUpdateSolrClient updateEngine)
            throws Exception {
        Set<String> fieldSuffixes = new HashSet<>(Arrays.asList(LABEL_SORT_SUFFIX, LABEL_DISPLAY_SUFFIX));
        excludeMatchedFields(fieldSuffixes, queryEngine, "dynamicFields");
        excludeMatchedFields(fieldSuffixes, queryEngine, "fields");
        createStringFields(fieldSuffixes, updateEngine);
    }

    private static void createStringFields(Set<String> fieldSuffixes, ConcurrentUpdateSolrClient updateEngine)
            throws Exception {
        for (String suffix : fieldSuffixes) {
            Map<String, Object> fieldAttributes = getFieldAttributes(suffix);
            SchemaRequest.AddDynamicField request = new SchemaRequest.AddDynamicField(fieldAttributes);
            SchemaResponse.UpdateResponse response = request.process(updateEngine);
            if (response.getStatus() != 0) {
                throw new Exception("Creation of missing solr field '*" + suffix + "' failed");
            }
            log.info("Solr dynamic field '*" + suffix + "' has been created.");
        }
    }

    private static Map<String, Object> getFieldAttributes(String suffix) {
        Map<String, Object> fieldAttributes = new HashMap<String, Object>();
        fieldAttributes.put("type", "string");
        fieldAttributes.put("stored", "true");
        fieldAttributes.put("indexed", "true");
        fieldAttributes.put("name", "*" + suffix);
        return fieldAttributes;
    }

    private static void excludeMatchedFields(Set<String> fieldSuffixes, SolrClient queryEngine, String fieldType)
            throws Exception {
        SolrQuery query = new SolrQuery();
        query.add(CommonParams.QT, "/schema/" + fieldType.toLowerCase());
        QueryResponse response = queryEngine.query(query);
        ArrayList<SimpleOrderedMap> fieldList = (ArrayList<SimpleOrderedMap>) response.getResponse().get(fieldType);
        if (fieldList == null) {
            return;
        }
        Set<String> it = new HashSet<>(fieldSuffixes);
        for (String target : it) {
            for (SimpleOrderedMap field : fieldList) {
                String fieldName = (String) field.get("name");
                if (fieldName.endsWith(target)) {
                    fieldSuffixes.remove(target);
                }
            }
        }
    }

    private static void initializeDateRangeFields(SolrClient queryEngine, ConcurrentUpdateSolrClient updateEngine)
            throws Exception {
        if (!isFieldTypeExists(DATE_RANGE_FIELD_TYPE, queryEngine, updateEngine)) {
            log.info("Solr field type '" + DATE_RANGE_FIELD_TYPE + "' not found. Trying to create.");
            createDateRangeFieldType(updateEngine);
        }
        if (!isDynamicFieldExists("*" + DATE_RANGE_SUFFIX, queryEngine, updateEngine)) {
            log.info("Solr dynamic field '*_drsim' of type dateRange not found. Trying to create.");
            createDateRangeField(DATE_RANGE_SUFFIX, updateEngine);
        }
    }

    private static boolean isFieldTypeExists(String typeName, SolrClient queryEngine,
            ConcurrentUpdateSolrClient updateEngine) throws Exception {
        try {
            SolrQuery query = new SolrQuery();
            query.add(CommonParams.QT, "/schema/fieldtypes");
            QueryResponse response = queryEngine.query(query);
            ArrayList<SimpleOrderedMap> typeList = (ArrayList<SimpleOrderedMap>) response.getResponse()
                    .get("fieldTypes");
            if (typeList == null) {
                return false;
            }
            for (SimpleOrderedMap type : typeList) {
                String name = (String) type.get("name");
                if (name.equals(typeName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return false;
    }

    private static boolean isDynamicFieldExists(String fieldName, SolrClient queryEngine,
            ConcurrentUpdateSolrClient updateEngine) throws Exception {
        try {
            SolrQuery query = new SolrQuery();
            query.add(CommonParams.QT, "/schema/dynamicfields");
            QueryResponse response = queryEngine.query(query);
            ArrayList<SimpleOrderedMap> fieldList = (ArrayList<SimpleOrderedMap>) response.getResponse()
                    .get("dynamicFields");
            if (fieldList == null) {
                return false;
            }
            for (SimpleOrderedMap field : fieldList) {
                String name = (String) field.get("name");
                if (name.equals(fieldName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return false;
    }

    private static void createDateRangeFieldType(ConcurrentUpdateSolrClient updateEngine) throws Exception {
        SchemaRequest.AddFieldType fieldTyperequest = new SchemaRequest.AddFieldType(getDateRangeFieldTypeDefinition());
        SchemaResponse.UpdateResponse response = fieldTyperequest.process(updateEngine);
        if (response.getStatus() != 0) {
            throw new Exception("Creation of missing solr field type 'dateRange' failed");
        }
        log.info("Solr field type 'dateRange' has been created.");
    }

    private static void createDateRangeField(String suffix, ConcurrentUpdateSolrClient updateEngine) throws Exception {
        Map<String, Object> fieldAttributes = getDateRangeFieldAttributes(suffix);
        SchemaRequest.AddDynamicField dynamicFieldrequest = new SchemaRequest.AddDynamicField(fieldAttributes);
        SchemaResponse.UpdateResponse response = dynamicFieldrequest.process(updateEngine);
        if (response.getStatus() != 0) {
            throw new Exception("Creation of missing solr field '*" + suffix + "' failed");
        }
        log.info("Solr dateRange dynamic field '*_drsim' has been created.");
    }

    private static FieldTypeDefinition getDateRangeFieldTypeDefinition() {
        Map<String, Object> fieldAttributes = new HashMap<String, Object>();
        fieldAttributes.put("class", "solr.DateRangeField");
        fieldAttributes.put("name", DATE_RANGE_FIELD_TYPE);
        FieldTypeDefinition ftd = new FieldTypeDefinition();
        ftd.setAttributes(fieldAttributes);
        return ftd;
    }

    private static Map<String, Object> getDateRangeFieldAttributes(String suffix) {
        Map<String, Object> fieldAttributes = new HashMap<String, Object>();
        fieldAttributes.put("type", DATE_RANGE_FIELD_TYPE);
        fieldAttributes.put("stored", "true");
        fieldAttributes.put("indexed", "true");
        fieldAttributes.put("name", "*" + suffix);
        return fieldAttributes;
    }

}
