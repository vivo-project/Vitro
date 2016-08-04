/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createConstructQueryContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.MissingParametersException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ConstructQueryContext;

/**
 * Run the construct query more than once, each time binding one value from each
 * Iterator.
 * 
 * Specify an iterator like this: "person=http://this,http://that"
 */
public class IteratingConstructModelBuilder extends ConstructModelBuilder {
	private static final Log log = LogFactory
			.getLog(IteratingConstructModelBuilder.class);

	private Map<String, String[]> iterators = new HashMap<>();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#iterator")
	public void addIterator(String iteratorSpec)
			throws ConfigurationBeanLoaderException {
		int equalsHere = iteratorSpec.indexOf('=');
		if (equalsHere == -1) {
			throw new ConfigurationBeanLoaderException("Invalid iterator '"
					+ iteratorSpec + "', should be 'bindingName=uri1,uri2'");
		}

		String bindingName = iteratorSpec.substring(0, equalsHere).trim();
		String[] values = iteratorSpec.substring(equalsHere + 1).split(",");
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i].trim();
		}

		iterators.put(bindingName, values);
	}

	@Override
	public Model buildModel() throws DataDistributorException {
		List<Map<String, String>> iteratorValues = enumerateValues();
		Model model = ModelFactory.createDefaultModel();
		for (Map<String, String> valueMap : iteratorValues) {
			model.add(populateAndRunTheQuery(valueMap));
		}
		return model;
	}

	private List<Map<String, String>> enumerateValues() {
		List<Map<String, String>> values = new ArrayList<>();
		for (Map.Entry<String, String[]> iterator : iterators.entrySet()) {
			values = addToListOfMaps(values, iterator);
		}
		log.debug("Iterators: " + iterators);
		log.debug("Iterator values: " + values);
		return values;
	}

	private List<Map<String, String>> addToListOfMaps(
			List<Map<String, String>> list, Entry<String, String[]> entry) {
		String bindingName = entry.getKey();
		String[] values = entry.getValue();
		if (list.isEmpty()) {
			return createNewValuesList(bindingName, values);
		} else {
			return mergeWithExistingList(bindingName, values, list);
		}
	}

	private List<Map<String, String>> createNewValuesList(String bindingName,
			String[] values) {
		List<Map<String, String>> merged = new ArrayList<>();
		for (String value : values) {
			Map<String, String> map = new HashMap<>();
			map.put(bindingName, value);
			merged.add(map);
		}
		return merged;
	}

	private List<Map<String, String>> mergeWithExistingList(String bindingName,
			String[] values, List<Map<String, String>> oldList) {
		List<Map<String, String>> merged = new ArrayList<>();
		for (String value : values) {
			List<Map<String, String>> list = replicateList(oldList);
			for (Map<String, String> map : list) {
				map.put(bindingName, value);
			}
			merged.addAll(list);
		}
		return merged;
	}

	private List<Map<String, String>> replicateList(
			List<Map<String, String>> oldList) {
		List<Map<String, String>> newList = new ArrayList<>();
		for (Map<String, String> map : oldList) {
			newList.add(new HashMap<>(map));
		}
		return newList;
	}

	private Model populateAndRunTheQuery(Map<String, String> iteratorValues)
			throws MissingParametersException {
		ConstructQueryContext queryContext = createConstructQueryContext(
				models.getRDFService(), rawConstructQuery);
		queryContext = binder.bindUriParameters(uriBindingNames, queryContext);
		queryContext = binder.bindLiteralParameters(literalBindingNames,
				queryContext);
		queryContext = bindIteratorValues(queryContext, iteratorValues);
		log.debug("Query context is: " + queryContext);
		return queryContext.execute().toModel();
	}

	private ConstructQueryContext bindIteratorValues(
			ConstructQueryContext queryContext,
			Map<String, String> iteratorValues) {
		for (Map.Entry<String, String> entry : iteratorValues.entrySet()) {
			queryContext = queryContext.bindVariableToUri(entry.getKey(),
					entry.getValue());
		}
		return queryContext;
	}

}
