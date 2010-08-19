/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Adjust any individual that has a thumbnail with no main image.
 */
public class AllThumbsAdjuster extends FsuScanner {
	protected final File imageDirectory;

	public AllThumbsAdjuster(FSUController controller) {
		super(controller);
		this.imageDirectory = controller.getImageDirectory();
	}

	/**
	 * For every individual with thumbnails but no main images, create a main
	 * image from the first thumbnail.
	 */
	public void adjust() {
		updateLog.section("Creating main images for thumbnails "
				+ "that have none.");

		ResIterator haveThumb = model.listResourcesWithProperty(thumbProperty);
		try {
			while (haveThumb.hasNext()) {
				Resource resource = haveThumb.next();

				if (resource.getProperty(imageProperty) == null) {
					createMainImageFromThumbnail(resource);
				}
			}
		} finally {
			haveThumb.close();
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
			File thumbFile = new File(imageDirectory, thumbFilename);
			File mainFile = new File(imageDirectory, mainFilename);
			mainFile = checkNameConflicts(mainFile);
			FileUtil.copyFile(thumbFile, mainFile);

			resource.addProperty(imageProperty, mainFilename);
		} catch (IOException e) {
			updateLog.error(resource, "failed to create main file '"
					+ mainFilename + "'", e);
		}
	}

}
