/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest.AUTHORIZED;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ComplexStringFormatter;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Show the details of where our RDF data is coming from. What are the
 * structures that supply the triples?
 * 
 * <pre>
 * Display like this:
 * 
 * ModelAccess for Context
 * blah-blah
 * 
 * ModelAccess for Request
 * blah-blah 
 * 
 * RDFServices
 *               CONTEXT    REQUEST
 * CONTENT       blah-blah  argle-bargle
 * CONFIGURATION balderdash obstreporous
 * 
 * Datasets  [same]
 * 
 * Models
 * Name                     ContextOnly
 * DISPLAY                  blah-blah
 * filegraph:tbox/junk.owl  bork-bork-bork
 * 
 * OntModels
 * Name                     Context        REQUEST
 * DISPLAY                  blah-blah      song-and-dance
 * filegraph:tbox/junk.owl  bork-bork-bork bogus-malarkey
 * 
 * For the request object, 
 *     Get the LANGUAGE_NEUTRAL versions, since they have fewer decorators.
 *     If the string is the same as the corresponding object in the context, show as "SAME AS CONTEXT"
 * 
 * Structure for freemarker:
 * map:
 *    modelAccess:
 *       context: text
 *       request: text
 *    rdfServices:
 *       content:
 *          context: text
 *          request: text
 *       configuration:
 *          context: text
 *          request: text
 *    datasets:
 *       [same]
 *    models:
 *       content:
 *          name1: 
 *             context: text
 *          name2: 
 *             context: text
 *          ...
 *       configuration:
 *          name3: 
 *             context: text
 *             request: text
 *          ...
 *    ontModels:
 *       name1: 
 *          context: text
 *          request: text
 *       ...
 *       
 * At the same time, write these to the log as INFO messages, without the fancy formatting.
 * </pre>
 */
public class ShowSourcesController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(ShowSourcesController.class);

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return AUTHORIZED;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		return new TemplateResponseValues("admin-showSources.ftl",
				new SourcesMap(vreq));
	}

	private static class SourcesMap extends HashMap<String, Object> {
		private static final Object SAME_AS_CONTEXT = "Same as Context";

		private final RequestModelAccess reqModels;
		private final ContextModelAccess ctxModels;

		private final SortedSet<String> contentModelNames;
		private final SortedSet<String> configurationModelNames;
		private final SortedSet<String> allModelNames;

		public SourcesMap(VitroRequest vreq) {
			this.reqModels = ModelAccess.on(vreq);
			this.ctxModels = ModelAccess.on(vreq.getSession()
					.getServletContext());

			this.contentModelNames = new TreeSet<>(ctxModels
					.getModelMaker(CONTENT).listModels().toList());
			this.configurationModelNames = new TreeSet<>(ctxModels
					.getModelMaker(CONFIGURATION).listModels().toList());
			this.allModelNames = new TreeSet<>(contentModelNames);
			this.allModelNames.addAll(configurationModelNames);

			addStructures(ctxModels, reqModels, "modelAccess");

			addStructures(ctxModels.getRDFService(CONTENT),
					reqModels.getRDFService(CONTENT, LANGUAGE_NEUTRAL),
					"rdfServices", CONTENT);
			addStructures(ctxModels.getRDFService(CONFIGURATION),
					reqModels.getRDFService(CONFIGURATION, LANGUAGE_NEUTRAL),
					"rdfServices", CONFIGURATION);

			addStructures(ctxModels.getDataset(CONTENT),
					reqModels.getDataset(CONTENT, LANGUAGE_NEUTRAL),
					"datasets", CONTENT);
			addStructures(ctxModels.getDataset(CONFIGURATION),
					reqModels.getDataset(CONFIGURATION, LANGUAGE_NEUTRAL),
					"datasets", CONFIGURATION);

			for (String modelName : contentModelNames) {
				addContextModel(
						ctxModels.getModelMaker(CONTENT).getModel(modelName),
						"models", CONTENT, ToString.modelName(modelName));
			}
			for (String modelName : configurationModelNames) {
				addContextModel(ctxModels.getModelMaker(CONFIGURATION)
						.getModel(modelName), "models", CONFIGURATION,
						ToString.modelName(modelName));
			}

			for (String modelName : allModelNames) {
				addStructures(ctxModels.getOntModel(modelName),
						reqModels.getOntModel(modelName, LANGUAGE_NEUTRAL),
						"ontModels", ToString.modelName(modelName));
			}
		}

		private void addStructures(Object contextDataStructure,
				Object requestDataStructure, Object... keys) {
			Map<String, Object> map = followPath(keys);
			map.put("context", formatStructure(contextDataStructure));

			if (String.valueOf(contextDataStructure).equals(
					String.valueOf(requestDataStructure))) {
				map.put("request", SAME_AS_CONTEXT);
			} else {
				map.put("request", formatStructure(requestDataStructure));
			}

			writeToLog(keys, "context", contextDataStructure);
			writeToLog(keys, "request", requestDataStructure);
		}

		private void addContextModel(Model model, Object... keys) {
			Map<String, Object> map = followPath(keys);
			map.put("context", formatStructure(model));
			writeToLog(keys, "context", model);
		}

		/**
		 * Get the inner map that corresponds to this set of keys. If there is
		 * no such map, create one.
		 */
		@SuppressWarnings("unchecked")
		private Map<String, Object> followPath(Object[] keys) {
			Map<String, Object> m = this;
			for (Object key : keys) {
				String stringKey = String.valueOf(key);
				if (!m.containsKey(stringKey)) {
					m.put(stringKey, new TreeMap<String, Object>());
				}
				m = (Map<String, Object>) m.get(stringKey);
			}
			return m;
		}

		private void writeToLog(Object[] keys, String lastKey,
				Object dataStructure) {
			List<Object> allKeys = new ArrayList<>();
			allKeys.addAll(Arrays.asList(keys));
			allKeys.add(lastKey);
			log.info("Data structure: " + allKeys + " "
					+ dsToString(dataStructure));
		}

		private String dsToString(Object dataStructure) {
			if (dataStructure instanceof OntModel) {
				return ToString.ontModelToString((OntModel) dataStructure);
			} else if (dataStructure instanceof Model) {
				return ToString.modelToString((Model) dataStructure);
			} else if (dataStructure instanceof Graph) {
				return ToString.graphToString((Graph) dataStructure);
			} else if (dataStructure instanceof RequestModelAccess
					|| dataStructure instanceof ContextModelAccess) {
				return ToString
						.replaceModelNames(String.valueOf(dataStructure));
			} else {
				return String.valueOf(dataStructure);
			}
		}

		private String formatStructure(Object dataStructure) {
			String dsString = dsToString(dataStructure);
			if (dataStructure instanceof RequestModelAccess
					|| dataStructure instanceof ContextModelAccess) {
				return new ComplexStringFormatter(dsString, ".   ").toString();
			} else {
				return new ComplexStringFormatter(dsString).toString();
			}
		}

	}
}
