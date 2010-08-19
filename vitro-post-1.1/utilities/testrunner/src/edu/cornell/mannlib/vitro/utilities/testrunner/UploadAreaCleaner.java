/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.io.IOException;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

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
					FileHelper.deleteFile(file);
				} else {
					FileHelper.purgeDirectoryRecursively(file);
				}
			}
		} catch (IOException e) {
			listener.cleanUploadFailed(uploadDirectory, e);
			throw e;
		} finally {
			listener.cleanUploadStop(uploadDirectory);
		}
	}

}
