/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpNode.BadNodeException;

/**
 * A utility class for NquadsParser. Breaks an NQuad line into 3 or 4 strings,
 * each holding the text representation of a node.
 * 
 * It's a little tricky where literals are involved, because white space is
 * significant, except between quotes, and quotes are only quotes if they aren't
 * escaped.
 */
class NQuadLineSplitter {
	private static final Log log = LogFactory.getLog(NQuadLineSplitter.class);

	private final String line;
	private final List<String> pieces = new ArrayList<>();
	private int i;

	public NQuadLineSplitter(String line) throws BadNodeException {
		this.line = line;
		while (!atEnd()) {
			advancePastWhiteSpace();
			switch (line.charAt(i)) {
			case '#':
				advancePastComment();
				break;
			case '<':
			case '_':
				scanUnquotedString();
				break;
			case '"':
				scanLiteralString();
				break;
			case '.':
				assureEndOfLine();
				break;
			default:
				throw new BadNodeException(
						"found unexpected character at position " + i
								+ " of line '" + line + "'");
			}
		}
	}

	private boolean atEnd() {
		return i >= line.length();
	}

	private boolean isWhiteSpace() {
		return " \t\r\n".indexOf(line.charAt(i)) >= 0;
	}

	private void advancePastWhiteSpace() {
		while (!atEnd() && isWhiteSpace()) {
			i++;
		}
	}

	private void advancePastComment() {
		i = line.length();
	}

	private void scanUnquotedString() {
		int start = i;
		while (!atEnd() && !isWhiteSpace()) {
			i++;
		}
		pieces.add(line.substring(start, i));
	}

	private void scanLiteralString() {
		int start = i;
		boolean inQuotes = false;
		while (!atEnd() && (inQuotes || !isWhiteSpace())) {
			if (isQuote()) {
				inQuotes = !inQuotes;
				log.debug("column " + i + ", inQuotes=" + inQuotes);
			}
			i++;
		}
		pieces.add(line.substring(start, i));
	}

	private void assureEndOfLine() throws BadNodeException {
		i++;
		while (!atEnd() && isWhiteSpace()) {
			i++;
		}
		if (atEnd()) {
			return;
		} else if (line.charAt(i) == '#') {
			return;
		} else {
			throw new BadNodeException(
					"Period was not followed by end of line: '" + line + "'");
		}
	}

	private boolean isQuote() {
		boolean isEscaped = i > 0 && line.charAt(i - 1) == '\\';
		return (line.charAt(i) == '"') && (!isEscaped);
	}

	public List<String> split() {
		return pieces;
	}

}