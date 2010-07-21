/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Adjust any individual that has a main image but no thumbnail.
 */
public class NoThumbsAdjuster extends FsuScanner {
	private ImageDirectoryWithBackup imageDirectoryWithBackup;

	public NoThumbsAdjuster(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
	}

	/**
	 * For every individual with main images but no thumbnails, create a
	 * thumbnail from the first main image.
	 */
	public void adjust() {
		updateLog.section("Creating thumbnails to match main images.");

		ResIterator haveImage = model.listResourcesWithProperty(imageProperty);
		try {
			while (haveImage.hasNext()) {
				Resource resource = haveImage.next();

				if (resource.getProperty(thumbProperty) == null) {
					createThumbnailFromMainImage(resource);
				}
			}
		} finally {
			haveImage.close();
		}
	}

	/**
	 * This individual has a main image but no thumbnail. Create one.
	 * <ul>
	 * <li>Figure a name for the thumbnail image.</li>
	 * <li>Make a scaled copy of the main image into the thumbnail.</li>
	 * <li>Set that file as a thumbnail (old-style) on the individual.</li>
	 * </ul>
	 */
	private void createThumbnailFromMainImage(Resource resource) {
		String mainFilename = getValues(resource, imageProperty).get(0);
		String thumbFilename = addFilenamePrefix("_thumbnail_", mainFilename);
		updateLog.log(resource, "creating a thumbnail at '" + thumbFilename
				+ "' from the main image at '" + mainFilename + "'");

		File mainFile = imageDirectoryWithBackup.getExistingFile(mainFilename);
		File thumbFile = imageDirectoryWithBackup.getNewfile(thumbFilename);
		thumbFile = checkNameConflicts(thumbFile);
		try {
			generateThumbnailImage(mainFile, thumbFile,
					FileStorageUpdater.THUMBNAIL_WIDTH,
					FileStorageUpdater.THUMBNAIL_HEIGHT);

			resource.addProperty(thumbProperty, thumbFilename);
		} catch (IOException e) {
			updateLog.error(resource, "failed to create thumbnail file '"
					+ thumbFilename + "'", e);
		}
	}

	/**
	 * Read in the main image, and scale it to a thumbnail that maintains the
	 * aspect ratio, but doesn't exceed either of these dimensions.
	 */
	private void generateThumbnailImage(File mainFile, File thumbFile,
			int maxWidth, int maxHeight) throws IOException {
		BufferedImage bsrc = ImageIO.read(mainFile);

		double scale = Math.min(((double) maxWidth) / bsrc.getWidth(),
				((double) maxHeight) / bsrc.getHeight());
		AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
		int newWidth = (int) (scale * bsrc.getWidth());
		int newHeight = (int) (scale * bsrc.getHeight());
		updateLog.log("Scaling '" + mainFile + "' by a factor of " + scale
				+ ", from " + bsrc.getWidth() + "x" + bsrc.getHeight() + " to "
				+ newWidth + "x" + newHeight);

		BufferedImage bdest = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bdest.createGraphics();

		g.drawRenderedImage(bsrc, at);

		ImageIO.write(bdest, "JPG", thumbFile);
	}

}
