/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Collection;
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
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileAlreadyExistsException;
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
 * <li>A directory of "deleted" files - these were thumbnail files or extra main
 * image files -- see the details below.</li>
 * <li>A directory of "unreferenced" files - these wee in the image directory,
 * but not connected to any entity.</li>
 * </ul>
 * </p>
 * <p>
 * We consider some special cases:
 * <ul>
 * <li>
 * In the old style, some thumbnails bore no relationship to the main image. So,
 * if we find a main image and a thumbnail, we will discard the thumbnail file
 * (move it to the "deleted" directory), and generate a new one.</li>
 * <li>
 * In the old style, it was possible to have a thumbnail without a main image.
 * If we find that, we will delare the thumbnail to be the main image, and
 * generate a new thumbnail.</li>
 * <li>
 * We may find individuals with more than one main image. If so, we will discard
 * all but the first one (move them to the "deleted" directory).</li>
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
 * <li>Generate a new thumbnail.
 * <ul>
 * <li>Scale the main image to produce a thumbnail</li>
 * <li>Store the thumbnail in the new file system.</li>
 * <li>Create a ByteStream individual for the thumbnail.</li>
 * <li>Create a Surrogate individual for the thumbnail.</li>
 * <li>Tie these together and attach to the Surrogate for the main image.</li>
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH-mm-sss");
		String filename = "upgradeLog." + sdf.format(new Date()) + ".txt";
		return new File(upgradeDirectory, filename);
	}

	/**
	 * Go through all of the individuals who have image files or thumbnail
	 * files, adjusting them to the new way.
	 * 
	 * If an individual has a thumbnail but no main image, convert the thumbnail
	 * to a main image and proceed. Then we can treat all images in the same
	 * way.
	 */
	public void update() {
		// If there is nothing to do, we're done: don't even create a log file.
		if (!isThereAnythingToDo()) {
			return;
		}

		try {
			updateLog = new PrintWriter(this.logFile);
			adjustIndividualsWhoAreAllThumbs();
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
	 * For every individual with thumbnails but no main images, make each
	 * thumbnail into a main image.
	 */
	private void adjustIndividualsWhoAreAllThumbs() {
		ResIterator haveThumb = model.listResourcesWithProperty(thumbProperty);
		try {
			while (haveThumb.hasNext()) {
				Resource resource = haveThumb.next();

				// Resources who do have main images don't need anything here.
				if (resource.getProperty(imageProperty) == null) {
					for (String value : getValues(resource, thumbProperty)) {
						resource.addProperty(imageProperty, value);
						logIt(resource, "moved thumbnail '" + value
								+ "' to main image.");
					}
					resource.removeAll(thumbProperty);
				}
			}
		} finally {
			haveThumb.close();
		}

	}

	/**
	 * Find all individuals that have main files. For each one:
	 * <ul>
	 * <li>Discard any thumbnail files.</li>
	 * <li>Translate the first main image into the new system.</li>
	 * <li>Generate and store a new thumbnail for the image.</li>
	 * <li>Discard any other main image files.</li>
	 * <li>Remove all old-style main image properties.</li>
	 * <li>Remove all old-style thumbnail properties.</li>
	 * </ul>
	 */
	private void processIndividualsWhoHaveImages() {
		Property imageProperty = model.createProperty(IMAGEFILE);
		ResIterator haveImage = model.listResourcesWithProperty(imageProperty);
		try {
			while (haveImage.hasNext()) {
				Resource resource = haveImage.next();
				for (String value : getValues(resource, thumbProperty)) {
					discardThumbnail(resource, value);
				}
				resource.removeAll(thumbProperty);

				boolean first = true;
				for (String value : getValues(resource, imageProperty)) {
					if (first) {
						translateMainImage(resource, value);
						generateThumbnailAndStore(resource);
					} else {
						discardExtraMainImage(resource, value);
					}
					first = false;
				}
				resource.removeAll(imageProperty);
			}
		} finally {
			haveImage.close();
		}
	}

	/**
	 * If they have an old-style thumbnail, move it to the "discarded"
	 * directory.
	 */
	private void discardThumbnail(Resource resource, String path) {
		try {
			logIt(resource, "discarding old thumbnail at '" + path + "'");
			File oldFile = new File(imageDirectory, path);
			File deletedFile = new File(deletedDirectory, path);
			moveFile(oldFile, deletedFile);
		} catch (IOException e) {
			logIt(e);
		}
	}

	/**
	 * <pre>
	 *   Translate the main image into the new system
	 *   	Create a new File, FileByteStream.
	 *   	Attach to the individual.
	 *   	Attempt to infer MIME type. 
	 *   	Copy into the File system.
	 *   	Delete from images directory,
	 * </pre>
	 */
	private void translateMainImage(Resource resource, String path) {
		File oldFile = new File(imageDirectory, path);
		String filename = getSimpleFilename(path);
		String mimeType = getMimeType(resource, filename);

		// Create the file individuals in the model
		Individual byteStream = fileModelHelper.createByteStreamIndividual();
		Individual file = fileModelHelper.createFileIndividual(mimeType,
				filename, byteStream);

		logIt(resource, "translating main image '" + path
				+ "' into the file storage as '" + file.getURI() + "'");

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(oldFile);

			// Store the file in the FileStorage system.
			fileStorage.createFile(byteStream.getURI(), filename, inputStream);

			// Set the file as the main image for the person.
			Individual person = fileModelHelper.getIndividualByUri(resource
					.getURI());
			fileModelHelper.setAsMainImageOnEntity(person, file);
		} catch (FileAlreadyExistsException e) {
			logIt(resource, "Can't create the main image file.");
			logIt(e);
		} catch (IOException e) {
			logIt(resource, "Can't create the main image file.");
			logIt(e);
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
	}

	/**
	 * <pre>
	 *   Generate the thumbnail.
	 *   	Create a new File, FileByteStream
	 *   	attach to the main image
	 *   	Mime type and filename come from generation process.
	 *   	Copy into the file system.
	 * </pre>
	 */
	private void generateThumbnailAndStore(Resource resource) {
		Individual person = fileModelHelper.getIndividualByUri(resource
				.getURI());
		String mainBytestreamUri = FileModelHelper
				.getMainImageBytestreamUri(person);
		String mainFilename = FileModelHelper.getMainImageFilename(person);
		String thumbnailFilename = createThumbnailFilename(getSimpleFilename(mainFilename));
		String mimeType = getMimeType(resource, thumbnailFilename);

		// Create the file individuals in the model
		Individual thumbByteStream = fileModelHelper
				.createByteStreamIndividual();
		Individual thumbSurrogate = fileModelHelper.createFileIndividual(
				mimeType, thumbnailFilename, thumbByteStream);

		logIt(resource, "generating new thumbnail image " + "and storing as '"
				+ thumbSurrogate.getURI() + "'");

		InputStream mainImageInputStream = null;
		InputStream thumbnailInputStream = null;
		try {
			mainImageInputStream = fileStorage.getInputStream(
					mainBytestreamUri, mainFilename);
			thumbnailInputStream = scaleImageForThumbnail(mainImageInputStream,
					THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

			// Store the file in the FileStorage system.
			fileStorage.createFile(thumbByteStream.getURI(), thumbnailFilename,
					thumbnailInputStream);

			// Set the file as the thumbnail on the main image for the person.
			fileModelHelper.setThumbnailOnIndividual(person, thumbSurrogate);
		} catch (FileAlreadyExistsException e) {
			logIt(resource, "Can't create the thumbnail file.");
			logIt(e);
		} catch (IOException e) {
			logIt(resource, "Can't create the thumbnail file.");
			logIt(e);
		} finally {
			if (thumbnailInputStream != null) {
				try {
					thumbnailInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (mainImageInputStream != null) {
				try {
					mainImageInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * If they have more than one main image, move any redundant ones to the
	 * "deleted" directory.
	 */
	private void discardExtraMainImage(Resource resource, String path) {
		try {
			logIt(resource, "discarding a redundant main image at '" + path
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
	private Collection<String> getValues(Resource resource, Property property) {
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
	 * Take the image file, and create a scaled down version of it.
	 * 
	 * @param source
	 *            the input stream of the image file - you must close this
	 *            yourself.
	 * @return the input stream of the thumbnail - you must close this also.
	 */
	private InputStream scaleImageForThumbnail(InputStream source, int width,
			int height) throws IOException {
		BufferedImage bsrc = ImageIO.read(source);

		AffineTransform at = AffineTransform.getScaleInstance((double) width
				/ bsrc.getWidth(), (double) height / bsrc.getHeight());

		BufferedImage bdest = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bdest.createGraphics();

		g.drawRenderedImage(bsrc, at);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(bdest, "JPG", buffer);
		return new ByteArrayInputStream(buffer.toByteArray());
	}

	/**
	 * Create a name for the thumbnail from the name of the original file.
	 * "myPicture.anything" becomes "thumbnail_myPicture.jpg".
	 */
	private String createThumbnailFilename(String filename) {
		String prefix = "thumbnail_";
		String extension = ".jpg";
		int periodHere = filename.lastIndexOf('.');
		if (periodHere == -1) {
			return prefix + filename + extension;
		} else {
			return prefix + filename.substring(0, periodHere) + extension;
		}
	}

	/**
	 * Guess as to what the MIME type might be.
	 */
	private String getMimeType(Resource resource, String filename) {
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
		if (!from.exists()) {
			throw new FileNotFoundException("File '" + from.getAbsolutePath()
					+ "' does not exist.");
		}
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

		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			byte[] buffer = new byte[8192];
			int howMany;
			while (-1 != (howMany = in.read(buffer))) {
				out.write(buffer, 0, howMany);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		deleteFile(from);
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
