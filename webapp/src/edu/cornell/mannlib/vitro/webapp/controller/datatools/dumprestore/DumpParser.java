/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.io.IOException;

/**
 * The interface for parsers that process dump/restore files.
 */
public interface DumpParser extends AutoCloseable, Iterable<DumpQuad> {
	@Override
	public void close() throws IOException;

	public static class BadInputException extends RuntimeException {
		public BadInputException(String message) {
			super(message);
		}
		
		public BadInputException(String message, Throwable cause) {
			super(message, cause);
		}

	}
}
