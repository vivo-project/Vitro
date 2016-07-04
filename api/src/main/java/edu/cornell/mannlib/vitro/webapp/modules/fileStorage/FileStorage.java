/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.fileStorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * The interface for the File Storage system.
 */
public interface FileStorage extends Application.Module {
	/**
	 * Store the bytes from this stream as a file with the specified ID and
	 * filename. If the file already exists, it is over-written.
	 * 
	 * @throws FileAlreadyExistsException
	 *             if a file already exists with this ID but with a different
	 *             filename.
	 * 
	 */
	void createFile(String id, String filename, InputStream bytes)
			throws FileAlreadyExistsException, IOException;

	/**
	 * If a file exists with this ID, get its name.
	 * 
	 * @return The name of the file (un-encoded) if it exists, or
	 *         {@code null} if it does not.
	 */
	String getFilename(String id) throws IOException;

	/**
	 * Get a stream that will provide the contents of the file that was stored
	 * with this ID and this filename. Close the stream when you're finished
	 * with it.
	 * 
	 * @throws FileNotFoundException
	 *             if there is no file that matches this ID and filename.
	 */
	InputStream getInputStream(String id, String filename)
			throws FileNotFoundException, IOException;

	/**
	 * If a file exists with this ID, it will be deleted, regardless of the file
	 * name. If no such file exists, no action is taken, no exception is thrown.
	 * 
	 * @return {@code true} if a file existed, {@code false} otherwise.
	 */
	boolean deleteFile(String id) throws IOException;
}
