/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Some utility methods to ease the transition from net.sf.json to Jackson.
 */
public class JacksonUtils {
	private static final String QUOTE = "\"";

	/**
	 * A "clean room" replacement for net.sf.json.util.JSONUtils.quote().
	 */
	public static String quote(String raw) {
		if (raw == null) {
			// Null string is treated like an empty string.
			return QUOTE + QUOTE;
		} else {
			return new StringBuilder(QUOTE)
					.append(JsonStringEncoder.getInstance().quoteAsString(raw))
					.append(QUOTE).toString();
		}
	}

	/**
	 * net.sf.json could parse a JSON string without throwing a checked
	 * exception. Make it so we can do the same with Jackson.
	 */
	public static JsonNode parseJson(String json) {
		try {
			return new ObjectMapper().readTree(json);
		} catch (IOException e) {
			throw new JacksonUtilsException(e);
		}
	}

	/**
	 * net.sf.json provided this method (assuming that 'values' is an array of
	 * JSON objects that contain Strings.
	 *
	 * literalValues = (List<String>) JSONSerializer.toJava(values);
	 *
	 * So here is a replacement for that.
	 */
	public static List<String> jsonArrayToStrings(ArrayNode values) {
		List<String> strings = new ArrayList<>();
		for (JsonNode node : values) {
			strings.add(node.asText());
		}
		return strings;
	}

	public static String getString(JsonNode node, String name) {
		if (node.has(name)) {
			return node.get(name).asText();
		}

		return null;
	}

	public static class JacksonUtilsException extends RuntimeException {

		public JacksonUtilsException() {
			super();
		}

		public JacksonUtilsException(String message, Throwable cause) {
			super(message, cause);
		}

		public JacksonUtilsException(String message) {
			super(message);
		}

		public JacksonUtilsException(Throwable cause) {
			super(cause);
		}

	}
}
