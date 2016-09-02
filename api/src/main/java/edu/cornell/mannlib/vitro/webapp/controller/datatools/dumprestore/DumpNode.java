/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonObject;
import javax.json.JsonString;

import org.apache.jena.atlas.lib.EscapeStr;


/**
 * A representation of an RDF Node, read from one format of a result set, and
 * able to write to a different format.
 */
public abstract class DumpNode {
	public static DumpNode fromJson(JsonObject json) throws BadNodeException {
		if (json == null) {
			return new DumpNullNode();
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

	private static final String PATTERN_NQUAD_URI = "<(.+)>";
	private static final String PATTERN_NQUAD_LITERAL = "" //
			+ "\"(.*)\"" // quoted string
			+ "(@(.+))?" // optional language specifier
			+ "(\\^\\^<(.+)>)?" // optional datatype
	;
	private static final String PATTERN_NQUAD_BLANK = "_:(.+)";

	public static DumpNode fromNquad(String text) throws BadNodeException {
		if (text == null) {
			return new DumpNullNode();
		}

		text = text.trim();
		if (text.isEmpty()) {
			return new DumpNullNode();
		}

		Matcher m1 = Pattern.compile(PATTERN_NQUAD_URI).matcher(text);
		Matcher m2 = Pattern.compile(PATTERN_NQUAD_LITERAL).matcher(text);
		Matcher m3 = Pattern.compile(PATTERN_NQUAD_BLANK).matcher(text);

		if (m1.matches()) {
			return new DumpUriNode(unescape(m1.group(1)));
		} else if (m2.matches()) {
			return new DumpLiteralNode(unescape(m2.group(1)),
					unescape(m2.group(3)), unescape(m2.group(5)));
		} else if (m3.matches()) {
			return new DumpBlankNode(unescape(m3.group(1)));
		} else {
			throw new BadNodeException("Can't parse node: '" + text + "'");
		}
	}

	private static String unescape(String s) {
		return (s == null) ? null : EscapeStr.unescapeStr(s);
	}

	private static String getString(JsonObject json, String name) {
		JsonString jsString = json.getJsonString(name);
		return (jsString == null) ? null : json.getString(name);
	}

	public abstract String getValue();

	public abstract String toNquad();

	public boolean isNull() {
		return false;
	}

	public boolean isUri() {
		return false;
	}

	public boolean isLiteral() {
		return false;
	}

	public boolean isBlank() {
		return false;
	}

	public static class DumpNullNode extends DumpNode {
		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public String getValue() {
			return null;
		}

		@Override
		public String toNquad() {
			return "";
		}
	}

	public static class DumpUriNode extends DumpNode {
		private final String uri;

		private DumpUriNode(String uri) throws BadNodeException {
			if (uri == null) {
				throw new BadNodeException("uri may not be null.");
			}
			this.uri = uri;
		}

		@Override
		public boolean isUri() {
			return true;
		}

		@Override
		public String getValue() {
			return uri;
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

		private DumpLiteralNode(String value, String language, String datatype)
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
		public boolean isLiteral() {
			return true;
		}

		@Override
		public String getValue() {
			return value;
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

		private DumpBlankNode(String label) throws BadNodeException {
			if (label == null) {
				throw new BadNodeException("label may not be null.");
			}
			this.label = label;
		}

		@Override
		public boolean isBlank() {
			return true;
		}

		@Override
		public String getValue() {
			return label;
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
