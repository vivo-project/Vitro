/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import javax.json.JsonObject;
import javax.json.JsonString;

import org.apache.jena.riot.out.EscapeStr;

/**
 * A representation of an RDF Node, read from one format of a result set, and
 * able to write to a different format.
 */
public abstract class DumpNode {
	public static DumpNode fromJson(JsonObject json) throws BadNodeException {
		if (json == null) {
			return null;
		}

		String type = getString(json, "type");
		switch (type) {
		case "uri":
			return new DumpUriNode(getString(json, "value"));
		case "literal":
		case "typed-literal": // this isn't part of the spec, but Jena uses it.
			return new DumpLiteralNode(getString(json, "value"), getString(
					json, "xml:lang"), getString(json, "datatype"));
		case "bnode":
			return new DumpBlankNode(getString(json, "value"));
		default:
			throw new BadNodeException("Unrecognized type: '" + type + "'");
		}
	}

	private static String getString(JsonObject json, String name) {
		JsonString jsString = json.getJsonString(name);
		return (jsString == null) ? null : json.getString(name);
	}

	public abstract String toNquad();

	public static class DumpUriNode extends DumpNode {
		private final String uri;

		public DumpUriNode(String uri) throws BadNodeException {
			if (uri == null) {
				throw new BadNodeException("uri may not be null.");
			}
			this.uri = uri;
		}

		@Override
		public String toNquad() {
			return "<" + EscapeStr.stringEsc(uri) + ">";
		}
	}

	public static class DumpLiteralNode extends DumpNode {
		private final String value;
		private final String language;
		private final String datatype;

		public DumpLiteralNode(String value, String language, String datatype)
				throws BadNodeException {
			if (value == null) {
				throw new BadNodeException("value may not be null.");
			}
			if (language != null && datatype != null) {
				throw new BadNodeException("either language('" + language
						+ "') or datatype('" + datatype + "') must be null.");
			}
			this.value = value;
			this.language = language;
			this.datatype = datatype;
		}

		@Override
		public String toNquad() {
			String valueString = "\"" + EscapeStr.stringEsc(value) + "\"";
			if (language != null) {
				return valueString + "@" + language;
			} else if (datatype != null) {
				return valueString + "^^<" + EscapeStr.stringEsc(datatype)
						+ ">";
			} else {
				return valueString;
			}
		}
	}

	public static class DumpBlankNode extends DumpNode {
		private final String label;

		public DumpBlankNode(String label) throws BadNodeException {
			if (label == null) {
				throw new BadNodeException("label may not be null.");
			}
			this.label = label;
		}

		@Override
		public String toNquad() {
			return "_:" + label;
		}
	}

	public static class BadNodeException extends Exception {
		public BadNodeException(String message) {
			super(message);
		}

		public BadNodeException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
