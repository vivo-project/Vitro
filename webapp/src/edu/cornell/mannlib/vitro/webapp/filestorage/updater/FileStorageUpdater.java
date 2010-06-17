/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileModelHelper;
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
public class FileStorageUpdater {
	private static final Log log = LogFactory.getLog(FileStorageUpdater.class);

	private static final String IMAGEFILE = VitroVocabulary.vitroURI
			+ "imageFile";
	private static final String IMAGETHUMB = VitroVocabulary.vitroURI
			+ "imageThumb";

	/** How wide should a generated thumbnail image be (in pixels)? */
	private static final int THUMBNAIL_WIDTH = 150;

	/** How high should a generated thumbnail image be (in pixels)? */
	private static final int THUMBNAIL_HEIGHT = 150;

	private final Model model;
	private final Property imageProperty;
	private final Property thumbProperty;

	private final FileStorage fileStorage;
	private FileModelHelper fileModelHelper;

	private final File imageDirectory;
	private final File deletedDirectory;
	private final File unreferencedDirectory;

	private final File logFile;
	private PrintWriter updateLog;
	private final SimpleDateFormat timeStamper = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public FileStorageUpdater(WebappDaoFactory wadf, Model model,
			FileStorage fileStorage, File uploadDirectory) {
		this.model = model;
		this.imageProperty = model.createProperty(IMAGEFILE);
		this.thumbProperty = model.createProperty(IMAGETHUMB);

		this.fileStorage = fileStorage;
		this.fileModelHelper = new FileModelHelper(wadf);

		this.imageDirectory = new File(uploadDirectory, "images");

		File upgradeDirectory = new File(uploadDirectory, "upgrade");
		this.deletedDirectory = new File(upgradeDirectory, "deleted");
		this.unreferencedDirectory = new File(upgradeDirectory, "unreferenced");
		this.logFile = getTimestampedFilename(upgradeDirectory);
	}

	/**
	 * Create a filename for the log file that contains a timestamp, so if we
	 * run the process more than once, we will see multiple files.
	 */
	private File getTimestampedFilename(File upgradeDirectory) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-sss");
		String filename = "upgradeLog." + sdf.format(new Date()) + ".txt";
		return new File(upgradeDirectory, filename);
	}

	/**
	 * <p>
	 * Go through all of the individuals who have image files or thumbnail
	 * files, adjusting them to the new way.
	 * </p>
	 * <p>
	 * Maybe there is nothing to do. If that's true, we don't even create a log
	 * file, we just exit.
	 * </p>
	 * <p>
	 * In the old style, main images were independent from thumbnails, but we
	 * won't permit that any more.
	 * <ul>
	 * <li>
	 * If an individual has a thumbnail but no main image, make a copy of the
	 * thumbnail to become the main image.</li>
	 * <li>If an individual has a main image but no thumbnail, make a scaled
	 * copy of the main image to become the thumbnail.</li>
	 * </ul>
	 * Once this has been done, we can process all individuals in the same way.
	 * </p>
	 */
	public void update() {
		// If there is nothing to do, we're done: don't even create a log file.
		if (!isThereAnythingToDo()) {
			log.debug("Found no pre-1.1 file references.");
			return;
		}
		log.info("Updating pre-1.1 file references. Log file is " + logFile);

		try {
			updateLog = new PrintWriter(this.logFile);
			adjustIndividualsWhoAreAllThumbs();
			adjustIndividualsWhoHaveNoThumbs();
			processIndividualsWhoHaveImages();
			cleanupImagesDirectory(imageDirectory);
		} catch (FileNotFoundException e) {
			log.error(e);
		} finally {
			if (updateLog != null) {
				updateLog.flush();
				updateLog.close();
			}
		}

		if (isThereAnythingToDo()) {
			throw new IllegalStateException(
					"FileStorageUpdate was unsuccessful -- "
							+ "model still contains pre-1.1 file references.");
		}
		
		log.info("Finished updating pre-1.1 file references.");
	}

	/**
	 * Query the model. If there is anybody with a main image or a thumbnail in
	 * the old style, then we have work to do.
	 */
	private boolean isThereAnythingToDo() {
		ResIterator haveImage = model.listResourcesWithProperty(imageProperty);
		try {
			if (haveImage.hasNext()) {
				return true;
			}
		} finally {
			haveImage.close();
		}

		ResIterator haveThumb = model.listResourcesWithProperty(thumbProperty);
		try {
			if (haveThumb.hasNext()) {
				return true;
			}
		} finally {
			haveThumb.close();
		}

		return false;
	}

	/**
	 * For every individual with thumbnails but no main images, create a main
	 * image from the first thumbnail.
	 */
	private void adjustIndividualsWhoAreAllThumbs() {
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
		logIt(resource, "creating a main file at '" + mainFilename
				+ "' to match the thumbnail at '" + thumbFilename + "'");

		try {
			File thumbFile = new File(imageDirectory, thumbFilename);
			File mainFile = new File(imageDirectory, mainFilename);
			copyFile(thumbFile, mainFile);

			resource.addProperty(imageProperty, mainFilename);
		} catch (IOException e) {
			logIt(resource, "failed to create main file '" + mainFilename + "'");
			logIt(e);
		}
	}

	/**
	 * For every individual with main images but no thumbnails, create a
	 * thumbnail from the first main image.
	 */
	private void adjustIndividualsWhoHaveNoThumbs() {
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
		logIt(resource, "creating a thumbnail at '" + thumbFilename
				+ "' from the main image at '" + mainFilename + "'");

		File mainFile = new File(imageDirectory, mainFilename);
		File thumbFile = new File(imageDirectory, thumbFilename);
		try {
			generateThumbnailImage(mainFile, thumbFile, THUMBNAIL_WIDTH,
					THUMBNAIL_HEIGHT);

			resource.addProperty(thumbProperty, mainFilename);
		} catch (IOException e) {
			logIt(resource, "failed to create thumbnail file '" + thumbFilename
					+ "'");
			logIt(e);
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
		int newWidth = (int) scale * bsrc.getWidth();
		int newHeight = (int) scale * bsrc.getHeight();
		logIt("Scaling '" + mainFile + "' by a factor of " + scale + ", from "
				+ bsrc.getWidth() + "x" + bsrc.getHeight() + " to " + newWidth
				+ "x" + newHeight);

		BufferedImage bdest = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bdest.createGraphics();

		g.drawRenderedImage(bsrc, at);

		ImageIO.write(bdest, "JPG", thumbFile);
	}

	/**
	 * By the time we get here, any individual with a main image also has a
	 * thumbnail, and vice versa. For each one, translate one main image and one
	 * thumbnail into the new system and discard any other images.
	 */
	private void processIndividualsWhoHaveImages() {
		ResIterator haveImage = model.listResourcesWithProperty(imageProperty);
		try {
			while (haveImage.hasNext()) {
				Resource resource = haveImage.next();
				translateImagesAndDiscardExtras(resource);
			}
		} finally {
			haveImage.close();
		}
	}

	/**
	 * This individual has at least one main image and at least one thumbnail.
	 * <ul>
	 * <li>Translate the first main image into the new system.</li>
	 * <li>Translate the first thumbnail into the new system.</li>
	 * <li>Discard any other main image files or thumbnail files.</li>
	 * <li>Remove all old-style main image properties.</li>
	 * <li>Remove all old-style thumbnail properties.</li>
	 * </ul>
	 */
	private void translateImagesAndDiscardExtras(Resource resource) {
		boolean first;

		first = true;
		for (String value : getValues(resource, imageProperty)) {
			if (first) {
				translateMainImage(resource, value);
			} else {
				discardExtraImage(resource, value, "main image");
			}
			first = false;
		}
		resource.removeAll(imageProperty);

		first = true;
		for (String value : getValues(resource, thumbProperty)) {
			if (first) {
				translateThumbnail(resource, value);
			} else {
				discardExtraImage(resource, value, "thumbnail");
			}
		}
		resource.removeAll(thumbProperty);
	}

	/**
	 * Translate the main image into the new system
	 */
	private void translateMainImage(Resource resource, String path) {
		logIt(resource, "translating main image '" + path
				+ "' into the file storage");

		Individual file;
		try {
			file = translateFile(resource, path);

			// Set the file as the thumbnail for the person.
			Individual person = fileModelHelper.getIndividualByUri(resource
					.getURI());
			fileModelHelper.setAsMainImageOnEntity(person, file);
		} catch (IOException e) {
			logIt(resource, "Can't create the main image file.");
			logIt(e);
		}
	}

	/**
	 * Translate the thumbnail into the new system.
	 */
	private void translateThumbnail(Resource resource, String path) {
		logIt(resource, "translating thumbnail '" + path
				+ "' into the file storage");

		Individual file;
		try {
			file = translateFile(resource, path);

			// Set the file as the thumbnail for the person.
			Individual person = fileModelHelper.getIndividualByUri(resource
					.getURI());
			fileModelHelper.setThumbnailOnIndividual(person, file);
		} catch (IOException e) {
			logIt(resource, "Can't create the thumbnail file.");
			logIt(e);
		}
	}

	/**
	 * Translate an image file into the new system
	 * <ul>
	 * <li>Create a new File, FileByteStream.</li>
	 * <li>Attempt to infer MIME type.</li>
	 * <li>Copy into the File system.</li>
	 * <li>Delete from images directory.</li>
	 * </ul>
	 * 
	 * @return the new File surrogate.
	 */
	private Individual translateFile(Resource resource, String path)
			throws IOException {
		File oldFile = new File(imageDirectory, path);
		String filename = getSimpleFilename(path);
		String mimeType = guessMimeType(resource, filename);

		// Create the file individuals in the model
		Individual byteStream = fileModelHelper.createByteStreamIndividual();
		Individual file = fileModelHelper.createFileIndividual(mimeType,
				filename, byteStream);

		logIt(resource, "translating image '" + path
				+ "' into the file storage as '" + file.getURI() + "'");

		InputStream inputStream = null;
		try {
			// Store the file in the FileStorage system.
			inputStream = new FileInputStream(oldFile);
			fileStorage.createFile(byteStream.getURI(), filename, inputStream);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			deleteFile(oldFile);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return file;
	}

	/**
	 * If they have more than one main image or more than one thumbnail, move
	 * the extras to the "deleted" directory.
	 */
	private void discardExtraImage(Resource resource, String path, String label) {
		try {
			logIt(resource, "discarding a redundant " + label + " at '" + path
					+ "'");
			File oldFile = new File(imageDirectory, path);
			File deletedFile = new File(deletedDirectory, path);
			moveFile(oldFile, deletedFile);
		} catch (IOException e) {
			logIt(e);
		}
	}

	/**
	 * Go through the images directory, and discard any that remain. They must
	 * not have been referenced by any existing individuals.
	 */
	private void cleanupImagesDirectory(File directory) {
		logIt("Cleaning up image directory '" + directory + "'");
		try {
			File targetDirectory = makeCorrespondingDirectory(directory);
			File[] children = directory.listFiles();
			for (File child : children) {
				if (child.isDirectory()) {
					cleanupImagesDirectory(child);
				} else {
					logIt("Moving unreferenced file '" + child.getPath() + "'");
					try {
						moveFile(child, new File(targetDirectory, child
								.getName()));
					} catch (IOException e) {
						logIt("Can't move unreferenced file '"
								+ child.getAbsolutePath() + "'");
						logIt(e);
					}
				}
			}
		} catch (IOException e) {
			logIt("Failed to clean up images directory '"
					+ directory.getAbsolutePath() + "'");
			logIt(e);
		}
	}

	/**
	 * Figure out the path from the "images" directory to this one, and create a
	 * corresponding directory in the "unreferenced" area.
	 */
	private File makeCorrespondingDirectory(File directory) throws IOException {
		String imagesPath = imageDirectory.getAbsolutePath();
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

	/**
	 * Read all of the specified properties on a resource, and return a
	 * {@link List} of the {@link String} values.
	 */
	private List<String> getValues(Resource resource, Property property) {
		List<String> list = new ArrayList<String>();
		StmtIterator stmts = resource.listProperties(property);
		try {
			while (stmts.hasNext()) {
				Statement stmt = stmts.next();
				RDFNode object = stmt.getObject();
				if (object.isLiteral()) {
					list.add(((Literal) object).getString());
				} else {
					logIt(resource, "property value was not a literal: "
							+ "property is '" + property.getURI()
							+ "', value is '" + object + "'");
				}
			}
		} finally {
			stmts.close();
		}
		return list;
	}

	/**
	 * Remove any path parts, and just get the filename and extension.
	 */
	private String getSimpleFilename(String path) {
		return FilenameUtils.getName(path);
	}

	/**
	 * Guess what the MIME type might be.
	 */
	private String guessMimeType(Resource resource, String filename) {
		if (filename.endsWith(".gif")) {
			return "image/gif";
		} else if (filename.endsWith(".png")) {
			return "image/png";
		} else if (filename.endsWith(".jpg")) {
			return "image/jpeg";
		} else if (filename.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (filename.endsWith(".jpe")) {
			return "image/jpeg";
		} else {
			logIt(resource,
					"can't recognize the MIME type of this image file: '"
							+ filename + "'");
			return "image";
		}
	}

	/**
	 * Copy a file from one location to another, and remove it from the original
	 * location.
	 */
	private void moveFile(File from, File to) throws IOException {
		copyFile(from, to);
		deleteFile(from);
	}

	/**
	 * Copy a file from one location to another.
	 */
	private void copyFile(File from, File to) throws IOException {
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
	private void writeFile(InputStream stream, File to) throws IOException {
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
	private void deleteFile(File file) throws IOException {
		file.delete();
		if (file.exists()) {
			throw new IOException("Failed to delete file '"
					+ file.getAbsolutePath() + "'");
		}
	}

	/**
	 * Find the filename within a path so we can add this prefix to it, while
	 * retaining the path.
	 */
	private String addFilenamePrefix(String prefix, String path) {
		int slashHere = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		if (slashHere == -1) {
			return prefix + path;
		} else {
			String dirs = path.substring(0, slashHere + 1);
			String filename = path.substring(slashHere + 1);
			return dirs + prefix + filename;
		}
	}

	/**
	 * Write this message to the update log.
	 */
	private void logIt(String message) {
		updateLog.println(timeStamper.format(new Date()) + " " + message);
	}

	/**
	 * Write this message about this resource to the update log.
	 */
	private void logIt(Resource resource, String message) {
		logIt("On resource '" + resource.getURI() + "', " + message);
	}

	/**
	 * Write this exception to the update log.
	 */
	private void logIt(Exception e) {
		logIt(e.toString());
		e.printStackTrace(updateLog);
	}
}
