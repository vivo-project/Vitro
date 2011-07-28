/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.MAXIMUM_FILE_SIZE;
import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.PARAMETER_UPLOADED_FILE;
import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.THUMBNAIL_HEIGHT;
import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.THUMBNAIL_WIDTH;
import static edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest.FILE_ITEM_MAP;
import static edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest.FILE_UPLOAD_EXCEPTION;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.util.ImagingListener;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.CropRectangle;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.Dimensions;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.UserMistakeException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.TempFileHolder;
import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileAlreadyExistsException;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;

/**
 * Handle the mechanics of validating, storing, and deleting file images.
 */
public class ImageUploadHelper {
	private static final Log log = LogFactory.getLog(ImageUploadHelper.class);

	/**
	 * When they upload a new image, store it as this session attribute until
	 * we're ready to attach it to the Individual.
	 */
	public static final String ATTRIBUTE_TEMP_FILE = "ImageUploadHelper.tempFile";

	/**
	 * If the main image is larger than this, it will be displayed at reduced
	 * scale.
	 */
	public static final int MAXIMUM_IMAGE_DISPLAY_WIDTH = 500;

	/** Recognized file extensions mapped to MIME-types. */
	private static final Map<String, String> RECOGNIZED_FILE_TYPES = createFileTypesMap();

	/** Browser-specific MIME-types mapped to recognized MIME-types. */
	private static final Map<String, String> NON_STANDARD_MIME_TYPES = createNonStandardMimeTypesMap();

	private static Map<String, String> createFileTypesMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(".gif", "image/gif");
		map.put(".png", "image/png");
		map.put(".jpg", "image/jpeg");
		map.put(".jpeg", "image/jpeg");
		map.put(".jpe", "image/jpeg");
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Internet Explorer can tell us that an image has a funky
	 * Microsoft-specific MIME-type, and we can replace it with one that
	 * everyone recognizes. This table records those types.
	 */
	private static Map<String, String> createNonStandardMimeTypesMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("image/x-png", "image/png");
		map.put("image/pjpeg", "image/jpeg");
		return Collections.unmodifiableMap(map);
	}

	/*
	 * Prevent Java Advanced Imaging from complaining about the lack of
	 * accelerator classes.
	 */
	static {
		JAI.getDefaultInstance().setImagingListener(
				new NonNoisyImagingListener());
	}

	private final FileStorage fileStorage;
	private final UploadedFileHelper uploadedFileHelper;

	ImageUploadHelper(FileStorage fileStorage,
			WebappDaoFactory webAppDaoFactory, ServletContext ctx) {
		this.fileStorage = fileStorage;
		this.uploadedFileHelper = new UploadedFileHelper(fileStorage,
				webAppDaoFactory, ctx);
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
		Object exception = request.getAttribute(FILE_UPLOAD_EXCEPTION);
		if (exception != null) {
			int limit = MAXIMUM_FILE_SIZE / (1024 * 1024);
			throw new UserMistakeException(
					"Please upload an image smaller than " + limit
							+ " megabytes");
		}

		Map<String, List<FileItem>> map = (Map<String, List<FileItem>>) request
				.getAttribute(FILE_ITEM_MAP);
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
			throw new UserMistakeException("Please browse and select a photo.");
		}

		String filename = getSimpleFilename(file);
		String mimeType = getMimeType(file);
		if (!RECOGNIZED_FILE_TYPES.containsValue(mimeType)) {
			log.debug("Unrecognized MIME type: '" + mimeType + "'");
			throw new UserMistakeException("'" + filename
					+ "' is not a recognized image file type. "
					+ "Please upload JPEG, GIF, or PNG files only.");
		}

		return file;
	}

	/**
	 * The user has uploaded a new main image, but we're not ready to assign it
	 * to them.
	 * 
	 * Put it into the file storage system, and attach it as a temp file on the
	 * session until we need it.
	 */
	FileInfo storeNewImage(FileItem fileItem, VitroRequest vreq) {
		InputStream inputStream = null;
		try {
			inputStream = fileItem.getInputStream();
			String mimeType = getMimeType(fileItem);
			String filename = getSimpleFilename(fileItem);
			FileInfo fileInfo = uploadedFileHelper.createFile(filename,
					mimeType, inputStream);

			TempFileHolder.attach(vreq.getSession(), ATTRIBUTE_TEMP_FILE,
					fileInfo);

			return fileInfo;
		} catch (FileAlreadyExistsException e) {
			throw new IllegalStateException("Can't create the new image file.",
					e);
		} catch (IOException e) {
			throw new IllegalStateException("Can't create the new image file.",
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
	}

	/**
	 * Find out how big this image is.
	 * 
	 * @throws UserMistakeException
	 *             if the image is smaller than a thumbnail.
	 */
	Dimensions getNewImageSize(FileInfo fileInfo) throws UserMistakeException {
		InputStream source = null;
		try {
			String uri = fileInfo.getBytestreamUri();
			String filename = fileInfo.getFilename();

			source = fileStorage.getInputStream(uri, filename);
			MemoryCacheSeekableStream stream = new MemoryCacheSeekableStream(
					source);
			RenderedOp image = JAI.create("stream", stream);

			Dimensions size = new Dimensions(image.getWidth(),
					image.getHeight());
			log.debug("new image size is " + size);

			if ((size.height < THUMBNAIL_HEIGHT)
					|| (size.width < THUMBNAIL_WIDTH)) {
				throw new UserMistakeException(
						"The uploaded image should be at least "
								+ THUMBNAIL_HEIGHT + " pixels high and "
								+ THUMBNAIL_WIDTH + " pixels wide.");
			}

			return size;
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("File not found: " + fileInfo, e);
		} catch (IOException e) {
			throw new IllegalStateException("Can't read image file: "
					+ fileInfo, e);
		} catch (UserMistakeException e) {
			throw e;
		} catch (Exception e) {
			log.warn("Unexpected exception in image handling", e);
			throw new UserMistakeException("Sorry, we were unable to process "
					+ "the photo you provided. Please try another photo.");
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the info for the new image, from where we stored it in the session.
	 * 
	 * @throws UserMistakeException
	 *             if it isn't there.
	 */
	FileInfo getNewImageInfo(VitroRequest vreq) throws UserMistakeException {
		FileInfo fileInfo = TempFileHolder.remove(vreq.getSession(),
				ATTRIBUTE_TEMP_FILE);

		if (fileInfo == null) {
			throw new UserMistakeException(
					"There is no image file to be cropped.");
		}

		return fileInfo;
	}

	/**
	 * Crop the main image to create the thumbnail, and put it into the file
	 * storage system.
	 */
	FileInfo generateThumbnail(CropRectangle crop, FileInfo newImage) {
		InputStream mainStream = null;
		InputStream thumbStream = null;
		try {
			String mainBytestreamUri = newImage.getBytestreamUri();
			String mainFilename = newImage.getFilename();
			mainStream = fileStorage.getInputStream(mainBytestreamUri,
					mainFilename);

			thumbStream = new ImageUploadThumbnailer(THUMBNAIL_HEIGHT,
					THUMBNAIL_WIDTH).cropAndScale(mainStream, crop);

			String mimeType = RECOGNIZED_FILE_TYPES.get(".jpg");
			String filename = createThumbnailFilename(mainFilename);
			FileInfo fileInfo = uploadedFileHelper.createFile(filename,
					mimeType, thumbStream);

			log.debug("Created thumbnail: " + fileInfo);
			return fileInfo;
		} catch (FileAlreadyExistsException e) {
			throw new IllegalStateException("Can't create the thumbnail file: "
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new IllegalStateException("Can't create the thumbnail file",
					e);
		} finally {
			if (mainStream != null) {
				try {
					mainStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (thumbStream != null) {
				try {
					thumbStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * If this entity already had a main image, remove it. If the image and the
	 * thumbnail are no longer used by anyone, throw them away.
	 */
	void removeExistingImage(Individual person) {
		uploadedFileHelper.removeMainImage(person);
	}

	/**
	 * Store the image on the entity, and the thumbnail on the image.
	 */
	void storeImageFiles(Individual entity, FileInfo newImage,
			FileInfo thumbnail) {
		uploadedFileHelper.setImagesOnEntity(entity.getURI(), newImage,
				thumbnail);
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
			// If the browser supplied the MIME type, we may need to
			// replace it with the standard value.
			if (NON_STANDARD_MIME_TYPES.containsKey(mimeType)) {
				mimeType = NON_STANDARD_MIME_TYPES.get(mimeType);
			}
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
	 * <p>
	 * This {@link ImagingListener} means that Java Advanced Imaging won't dump
	 * an exception log to {@link System#out}. It writes to the log, instead.
	 * </p>
	 * <p>
	 * Further, since the lack of native accelerator classes isn't an error, it
	 * is written as a simple log message.
	 * </p>
	 */
	static class NonNoisyImagingListener implements ImagingListener {
		@Override
		public boolean errorOccurred(String message, Throwable thrown,
				Object where, boolean isRetryable) throws RuntimeException {
			if (thrown instanceof RuntimeException) {
				throw (RuntimeException) thrown;
			}
			if ((thrown instanceof NoClassDefFoundError)
					&& (thrown.getMessage()
							.contains("com/sun/medialib/mlib/Image"))) {
				log.info("Java Advanced Imaging: Could not find mediaLib "
						+ "accelerator wrapper classes. "
						+ "Continuing in pure Java mode.");
				return false;
			}
			log.error(thrown, thrown);
			return false;
		}

	}
}
