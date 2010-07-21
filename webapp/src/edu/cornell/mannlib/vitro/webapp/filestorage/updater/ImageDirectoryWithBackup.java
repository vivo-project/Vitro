/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A way to look for files in TOMCAT_WEBAPP/vivo/images, if they are not found
 * in upload.directory/images.
 */
public class ImageDirectoryWithBackup {
	private static final Log log = LogFactory
			.getLog(ImageDirectoryWithBackup.class);

	/** The primary image directory, where we do most of the manipulation. */
	private final File uploadImageDirectory;

	/**
	 * If we are looking for a file and don't find it in the primary directory,
	 * look for it here.
	 */
	private final File webappImageDirectory;

	/**
	 * Be careful! webappImageDirectory may be null.
	 */
	public ImageDirectoryWithBackup(File uploadImageDirectory,
			File webappImageDirectory) {
		this.uploadImageDirectory = uploadImageDirectory;
		this.webappImageDirectory = webappImageDirectory;
	}

	/**
	 * When looking to read a file, start by looking in the
	 * {@link #uploadImageDirectory}.
	 * 
	 * If the file isn't found there, look in the {@link #webappImageDirectory}
	 * as a fallback.
	 * 
	 * If not there either, return the pointer to the nonexistent file in the
	 * {@link #uploadImageDirectory}.
	 */
	File getExistingFile(String relativePath) {
		File file1 = new File(uploadImageDirectory, relativePath);
		if (file1.exists()) {
			log.trace("Found file: " + file1.getAbsolutePath());
			return file1;
		}
		if (webappImageDirectory != null) {
			File file2 = new File(webappImageDirectory, relativePath);
			if (file2.exists()) {
				log.trace("Found file: " + file2.getAbsolutePath());
				return file2;
			}
		}
		log.trace("Didn't find file: " + file1.getAbsolutePath());
		return file1;
	}

	/**
	 * New files will always be created in the primary directory.
	 */
	File getNewfile(String relativePath) {
		return new File(uploadImageDirectory, relativePath);
	}

	/**
	 * You can get a direct reference to the primary image directory, but it
	 * should only be used for directory-base operations, like final cleanup.
	 */
	public File getPrimaryImageDirectory() {
		return uploadImageDirectory;
	}
}
