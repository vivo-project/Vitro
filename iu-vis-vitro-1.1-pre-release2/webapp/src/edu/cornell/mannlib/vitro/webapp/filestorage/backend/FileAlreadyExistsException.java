/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import java.io.IOException;

/**
 * Indicates that a file already exists with the specified ID, but with a
 * different filename from the one specified.
 */
public class FileAlreadyExistsException extends IOException {
	private final String id;
	private final String existingFilename;
	private final String requestedFilename;

	public FileAlreadyExistsException(String id, String existingFilename,
			String requestedFilename) {
		super("File with a different name already exists at this ID: '" + id
				+ "', requested filename: '" + requestedFilename
				+ "', existing filename: '" + existingFilename + "'");
		this.id = id;
		this.existingFilename = existingFilename;
		this.requestedFilename = requestedFilename;
	}

	public String getId() {
		return id;
	}

	public String getExistingFilename() {
		return existingFilename;
	}

	public String getRequestedFilename() {
		return requestedFilename;
	}

}
