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

import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;

/**
 * Make copies of the main image and thumbnail in the new file storage system,
 * and in the model. Remove the old properties, but don't remove the old files
 * yet, in case someone else is referring to them also.
 */
public class ImageSchemaTranslater extends FsuScanner {
	private final ImageDirectoryWithBackup imageDirectoryWithBackup;
	protected final FileStorage fileStorage;
	protected final UploadedFileHelper uploadedFileHelper;

	public ImageSchemaTranslater(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
		this.fileStorage = controller.getFileStorage();
		this.uploadedFileHelper = controller.getUploadedFileHelper();
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

		List<String> thumbnails = getValues(resource, thumbProperty);
		if (thumbnails.size() != 1) {
			updateLog.error(resource, "has " + thumbnails.size()
					+ " thumbnails: " + thumbnails);
			return;
		}

		FileInfo main = translateFile(resource, mainImages.get(0), "main image");
		FileInfo thumb = translateFile(resource, thumbnails.get(0), "thumbnail");
		if ((main == null) || (thumb == null)) {
			return;
		}
		uploadedFileHelper.setImagesOnEntity(resource.getURI(), main, thumb);

		translated.add(mainImages.get(0));
		ResourceWrapper.removeAll(resource, imageProperty);

		translated.add(thumbnails.get(0));
		ResourceWrapper.removeAll(resource, thumbProperty);

	}

	/**
	 * Translate an image file into the new system
	 * <ul>
	 * <li>Attempt to infer MIME type.</li>
	 * <li>Copy into the File system.</li>
	 * <li>Create the File and Bytestream individuals in the model.</li>
	 * </ul>
	 */
	private FileInfo translateFile(Resource resource, String path, String label) {
		File oldFile = imageDirectoryWithBackup.getExistingFile(path);
		String filename = getSimpleFilename(path);
		String mimeType = guessMimeType(resource, filename);

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(oldFile);
			// Create the file individuals in the model
			FileInfo fileInfo = uploadedFileHelper.createFile(filename,
					mimeType, inputStream);
			updateLog.log(resource, "translating " + label + " '" + path
					+ "' into the file storage as '" + fileInfo.getUri() + "'");
			return fileInfo;
		} catch (IOException e) {
			updateLog.error(resource, "Can't create the " + label + " file. ",
					e);
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
