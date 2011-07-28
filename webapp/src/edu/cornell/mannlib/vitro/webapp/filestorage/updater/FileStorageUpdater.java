/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;

/**
 * <p>
 * Clean up any files that are stored in the old directory structure and
 * referenced by old-style image properties.
 * </p>
 * <p>
 * Besides converting the files to the new framework, this process will produce
 * these artifacts:
 * <ul>
 * <li>A log file in the uploaded files directory, with a timestamped name, such
 * as <code>upgrade/upgradeLog2010-06-20T14-55-00.txt</code>, for example. If
 * for any reason, the upgrade process must run again, the log will not be
 * overwritten.</li>
 * <li>A directory of "deleted" files - these were extra thumbnail files or
 * extra main image files -- see the details below.</li>
 * <li>A directory of "unreferenced" files - these were in the image directory,
 * but not connected to any entity.</li>
 * </ul>
 * </p>
 * <p>
 * We consider some special cases:
 * <ul>
 * <li>An individual may refer to an image file that does not actually exist. If
 * so, that reference will be deleted.</li>
 * <li>There may be more than one reference to the same image file. If so, all
 * but the first such reference will be deleted.</li>
 * <li>
 * In the old style, it was possible to have a main image without a thumbnail.
 * If we find that, we will generate a scaled down copy of the main image, store
 * that as a thumbnail image file, and then proceed.</li>
 * <li>
 * In the old style, it was possible to have a thumbnail without a main image.
 * If we find that, we will make a copy of the thumbnail image file, declare
 * that copy to be the main image, and then proceed.</li>
 * <li>
 * We may find individuals with more than one main image, or more than one
 * thumbnail. If so, we will discard all but the first one (move them to the
 * "deleted" directory).</li>
 * </ul>
 * </p>
 * <p>
 * Aside from these special cases, we will:
 * <ul>
 * <li>Translate the main image.
 * <ul>
 * <li>Store the image in the new file system.</li>
 * <li>Delete the image from the old images directory.</li>
 * <li>Create a ByteStream individual for the main image.</li>
 * <li>Create a Surrogate individual for the main image.</li>
 * <li>Tie these together and attach to the entity that owns the image.</li>
 * </ul>
 * </li>
 * <li>Translate the thumbnail.
 * <ul>
 * <li>Store the thumbnail in the new file system.</li>
 * <li>Create a ByteStream individual for the thumbnail.</li>
 * <li>Create a Surrogate individual for the thumbnail.</li>
 * <li>Tie these together and attach to the Surrogate for the main image.</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * After processing all of these cases, there may be some images remaining in
 * the "images" directory. These will be moved to the "unreferenced" directory,
 * while preserving any internal tree structure.
 * </p>
 */
public class FileStorageUpdater implements FSUController {
	private static final Log log = LogFactory.getLog(FileStorageUpdater.class);

	/** How wide should a generated thumbnail image be (in pixels)? */
	public static final int THUMBNAIL_WIDTH = 200;

	/** How high should a generated thumbnail image be (in pixels)? */
	public static final int THUMBNAIL_HEIGHT = 200;

	/** How is the main image referenced in the old scheme? */
	public static final String IMAGEFILE = VitroVocabulary.vitroURI
			+ "imageFile";

	/** How is the thumbnail referenced in the old scheme? */
	public static final String IMAGETHUMB = VitroVocabulary.vitroURI
			+ "imageThumb";

	private final Model model;

	private final FileStorage fileStorage;
	private final UploadedFileHelper uploadedFileHelper;
	private final ImageDirectoryWithBackup imageDirectoryWithBackup;
	private final File upgradeDirectory;

	private FSULog updateLog;

	public FileStorageUpdater(WebappDaoFactory wadf, Model model,
			FileStorage fileStorage, File uploadDirectory,
			File webappImageDirectory, ServletContext ctx) {
		this.model = model;
		this.fileStorage = fileStorage;
		this.uploadedFileHelper = new UploadedFileHelper(fileStorage, wadf, ctx);
		this.upgradeDirectory = new File(uploadDirectory, "upgrade");

		this.imageDirectoryWithBackup = new ImageDirectoryWithBackup(new File(
				uploadDirectory, "images"), webappImageDirectory);
	}

	/**
	 * <p>
	 * Go through all of the individuals who have image files or thumbnail
	 * files, adjusting them to the new way.
	 * </p>
	 * <p>
	 * If there is nothing to do, don't even create a log file, just exit.
	 * </p>
	 * <p>
	 * If there is something to do, go through the whole process.
	 * </p>
	 * <p>
	 * At the end, there should be nothing to do. If that's true, clean out the
	 * old images directory.
	 * </p>
	 */
	public void update() {
		// If there is nothing to do, we're done: don't even create a log file.
		if (!isThereAnythingToDo()) {
			log.debug("Found no pre-1.1 file references.");
			return;
		}

		// Create the upgrade directory and the log file.
		setup();

		try {
			// Remove any image properties that don't point to literals.
			new NonLiteralPropertyRemover(this).remove();

			// Remove any image properties that point to files that don't exist.
			new DeadEndPropertyRemover(this).remove();

			// No resource may have multiple main images or multiple thumbnails.
			new MultiplePropertyRemover(this).remove();

			// Create a main image for any thumbnail that doesn't have one.
			new AllThumbsAdjuster(this).adjust();

			// Create a thumbnail for any main image that doesn't have one.
			new NoThumbsAdjuster(this).adjust();

			// Copy all images into the new file storage system, translating
			// into the new schema. Get a list of all the images we translated.
			ImageSchemaTranslater translater = new ImageSchemaTranslater(this);
			Collection<String> translatedFiles = translater.translate();

			if (isThereAnythingToDo()) {
				throw new IllegalStateException(
						"FileStorageUpdate was unsuccessful -- "
								+ "model still contains pre-1.1 file references.");
			}

			// Clean out the old image directory, separating into files which
			// were translated, and files for which we found no reference.
			new ImageDirectoryCleaner(this).clean(translatedFiles);

			updateLog.section("File Storage update is complete.");
		} finally {
			updateLog.close();
		}

		log.info("Finished updating pre-1.1 file references.");
	}

	/**
	 * Query the model. If there are any resources with old-style image
	 * properties, we have work to do.
	 */
	private boolean isThereAnythingToDo() {
		if (!ModelWrapper.listResourcesWithProperty(model,
				model.createProperty(IMAGEFILE)).isEmpty()) {
			return true;
		}

		if (!ModelWrapper.listResourcesWithProperty(model,
				model.createProperty(IMAGETHUMB)).isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * Create the upgrade directory. Create the log file. If we fail, drop dead.
	 */
	private void setup() {
		try {
			this.upgradeDirectory.mkdirs();
			updateLog = new FSULog(this.upgradeDirectory,
					"FileStorageUpdater-log");
			log.info("Updating pre-1.1 file references. Log file is "
					+ updateLog.getFilename());
		} catch (IOException e) {
			if (updateLog != null) {
				updateLog.close();
			}
			throw new IllegalStateException("can't create log file: '"
					+ updateLog.getFilename() + "'", e);
		}
	}

	// ----------------------------------------------------------------------
	// Methods to set up the individual scanners.
	// ----------------------------------------------------------------------

	@Override
	public Model getModel() {
		return this.model;
	}

	@Override
	public FSULog getUpdateLog() {
		return this.updateLog;
	}

	@Override
	public UploadedFileHelper getUploadedFileHelper() {
		return this.uploadedFileHelper;
	}

	@Override
	public FileStorage getFileStorage() {
		return this.fileStorage;
	}

	@Override
	public ImageDirectoryWithBackup getImageDirectoryWithBackup() {
		return this.imageDirectoryWithBackup;
	}

	@Override
	public File getTranslatedDirectory() {
		return new File(this.upgradeDirectory, "translatedImages");
	}

	@Override
	public File getUnreferencedDirectory() {
		return new File(this.upgradeDirectory, "unreferencedImages");
	}

}
