/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.PARAMETER_UPLOADED_FILE;
import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.THUMBNAIL_HEIGHT;
import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.THUMBNAIL_WIDTH;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.Dimensions;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.UserMistakeException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileModelHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileAlreadyExistsException;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;

/**
 * Handle the mechanics of validating, storing, and deleting file images.
 */
public class ImageUploadHelper {
	private static final Log log = LogFactory.getLog(ImageUploadHelper.class);

	/** Recognized file extensions mapped to MIME-types. */
	private static final Map<String, String> RECOGNIZED_FILE_TYPES = createFileTypesMap();

	private static Map<String, String> createFileTypesMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(".gif", "image/gif");
		map.put(".png", "image/png");
		map.put(".jpg", "image/jpeg");
		map.put(".jpeg", "image/jpeg");
		map.put(".jpe", "image/jpeg");
		return Collections.unmodifiableMap(map);
	}

	private final FileModelHelper fileModelHelper;
	private final FileStorage fileStorage;

	ImageUploadHelper(FileStorage fileStorage, WebappDaoFactory webAppDaoFactory) {
		this.fileModelHelper = new FileModelHelper(webAppDaoFactory);
		this.fileStorage = fileStorage;
	}

	/**
	 * The image must be present and non-empty, and must have a mime-type that
	 * represents an image we support.
	 * 
	 * We rely on the fact that a {@link FileUploadServletRequest} will always
	 * have a map of {@link FileItem}s, even if it is empty. However, that map
	 * may not contain the field that we want, or that field may contain an
	 * empty file.
	 * 
	 * @throws UserMistakeException
	 *             if there is no file, if it is empty, or if it is not an image
	 *             file.
	 */
	@SuppressWarnings("unchecked")
	FileItem validateImageFromRequest(HttpServletRequest request)
			throws UserMistakeException {
		Map<String, List<FileItem>> map = (Map<String, List<FileItem>>) request
				.getAttribute(FileUploadServletRequest.FILE_ITEM_MAP);
		if (map == null) {
			throw new IllegalStateException("Failed to parse the "
					+ "multi-part request for uploading an image.");
		}
		List<FileItem> list = map.get(PARAMETER_UPLOADED_FILE);
		if ((list == null) || list.isEmpty()) {
			throw new UserMistakeException("The form did not contain a '"
					+ PARAMETER_UPLOADED_FILE + "' field.");
		}

		FileItem file = list.get(0);
		if (file.getSize() == 0) {
			throw new UserMistakeException("No file was uploaded in '"
					+ PARAMETER_UPLOADED_FILE + "'");
		}

		String filename = getSimpleFilename(file);
		String mimeType = getMimeType(file);
		if (!RECOGNIZED_FILE_TYPES.containsValue(mimeType)) {
			throw new UserMistakeException("'" + filename
					+ "' is not a recognized image file type. "
					+ "These are the recognized types: "
					+ RECOGNIZED_FILE_TYPES);
		}

		return file;
	}

	/**
	 * If this entity already had a main image, remove the connection. If the
	 * image and the thumbnail are no longer used by anyone, remove them from
	 * the model, and from the file system.
	 */
	void removeExistingImage(Individual person) {
		Individual mainImage = fileModelHelper.removeMainImage(person);
		if (mainImage == null) {
			return;
		}

		removeExistingThumbnail(person);

		if (!fileModelHelper.isFileReferenced(mainImage)) {
			Individual bytes = FileModelHelper.getBytestreamForFile(mainImage);
			if (bytes != null) {
				try {
					fileStorage.deleteFile(bytes.getURI());
				} catch (IOException e) {
					throw new IllegalStateException(
							"Can't delete the main image file: '"
									+ bytes.getURI() + "' for '"
									+ person.getName() + "' ("
									+ person.getURI() + ")", e);
				}
			}
			fileModelHelper.removeFileFromModel(mainImage);
		}
	}

	/**
	 * Store this image in the model and in the file storage system, and set it
	 * as the main image for this person.
	 */
	void storeMainImageFile(Individual person, FileItem imageFileItem) {
		InputStream inputStream = null;
		try {
			inputStream = imageFileItem.getInputStream();
			String mimeType = getMimeType(imageFileItem);
			String filename = getSimpleFilename(imageFileItem);

			// Create the file individuals in the model
			Individual byteStream = fileModelHelper
					.createByteStreamIndividual();
			Individual file = fileModelHelper.createFileIndividual(mimeType,
					filename, byteStream);

			// Store the file in the FileStorage system.
			fileStorage.createFile(byteStream.getURI(), filename, inputStream);

			// Set the file as the main image for the person.
			fileModelHelper.setAsMainImageOnEntity(person, file);
		} catch (FileAlreadyExistsException e) {
			throw new IllegalStateException(
					"Can't create the main image file for '" + person.getName()
							+ "' (" + person.getURI() + ")" + e.getMessage(), e);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Can't create the main image file for '" + person.getName()
							+ "' (" + person.getURI() + ")", e);
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
	 * If the entity already has a thumbnail, remove it. If there are no other
	 * references to the thumbnail, delete it from the model and from the file
	 * system.
	 */
	void removeExistingThumbnail(Individual person) {
		Individual mainImage = FileModelHelper.getMainImage(person);
		Individual thumbnail = FileModelHelper.getThumbnailForImage(mainImage);
		if (thumbnail == null) {
			return;
		}

		fileModelHelper.removeThumbnail(person);

		if (!fileModelHelper.isFileReferenced(thumbnail)) {
			Individual bytes = FileModelHelper.getBytestreamForFile(thumbnail);
			if (bytes != null) {
				try {
					fileStorage.deleteFile(bytes.getURI());
				} catch (IOException e) {
					throw new IllegalStateException(
							"Can't delete the thumbnail file: '"
									+ bytes.getURI() + "' for '"
									+ person.getName() + "' ("
									+ person.getURI() + ")", e);
				}
			}
			fileModelHelper.removeFileFromModel(thumbnail);
		}
	}

	/**
	 * Generate a thumbnail from the main image from it, store it in the model
	 * and in the file storage system, and set it as the thumbnail on the main
	 * image.
	 */
	void generateThumbnailAndStore(Individual person,
			ImageUploadController.CropRectangle crop) {
		String mainBytestreamUri = FileModelHelper
				.getMainImageBytestreamUri(person);
		String mainFilename = FileModelHelper.getMainImageFilename(person);
		if (mainBytestreamUri == null) {
			log.warn("Tried to generate a thumbnail on '" + person.getURI()
					+ "', but there was no main image.");
			return;
		}

		InputStream mainInputStream = null;
		InputStream thumbInputStream = null;
		try {
			mainInputStream = fileStorage.getInputStream(mainBytestreamUri,
					mainFilename);
			thumbInputStream = scaleImageForThumbnail(mainInputStream, crop);
			String mimeType = RECOGNIZED_FILE_TYPES.get(".jpg");
			String filename = createThumbnailFilename(mainFilename);

			// Create the file individuals in the model
			Individual byteStream = fileModelHelper
					.createByteStreamIndividual();
			Individual file = fileModelHelper.createFileIndividual(mimeType,
					filename, byteStream);

			// Store the file in the FileStorage system.
			fileStorage.createFile(byteStream.getURI(), filename,
					thumbInputStream);

			// Set the file as the thumbnail on the main image for the person.
			fileModelHelper.setThumbnailOnIndividual(person, file);
		} catch (FileAlreadyExistsException e) {
			throw new IllegalStateException("Can't create the thumbnail file: "
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new IllegalStateException("Can't create the thumbnail file",
					e);
		} finally {
			if (mainInputStream != null) {
				try {
					mainInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (thumbInputStream != null) {
				try {
					thumbInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Internet Explorer and Opera will give you the full path along with the
	 * filename. This will remove the path.
	 */
	private String getSimpleFilename(FileItem item) {
		String fileName = item.getName();
		if (fileName == null) {
			return null;
		} else {
			return FilenameUtils.getName(fileName);
		}
	}

	/**
	 * Get the MIME type as supplied by the browser. If none, try to infer it
	 * from the filename extension and the map of recognized MIME types.
	 */
	private String getMimeType(FileItem file) {
		String mimeType = file.getContentType();
		if (mimeType != null) {
			return mimeType;
		}

		String filename = getSimpleFilename(file);
		int periodHere = filename.lastIndexOf('.');
		if (periodHere == -1) {
			return null;
		}

		String extension = filename.substring(periodHere);
		return RECOGNIZED_FILE_TYPES.get(extension);
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
	 * Create a thumbnail from a source image, given a cropping rectangle (x, y,
	 * width, height).
	 */
	private InputStream scaleImageForThumbnail(InputStream source,
			ImageUploadController.CropRectangle crop) throws IOException {
		BufferedImage bsrc = ImageIO.read(source);

		// Insure that x and y fall within the image dimensions.
		int x = Math.max(0, Math.min(bsrc.getWidth(), Math.abs(crop.x)));
		int y = Math.max(0, Math.min(bsrc.getHeight(), Math.abs(crop.y)));

		// Insure that width and height are reasonable.
		int w = Math.max(5, Math.min(bsrc.getWidth() - x, crop.width));
		int h = Math.max(5, Math.min(bsrc.getHeight() - y, crop.height));

		// Figure the scales.
		double scaleWidth = ((double) THUMBNAIL_WIDTH) / ((double) w);
		double scaleHeight = ((double) THUMBNAIL_HEIGHT) / ((double) h);

		log.debug("Generating a thumbnail, initial crop info: " + crop.x + ", "
				+ crop.y + ", " + crop.width + ", " + crop.height);
		log.debug("Generating a thumbnail, bounded crop info: " + x + ", " + y
				+ ", " + w + ", " + h);
		log.debug("Generating a thumbnail, scales: " + scaleWidth + ", "
				+ scaleHeight);

		// Create the transform.
		AffineTransform at = new AffineTransform();
		at.scale(scaleWidth, scaleHeight);
		at.translate(-x, -y);

		// Apply the transform.
		BufferedImage bdest = new BufferedImage(THUMBNAIL_WIDTH,
				THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bdest.createGraphics();
		g.drawRenderedImage(bsrc, at);

		// Get an input stream.
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(bdest, "JPG", buffer);
		return new ByteArrayInputStream(buffer.toByteArray());
	}

	/**
	 * Find out how big the main image is.
	 */
	Dimensions getMainImageSize(Individual entity) {
		String uri = FileModelHelper.getMainImageBytestreamUri(entity);
		String filename = FileModelHelper.getMainImageFilename(entity);
		InputStream stream = null;
		try {
			stream = fileStorage.getInputStream(uri, filename);
			BufferedImage image = ImageIO.read(stream);
			return new Dimensions(image.getWidth(), image.getHeight());
		} catch (FileNotFoundException e) {
			log.warn("No main image file for '" + showUri(entity) + "'; name='"
					+ filename + "', bytestreamUri='" + uri + "'", e);
			return new Dimensions(0, 0);
		} catch (IOException e) {
			log.warn(
					"Can't read main image file for '" + showUri(entity)
							+ "'; name='" + filename + "', bytestreamUri='"
							+ uri + "'", e);
			return new Dimensions(0, 0);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String showUri(Individual entity) {
		return (entity == null) ? "null" : entity.getURI();
	}
}
