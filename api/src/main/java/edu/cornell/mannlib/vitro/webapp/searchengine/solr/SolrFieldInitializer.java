package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.LABEL_DISPLAY_SUFFIX;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.SORT_SUFFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.SimpleOrderedMap;

public class SolrFieldInitializer {

	static void initializeFields(SolrClient queryEngine, ConcurrentUpdateSolrClient updateEngine) throws Exception {
		Set<String> fieldSuffixes = new HashSet<>(Arrays.asList(SORT_SUFFIX, LABEL_DISPLAY_SUFFIX));
		excludeMatchedFields(fieldSuffixes, queryEngine, "dynamicFields");
		excludeMatchedFields(fieldSuffixes, queryEngine, "fields");
		createMissingFields(fieldSuffixes, updateEngine);
	}

	private static void createMissingFields(Set<String> fieldSuffixes, ConcurrentUpdateSolrClient updateEngine)
			throws Exception {
		for (String suffix : fieldSuffixes) {
			Map<String, Object> fieldAttributes = getFieldAttributes(suffix);
			SchemaRequest.AddDynamicField request = new SchemaRequest.AddDynamicField(fieldAttributes);
			SchemaResponse.UpdateResponse response = request.process(updateEngine);
			if (response.getStatus() != 0) {
				throw new Exception("Creation of missing solr field '*" + suffix + "' failed");
			}
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

}
