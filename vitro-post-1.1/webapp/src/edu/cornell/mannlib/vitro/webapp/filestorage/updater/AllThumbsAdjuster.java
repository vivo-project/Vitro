/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Adjust any individual that has a thumbnail with no main image.
 */
public class AllThumbsAdjuster extends FsuScanner {
	private ImageDirectoryWithBackup imageDirectoryWithBackup;

	public AllThumbsAdjuster(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
	}

	/**
	 * For every individual with thumbnails but no main images, create a main
	 * image from the first thumbnail.
	 */
	public void adjust() {
		updateLog.section("Creating main images for thumbnails "
				+ "that have none.");

		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				thumbProperty)) {
			if (ResourceWrapper.getProperty(resource, imageProperty) == null) {
				createMainImageFromThumbnail(resource);
			}
		}
	}

	/**
	 * This individual has a thumbnail but no main image. Create one.
	 * <ul>
	 * <li>Figure a name for the main image.</li>
	 * <li>Copy the thumbnail image file into the main image file.</li>
	 * <li>Set that file as an image (old-style) on the individual.</li>
	 * </ul>
	 */
	private void createMainImageFromThumbnail(Resource resource) {
		String thumbFilename = getValues(resource, thumbProperty).get(0);
		String mainFilename = addFilenamePrefix("_main_image_", thumbFilename);
		updateLog.log(resource, "creating a main file at '" + mainFilename
				+ "' to match the thumbnail at '" + thumbFilename + "'");

		try {
			File thumbFile = imageDirectoryWithBackup
					.getExistingFile(thumbFilename);
			File mainFile = imageDirectoryWithBackup.getNewfile(mainFilename);
			mainFile = checkNameConflicts(mainFile);
			FileUtil.copyFile(thumbFile, mainFile);
			ResourceWrapper.addProperty(resource, imageProperty, mainFilename);
		} catch (IOException e) {
			updateLog.error(resource, "failed to create main file '"
					+ mainFilename + "'", e);
		}
	}
}
