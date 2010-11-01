/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileModelHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;

/**
 * Make copies of the main image and thumbnail in the new file storage system,
 * and in the model. Remove the old properties, but don't remove the old files
 * yet, in case someone else is referring to them also.
 */
public class ImageSchemaTranslater extends FsuScanner {
	private final ImageDirectoryWithBackup imageDirectoryWithBackup;
	protected final FileModelHelper fileModelHelper;
	protected final FileStorage fileStorage;

	public ImageSchemaTranslater(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
		this.fileStorage = controller.getFileStorage();
		this.fileModelHelper = controller.getFileModelHelper();
	}

	/**
	 * By the time we get here, any individual with a main image also has a
	 * thumbnail, and vice versa, and exactly one of each. For each one,
	 * translate the main image and the thumbnail into the new system.
	 */
	public Collection<String> translate() {
		updateLog.section("Copying images into the new file storage, "
				+ "and adding them to the new model.");

		SortedSet<String> translated = new TreeSet<String>();
		ResIterator haveImage = model.listResourcesWithProperty(imageProperty);
		try {
			while (haveImage.hasNext()) {
				Resource resource = haveImage.next();
				translateImages(resource, translated);
			}
		} finally {
			haveImage.close();
		}
		return translated;
	}

	/**
	 * This individual should have exactly one main image and exactly one
	 * thumbnail.
	 * <ul>
	 * <li>Translate the first main image into the new system.</li>
	 * <li>Translate the first thumbnail into the new system.</li>
	 * <li>Remove all old-style main image properties.</li>
	 * <li>Remove all old-style thumbnail properties.</li>
	 * </ul>
	 */
	private void translateImages(Resource resource,
			Collection<String> translated) {
		List<String> mainImages = getValues(resource, imageProperty);
		if (mainImages.size() != 1) {
			updateLog.error(resource, "has " + mainImages.size()
					+ " main images: " + mainImages);
			return;
		}

		translateMainImage(resource, mainImages.get(0));
		translated.add(mainImages.get(0));
		ResourceWrapper.removeAll(resource, imageProperty);

		List<String> thumbnails = getValues(resource, thumbProperty);
		if (thumbnails.size() != 1) {
			updateLog.error(resource, "has " + thumbnails.size()
					+ " thumbnails: " + thumbnails);
			return;
		}

		translateThumbnail(resource, thumbnails.get(0));
		translated.add(thumbnails.get(0));
		ResourceWrapper.removeAll(resource, thumbProperty);
	}

	/**
	 * Translate the main image into the new system
	 */
	private void translateMainImage(Resource resource, String path) {
		Individual file = translateFile(resource, path, "main image");
		Individual person = fileModelHelper.getIndividualByUri(resource
				.getURI());
		fileModelHelper.setAsMainImageOnEntity(person, file);
	}

	/**
	 * Translate the thumbnail into the new system.
	 */
	private void translateThumbnail(Resource resource, String path) {
		Individual file = translateFile(resource, path, "thumbnail");
		Individual person = fileModelHelper.getIndividualByUri(resource
				.getURI());
		fileModelHelper.setThumbnailOnIndividual(person, file);
	}

	/**
	 * Translate an image file into the new system
	 * <ul>
	 * <li>Create a new File, FileByteStream.</li>
	 * <li>Attempt to infer MIME type.</li>
	 * <li>Copy into the File system.</li>
	 * </ul>
	 * 
	 * @return the new File surrogate.
	 */
	private Individual translateFile(Resource resource, String path,
			String label) {
		File oldFile = imageDirectoryWithBackup.getExistingFile(path);
		String filename = getSimpleFilename(path);
		String mimeType = guessMimeType(resource, filename);

		// Create the file individuals in the model
		Individual byteStream = fileModelHelper.createByteStreamIndividual(filename);
		Individual file = fileModelHelper.createFileIndividual(mimeType,
				filename, byteStream);

		updateLog.log(resource, "translating " + label + " '" + path
				+ "' into the file storage as '" + file.getURI() + "'");

		InputStream inputStream = null;
		try {
			// Store the file in the FileStorage system.
			inputStream = new FileInputStream(oldFile);
			fileStorage.createFile(byteStream.getURI(), filename, inputStream);
		} catch (IOException e) {
			updateLog.error(resource, "Can't create the " + label + " file. ",
					e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return file;
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
		if (filename.endsWith(".gif") || filename.endsWith(".GIF")) {
			return "image/gif";
		} else if (filename.endsWith(".png") || filename.endsWith(".PNG")) {
			return "image/png";
		} else if (filename.endsWith(".jpg") || filename.endsWith(".JPG")) {
			return "image/jpeg";
		} else if (filename.endsWith(".jpeg") || filename.endsWith(".JPEG")) {
			return "image/jpeg";
		} else if (filename.endsWith(".jpe") || filename.endsWith(".JPE")) {
			return "image/jpeg";
		} else {
			updateLog.warn(resource,
					"can't recognize the MIME type of this image file: '"
							+ filename + "'");
			return "image";
		}
	}

}
