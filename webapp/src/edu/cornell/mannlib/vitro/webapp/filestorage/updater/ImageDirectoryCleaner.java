/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Clean out the old image directory. Copy the files into the upgrade directory,
 * separating into the ones that we translated, and the ones that weren't
 * referenced.
 */
public class ImageDirectoryCleaner extends FsuScanner {
	private final ImageDirectoryWithBackup imageDirectoryWithBackup;
	protected final File translatedDirectory;
	protected final File unreferencedDirectory;

	public ImageDirectoryCleaner(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();

		this.translatedDirectory = controller.getTranslatedDirectory();
		this.unreferencedDirectory = controller.getUnreferencedDirectory();
	}

	/**
	 * Remove all of the files from the old image directory.
	 */
	public void clean(Collection<String> translatedFiles) {
		updateLog.section("Cleaning the old image directory of "
				+ "files that were translated.");
		removeTranslatedFiles(translatedFiles);

		updateLog.section("Cleaning the old image directory of "
				+ "files that were not referenced.");
		removeRemainingFiles(imageDirectoryWithBackup
				.getPrimaryImageDirectory());
	}

	/**
	 * Move all of the files that we translated into the new system.
	 */
	private void removeTranslatedFiles(Collection<String> translatedFiles) {
		for (String path : translatedFiles) {
			File oldFile = new File(
					imageDirectoryWithBackup.getPrimaryImageDirectory(), path);
			if (oldFile.exists()) {
				updateLog.log("moving image file '" + path
						+ "' to the 'translated' directory.");
				File deletedFile = new File(translatedDirectory, path);
				try {
					FileUtil.moveFile(oldFile, deletedFile);
				} catch (IOException e) {
					updateLog.error("Failed to move translated file '"
							+ oldFile.getAbsolutePath() + "'");
				}
			} else {
				updateLog.log("Not moving image file '" + path
						+ "' to the 'translated' directory -- "
						+ "found it in the backup directory.");
			}
		}
	}

	/**
	 * Go through the images directory, and discard any that remain. They must
	 * not have been referenced by any existing individuals.
	 */
	private void removeRemainingFiles(File directory) {
		updateLog.log("Cleaning image directory '" + directory + "'");
		try {
			File targetDirectory = makeCorrespondingDirectory(directory);
			File[] children = directory.listFiles();
			if (children != null) {
				for (File child : children) {
					if (child.isDirectory()) {
						removeRemainingFiles(child);
					} else {
						moveUnreferencedFile(targetDirectory, child);
					}
				}
			}
		} catch (IOException e) {
			updateLog.error(
					"Failed to clean images directory '"
							+ directory.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * Move this file from its current location to its new home in the
	 * "unreferenced" directory. Log it.
	 */
	private void moveUnreferencedFile(File targetDirectory, File file) {
		updateLog.log("Moving image file '" + file.getPath()
				+ "' to the 'unreferenced' directory");
		try {
			File newFile = new File(targetDirectory, file.getName());
			FileUtil.moveFile(file, newFile);
		} catch (IOException e) {
			updateLog.error(
					"Can't move unreferenced file '" + file.getAbsolutePath()
							+ "'", e);
		}
	}

	/**
	 * Figure out the path from the "images" directory to this one, and create a
	 * corresponding directory in the "unreferenced" area.
	 */
	private File makeCorrespondingDirectory(File directory) throws IOException {
		String imagesPath = imageDirectoryWithBackup.getPrimaryImageDirectory()
				.getAbsolutePath();
		String thisPath = directory.getAbsolutePath();

		if (!thisPath.startsWith(imagesPath)) {
			throw new IOException("Can't make a corresponding directory for '"
					+ thisPath + "'");
		}

		String suffix = thisPath.substring(imagesPath.length());

		File corresponding = new File(unreferencedDirectory, suffix);
		corresponding.mkdirs();
		if (!corresponding.exists()) {
			throw new IOException("Failed to create corresponding directory '"
					+ corresponding.getAbsolutePath() + "'");
		}

		return corresponding;
	}
}
