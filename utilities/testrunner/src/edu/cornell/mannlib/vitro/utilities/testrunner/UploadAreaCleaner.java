/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Clean out the file upload area, so the next suite will start with no uploads.
 */
public class UploadAreaCleaner {
	private final SeleniumRunnerParameters parms;
	private final Listener listener;

	public UploadAreaCleaner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.listener = parms.getListener();
	}

	/**
	 * Delete all of the directories and files in the upload directory. Don't
	 * delete the upload directory itself.
	 */
	public void clean() throws IOException {
		File uploadDirectory = parms.getUploadDirectory();
		if (!uploadDirectory.isDirectory()) {
			throw new IllegalArgumentException("'" + uploadDirectory.getPath()
					+ "' is not a directory.");
		}

		listener.cleanUploadStart(uploadDirectory);

		try {
			for (File file : uploadDirectory.listFiles()) {
				if (file.isFile()) {
					deleteFile(file);
				} else {
					purgeDirectoryRecursively(uploadDirectory);
				}
			}
		} catch (IOException e) {
			listener.cleanUploadFailed(uploadDirectory, e);
			throw e;
		} finally {
			listener.cleanUploadStop(uploadDirectory);
		}
	}

	/**
	 * Delete all of the files in a directory, any sub-directories, and the
	 * directory itself.
	 */
	protected static void purgeDirectoryRecursively(File directory)
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
	 * Delete a file, either before or after the test. If it can't be deleted,
	 * complain.
	 */
	protected static void deleteFile(File file) throws IOException {
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
		if (!file.isDirectory()) {
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

}
