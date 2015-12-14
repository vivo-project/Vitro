/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The supported media types for SPARQL queries that return RDF (i.e., CONSTRUCT
 * and DESCRIBE).
 */
public enum RdfResultMediaType {
	TEXT("text/plain", true, "NTRIPLE", "N-TRIPLE"),

	RDF_XML("application/rdf+xml", true, "RDFXML", "RDF/XML"),

	N3("text/n3", true, "N3", "N3"),

	TTL("text/turtle", false, "N3", "TTL"),

	JSON("application/json", false, "N3", "JSON"),
	
	JSON_LD("application/ld+json", false, "N3", "JSON");

	// ----------------------------------------------------------------------
	// Keep a map of content types, for easy conversion back and forth
	// ----------------------------------------------------------------------

	private final static Map<String, RdfResultMediaType> contentTypesMap = buildMap();

	private static Map<String, RdfResultMediaType> buildMap() {
		Map<String, RdfResultMediaType> map = new LinkedHashMap<>();
		for (RdfResultMediaType value : values()) {
			map.put(value.contentType, value);
		}
		return Collections.unmodifiableMap(map);
	}

	public static Collection<String> contentTypes() {
		return contentTypesMap.keySet();
	}

	public static RdfResultMediaType fromContentType(String contentType)
			throws IllegalArgumentException {
		RdfResultMediaType type = contentTypesMap.get(contentType);
		if (type == null) {
			throw new IllegalArgumentException(
					"No RdfResultMediaType has contentType='" + contentType
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
	private final String serializationFormat;

	/**
	 * What format shall we ask the resulting OntModel to write? 
	 */
	private final String jenaResponseFormat;

	private RdfResultMediaType(String contentType, boolean nativeFormat,
			String serializationFormat, String jenaResponseFormat) {
		this.contentType = contentType;
		this.nativeFormat = nativeFormat;
		this.serializationFormat = serializationFormat;
		this.jenaResponseFormat = jenaResponseFormat;
	}

	public String getContentType() {
		return contentType;
	}

	public boolean isNativeFormat() {
		return nativeFormat;
	}

	public String getSerializationFormat() {
		return serializationFormat;
	}

	public String getJenaResponseFormat() {
		return jenaResponseFormat;
	}

}
