/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

/**
 * The nuts and bolts of adding a document to the Elasticsearch index
 */
public class ESAdder {
    private static final Log log = LogFactory.getLog(ESAdder.class);

    private final String baseUrl;

    public ESAdder(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void add(Collection<SearchInputDocument> docs)
            throws SearchEngineException {
        for (SearchInputDocument doc : docs) {
            addDocument(doc);
        }
    }

    private void addDocument(SearchInputDocument doc)
            throws SearchEngineException {
        try {
            Map<String, List<Object>> map = convertDocToMap(doc);

            if (map.containsKey(VitroSearchTermNames.NAME_RAW)) {
                map.putIfAbsent(VitroSearchTermNames.AC_NAME_STEMMED, map.get(VitroSearchTermNames.NAME_RAW));
                map.putIfAbsent(VitroSearchTermNames.AC_NAME_UNTOKENIZED, map.get(VitroSearchTermNames.NAME_RAW));
            }

            String json = new ObjectMapper().writeValueAsString(map);
            if (json.contains("_drsim")) {
                json = reformatDRSIMFields(json);
            }
            log.debug("Adding document for '" + doc.getField("DocId") + "': "
                    + json);

            putToElastic(json, (String) doc.getField("DocId").getFirstValue());
        } catch (Exception e) {
            throw new SearchEngineException("Failed to convert to JSON", e);
        }
    }

    /**
     * Some field values are collections. Add the members of the collection
     * instead.
     */
    private Map<String, List<Object>> convertDocToMap(SearchInputDocument doc) {
        Map<String, List<Object>> map = new HashMap<>();
        for (SearchInputField field : doc.getFieldMap().values()) {
            ArrayList<Object> list = new ArrayList<>();
            for (Object value : field.getValues()) {
                if (value instanceof Collection) {
                    Collection<?> cValue = (Collection<?>) value;
                    list.addAll(cValue);
                } else {
                    list.add(value);
                }
            }
            map.put(field.getName(), list);
        }
        return map;
    }

    private String reformatDRSIMFields(String json) {
        String patternString = "\\[(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z) TO (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)]";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(json);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String dateStart = matcher.group(1);
            String dateEnd = matcher.group(2);

            String replacement = String.format("{\"gte\": \"%s\", \"lte\": \"%s\"}", dateStart, dateEnd)
                .replace("{", "\\{")
                .replace("}", "\\}");

            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString().replace("[\"{", "{").replace("}\"]", "}");
    }

    private void putToElastic(String json, String docId)
            throws SearchEngineException {
        try {
            String url = baseUrl + "/_doc/"
                    + URLEncoder.encode(docId, "UTF8");
            HttpClient httpClient = ESHttpBasicClientFactory.getHttpClient(baseUrl);

            HttpPut request = new HttpPut(url);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() >= 400) {
                log.warn("Response from Elasticsearch: "
                    + EntityUtils.toString(response.getEntity()));
            } else {
                log.debug("Response from Elasticsearch: "
                    + EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            throw new SearchEngineException("Failed to put to Elasticsearch",
                    e);
        }
    }
}
