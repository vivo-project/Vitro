/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.logging;

import static edu.cornell.mannlib.vitro.webapp.servlet.setup.FileGraphSetup.FILEGRAPH_URI_ROOT;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Polyadic;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;

/**
 * Some useful methods for printing out the contents of data structures:
 * OntModels, Models, Datasets, etc.
 */
public class ToString {
	/**
	 * Local implementations of OntModel can display themselves. Built-in Jena
	 * OntModels will show their hashcodes, base models, and sub-models.
	 */
	public static String ontModelToString(OntModel ontModel) {
		if (ontModel == null || isVitroClass(ontModel)) {
			return String.valueOf(ontModel);
		} else {
			Model base = ontModel.getBaseModel();
			Graph baseGraph = base.getGraph();
			List<Graph> subGraphs = ontModel.getSubGraphs();
			return simpleName(ontModel) + "[" + hashHex(ontModel) + ", base="
					+ modelToString(base) + ", subgraphs="
					+ subGraphsToString(subGraphs, baseGraph) + "]";
		}
	}

	/** Show the sub-graphs, except for the base graph. */
	private static String subGraphsToString(Collection<Graph> subGraphs,
			Graph baseGraph) {
		Set<Graph> set = new HashSet<>(subGraphs);
		set.remove(baseGraph);
		return setOfGraphsToString(set);
	}

	private static String setOfGraphsToString(Set<Graph> set) {
		Set<String> strings = new HashSet<>();
		for (Graph g : set) {
			strings.add(graphToString(g));
		}
		return "[" + StringUtils.join(strings, ", ") + "]";
	}

	/**
	 * Local implementations of Model can display themselves. Built-in Jena
	 * Graphs will show their hashcodes and graphs.
	 */
	public static String modelToString(Model model) {
		if (model == null || isVitroClass(model)) {
			return String.valueOf(model);
		} else {
			return simpleName(model) + "[" + hashHex(model) + ", base="
					+ graphToString(model.getGraph()) + "]";
		}
	}

	/**
	 * Local implementations of Graph can display themselves. Built-in Jena
	 * Graphs will show their hashcodes.
	 */
	public static String graphToString(Graph graph) {
		if (graph == null || isVitroClass(graph)) {
			return String.valueOf(graph);
		} else if (graph instanceof Polyadic) {
			return polyadicGraphToString((Polyadic) graph);
		} else {
			return simpleName(graph) + "[" + hashHex(graph) + "]";
		}
	}

	private static String polyadicGraphToString(Polyadic poly) {
		Graph baseGraph = poly.getBaseGraph();
		List<Graph> subGraphs = poly.getSubGraphs();
		return simpleName(poly) + "[" + hashHex(poly) + ", base="
				+ graphToString(baseGraph) + ", subgraphs="
				+ subGraphsToString(subGraphs, baseGraph) + "]";
	}

	/**
	 * If the string is found in ModelNames, return the name of the constant.
	 * 
	 * If the name is a filegraph, convert it to filegraph:[suffix]
	 * 
	 * Otherwise, return the string itself.
	 */
	public static String modelName(String name) {
		if (name == null) {
			return "null";
		}
		for (Entry<String, String> entry : ModelNames.namesMap.entrySet()) {
			if (name.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		if (name.startsWith(FILEGRAPH_URI_ROOT)) {
			return "filegraph:" + name.substring(FILEGRAPH_URI_ROOT.length());
		}
		return name;
	}

	/**
	 * Replace all Model URIs with their short names. If the filegraph URI root
	 * is found, replace it with "filegraph:".
	 */
	public static String replaceModelNames(String raw) {
		String s = raw;
		for (Entry<String, String> entry : ModelNames.namesMap.entrySet()) {
			s = s.replace(entry.getValue(), entry.getKey());
		}
		return s.replace(FILEGRAPH_URI_ROOT, "filegraph:");
	}

	public static boolean isVitroClass(Object o) {
		return (o == null) ? false : o.getClass().getName()
				.startsWith("edu.cornell");
	}

	public static String simpleName(Object o) {
		return (o == null) ? "null" : o.getClass().getSimpleName();
	}

	public static String hashHex(Object o) {
		return (o == null) ? "@00000000" : String.format("@%08x", o.hashCode());
	}

	/**
	 * This class contains only static methods. No need for an instance.
	 */
	private ToString() {
		// Nothing to initialize.
	}

}
