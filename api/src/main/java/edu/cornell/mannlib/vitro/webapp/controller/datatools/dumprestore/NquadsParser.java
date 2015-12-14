/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpNode.BadNodeException;

/**
 * TODO
 */
public class NquadsParser implements DumpParser {
	private final BufferedReader r;

	public NquadsParser(InputStream is) throws IOException {
		this.r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	}

	@Override
	public void close() throws IOException {
		r.close();
	}

	@Override
	public Iterator<DumpQuad> iterator() {
		return new NQIterator();
	}

	private class NQIterator implements Iterator<DumpQuad> {
		private DumpQuad next = null;

		NQIterator() {
			lookAhead();
		}

		private void lookAhead() {
			next = null;
			String line = null;
			try {
				while (true) {
					line = r.readLine();
					if (line == null) {
						return;
					}
					if (!line.trim().startsWith("#")) {
						break;
					}
				}
			} catch (IOException e) {
				return;
			}
			next = parseLine(line);
		}

		private DumpQuad parseLine(String line) {
			try {
				List<String> strings = parseNodeStrings(line);
				int stringCount = strings.size();
				if (stringCount != 3 && stringCount != 4) {
					throw new BadInputException("Input line is invalid: has "
							+ stringCount + " groups: '" + line + "' ==> "
							+ strings);
				}

				DumpNode s = DumpNode.fromNquad(strings.get(0));
				DumpNode p = DumpNode.fromNquad(strings.get(1));
				DumpNode o = DumpNode.fromNquad(strings.get(2));
				DumpNode g = DumpNode.fromNquad((stringCount == 4) ? strings
						.get(3) : null);
				return new DumpQuad(s, p, o, g);
			} catch (BadNodeException e) {
				throw new BadInputException("unable to parse node, line='"
						+ line + "'", e);
			}
		}

		private List<String> parseNodeStrings(String line)
				throws BadNodeException {
			return new NQuadLineSplitter(line).split();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public DumpQuad next() {
			DumpQuad dq = next;
			lookAhead();
			return dq;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
