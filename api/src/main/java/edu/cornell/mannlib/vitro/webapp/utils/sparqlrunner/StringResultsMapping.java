/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Parse the set of fieldNames against the results of the query, and make the
 * extracted values available.
 */
public class StringResultsMapping {
	public static final StringResultsMapping EMPTY = new StringResultsMapping();
	private final List<Map<String, String>> listOfMaps;

	public StringResultsMapping() {
		this.listOfMaps = Collections.emptyList();
	}

	public StringResultsMapping(ResultSet results, Set<String> fieldNames) {
		this.listOfMaps = mapResultsForQuery(results, fieldNames);
	}

	private List<Map<String, String>> mapResultsForQuery(ResultSet results,
			Set<String> fieldNames) {
		List<Map<String, String>> mapping = new ArrayList<>();
		while (results.hasNext()) {
			Map<String, String> rowMapping = mapResultsForRow(
					results.nextSolution(), fieldNames);
			if (!rowMapping.isEmpty()) {
				mapping.add(rowMapping);
			}
		}
		return mapping;
	}

	private Map<String, String> mapResultsForRow(QuerySolution row,
			Set<String> fieldNames) {
		Map<String, String> map = new HashMap<>();
		for (Iterator<String> names = row.varNames(); names.hasNext();) {
			String name = names.next();
			RDFNode node = row.get(name);
			String text = getTextForNode(node);
			if (StringUtils.isNotBlank(text)) {
				map.put(name, text);
			}
		}
		if (!fieldNames.isEmpty()) {
			map.keySet().retainAll(fieldNames);
		}
		return map;
	}

	private String getTextForNode(RDFNode node) {
		if (node == null) {
			return "";
		} else if (node.isLiteral()) {
			return node.asLiteral().getString().trim();
		} else {
			return node.toString().trim();
		}
	}

	public List<Map<String, String>> getListOfMaps() {
		List<Map<String, String>> list = new ArrayList<>();
		for (Map<String, String> map: listOfMaps) {
			list.add(new HashMap<>(map));
		}
		return list;
	}
	
	public List<String> flatten() {
		List<String> flat = new ArrayList<>();
		for (Map<String, String> map : listOfMaps) {
			flat.addAll(map.values());
		}
		return flat;
	}

	public Set<String> flattenToSet() {
		return new HashSet<>(flatten());
	}

}
