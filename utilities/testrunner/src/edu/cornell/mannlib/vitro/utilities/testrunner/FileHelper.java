/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Some utility methods for dealing with files and directories.
 */
public class FileHelper {

	/**
	 * Delete a file. If it can't be deleted, complain.
	 */
	public static void deleteFile(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		if (!file.exists()) {
			return;
		}

		/*
		 * If we were unable to delete the file, is it because it's a non-empty
		 * directory?
		 */
		if (file.isDirectory()) {
			final StringBuffer message = new StringBuffer(
					"Can't delete directory '" + file.getPath() + "'\n");
			file.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					message.append("   contains file '" + pathname + "'\n");
					return true;
				}
			});
			throw new IOException(message.toString().trim());
		} else {
			throw new IOException("Unable to delete file '" + file.getPath()
					+ "'");
		}
	}

	/**
	 * Delete all of the files in a directory, any sub-directories, and the
	 * directory itself.
	 */
	public static void purgeDirectoryRecursively(File directory)
			throws IOException {
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				purgeDirectoryRecursively(file);
			} else {
				deleteFile(file);
			}
		}
		deleteFile(directory);
	}

	/**
	 * Confirm that this is an existing, readable file.
	 */
	public static void checkReadableFile(File file, String label) {
		if (!file.exists()) {
			throw new IllegalArgumentException(label + " does not exist.");
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException(label + " is not a file.");
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException(label + " is not readable.");
		}
	}

	/**
	 * Get the name of this file, without the extension.
	 */
	public static String baseName(File file) {
		String name = file.getName();
		int periodHere = name.indexOf('.');
		if (periodHere == -1) {
			return name;
		} else {
			return name.substring(0, periodHere);
		}
	}

	/**
	 * Copy the contents of a file to a new location. If the target file already
	 * exists, it will be over-written.
	 */
	public static void copy(File source, File target) throws IOException {
		InputStream input = null;
		OutputStream output = null;

		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(target);
			int howMany;
			byte[] buffer = new byte[4096];
			while (-1 != (howMany = input.read(buffer))) {
				output.write(buffer, 0, howMany);
			}
		}finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/** No need to instantiate this, since all methods are static. */
	private FileHelper() {
		// Nothing to initialize.
	}

}
