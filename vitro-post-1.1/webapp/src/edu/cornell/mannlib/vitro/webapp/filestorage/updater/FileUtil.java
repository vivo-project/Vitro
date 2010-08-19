/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A collection of static routines for moving, copying and deleting files.
 */
public class FileUtil {
	/**
	 * Copy a file from one location to another, and remove it from the original
	 * location.
	 */
	public static void moveFile(File from, File to) throws IOException {
		copyFile(from, to);
		deleteFile(from);
	}

	/**
	 * Copy a file from one location to another.
	 */
	public static void copyFile(File from, File to) throws IOException {
		if (!from.exists()) {
			throw new FileNotFoundException("File '" + from.getAbsolutePath()
					+ "' does not exist.");
		}

		InputStream in = null;
		try {
			in = new FileInputStream(from);
			writeFile(in, to);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create a file with the contents of this data stream.
	 * 
	 * @param stream
	 *            the data stream. You must close it afterward.
	 */
	public static void writeFile(InputStream stream, File to)
			throws IOException {
		if (to.exists()) {
			throw new IOException("File '" + to.getAbsolutePath()
					+ "' already exists.");
		}

		File parent = to.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
			if (!parent.exists()) {
				throw new IOException("Can't create parent directory for '"
						+ to.getAbsolutePath() + "'");
			}
		}

		OutputStream out = null;
		try {
			out = new FileOutputStream(to);
			byte[] buffer = new byte[8192];
			int howMany;
			while (-1 != (howMany = stream.read(buffer))) {
				out.write(buffer, 0, howMany);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Delete this file, and make sure that it's gone.
	 */
	public static void deleteFile(File file) throws IOException {
		file.delete();
		if (file.exists()) {
			throw new IOException("Failed to delete file '"
					+ file.getAbsolutePath() + "'");
		}
	}

	/** No need to instantiate it -- all methods are static. */
	private FileUtil() {
		// Nothing to instantiate.
	}

}
