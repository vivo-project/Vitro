/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The supported media types for SPARQL queries that return a Result Set (i.e.,
 * SELECT and ASK).
 */
public enum ResultSetMediaType {
	TEXT("text/plain", true, "TEXT", null, "txt"),

	CSV("text/csv", true, "CSV", null, "csv"),

	TSV("text/tab-separated-values", false, "CSV", "tsv", "tsv"),

	XML("application/sparql-results+xml", true, "XML", null, "xml"),

	JSON("application/sparql-results+json", true, "JSON", null, "json");

	// ----------------------------------------------------------------------
	// Keep a map of content types, for easy conversion back and forth
	// ----------------------------------------------------------------------

	private final static Map<String, ResultSetMediaType> contentTypesMap = buildMap();

	private static Map<String, ResultSetMediaType> buildMap() {
		Map<String, ResultSetMediaType> map = new LinkedHashMap<>();
		for (ResultSetMediaType value : values()) {
			map.put(value.contentType, value);
		}
		return Collections.unmodifiableMap(map);
	}

	public static Collection<String> contentTypes() {
		return contentTypesMap.keySet();
	}

	public static ResultSetMediaType fromContentType(String contentType)
			throws IllegalArgumentException {
		ResultSetMediaType type = contentTypesMap.get(contentType);
		if (type == null) {
			throw new IllegalArgumentException(
					"No ResultSetMediaType has contentType='" + contentType
							+ "'");
		} else {
			return type;
		}
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	/**
	 * The MIME type as it would appear in an HTTP Accept or Content-Type
	 * header.
	 */
	private final String contentType;

	/**
	 * Is this a format that is supported directly by the RDFService?
	 */
	private final boolean nativeFormat;

	/**
	 * What format shall we ask the RDFService to supply?
	 */
	private final String rdfServiceFormat;

	/**
	 * What format shall we ask the ResultSetFormatter to output? (Applies only
	 * to non-native formats)
	 */
	private final String jenaResponseFormat;

	/**
	 * What extension should be used if file is downloaded?
	 */
	private final String extension;

	private ResultSetMediaType(String contentType, boolean nativeFormat,
			String rdfServiceFormat, String jenaResponseFormat, String extension) {
		this.contentType = contentType;
		this.nativeFormat = nativeFormat;
		this.rdfServiceFormat = rdfServiceFormat;
		this.jenaResponseFormat = jenaResponseFormat;
		this.extension = extension;
	}

	public String getContentType() {
		return contentType;
	}

	public boolean isNativeFormat() {
		return nativeFormat;
	}

	public String getRdfServiceFormat() {
		return rdfServiceFormat;
	}

	public String getJenaResponseFormat() {
		return jenaResponseFormat;
	}

	public String getExtension() {
		return extension;
	}

}
