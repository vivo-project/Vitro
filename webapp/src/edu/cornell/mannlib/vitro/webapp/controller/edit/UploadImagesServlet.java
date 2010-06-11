/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileAlreadyExistsException;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;

public class UploadImagesServlet extends VitroHttpServlet {
	private static final Logger log = Logger
			.getLogger(UploadImagesServlet.class);

	/** Recognized file extensions mapped to MIME-types. */
	private static final Map<String, String> RECOGNIZED_FILE_TYPES = createFileTypesMap();

	/** The field in the HTTP request that holds the file. */
	public static final String FILE_FIELD_NAME = "file1";

	/** The field in the HTTP request that holds the individual's URI. */
	public static final String URI_FIELD_NAME = "entityUri";

	/** Limit file size to 50 megabytes. */
	private static final int MAXIMUM_FILE_SIZE = 50 * 1024 * 1024;

	/** How wide should a generated thumbnail image be (in pixels)? */
	private static final int THUMBNAIL_WIDTH = 150;

	/** How high should a generated thumbnail image be (in pixels)? */
	private static final int THUMBNAIL_HEIGHT = 150;

	private FileStorage fileStorage;

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
	 * On startup, get a reference to the {@link FileStorage} system from the
	 * {@link ServletContext}.
	 * 
	 * @throws UnavailableException
	 *             if the attribute is missing, or is not of the correct type.
	 */
	@Override
	public void init() throws ServletException {
		Object o = getServletContext().getAttribute(
				FileStorageSetup.ATTRIBUTE_NAME);
		if (o instanceof FileStorage) {
			fileStorage = (FileStorage) o;
		} else if (o == null) {
			throw new UnavailableException(this.getClass().getSimpleName()
					+ " could not initialize. Attribute '"
					+ FileStorageSetup.ATTRIBUTE_NAME
					+ "' was not set in the servlet context.");
		} else {
			throw new UnavailableException(this.getClass().getSimpleName()
					+ " could not initialize. Attribute '"
					+ FileStorageSetup.ATTRIBUTE_NAME
					+ "' in the servlet context contained an instance of '"
					+ o.getClass().getName() + "' instead of '"
					+ FileStorage.class.getName() + "'");
		}
	}

	/**
	 * Treat a GET request like a POST request. However, since a GET request
	 * cannot contain uploaded files, this should produce an error.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * <pre>
	 * doPost()
	 *   Do as much as possible before we begin updating the model:
	 *     read the file
	 *     get the mime-type
	 *     be sure that we have a Person to associate with
	 *     generate the thumbnail and assign a filename and mime-type
	 *   If they already have a mainImage:
	 *     check to see whether anyone else is associated with that image.
	 *       if not, delete it -- remove the file, remove the individuals.
	 *   Update the model
	 *     for the thumbnail:
	 *       create the File and FileByteStream individuals
	 *       store the file in the file system
	 *       add properties to the File (surrogate)
	 *         filename, mimeType, downloadLocation
	 *     for the main image:
	 *       create the File and FileByteStream individuals
	 *       store the file in the file system
	 *       add properties to the File (surrogate)
	 *         filename, mimeType, downloadLocation
	 *       add property thumbnailImage
	 *     set main image as mainImage in the person.
	 * </pre>
	 */
	/**
	 * <p>
	 * Store an image file as the main image for the associated individual. The
	 * request must be a multi-part request containing the file. It must also
	 * contain a parameter for the URI of the individual.
	 * </p>
	 * <p>
	 * If the individual already has a main image, it will be removed. The new
	 * image is stored, and a thumbnail is generated and stored.
	 * </p>
	 */
	@Override
	protected void doPost(HttpServletRequest rawRequest,
			HttpServletResponse response) throws ServletException, IOException {
		List<String> errors = new ArrayList<String>();

		try {
			VitroRequest request = new VitroRequest(FileUploadServletRequest
					.parseRequest(rawRequest, MAXIMUM_FILE_SIZE));
			try {
				FileItem imageFileItem = validateImageFromRequest(request);
				Individual person = validateEntityUriFromRequest(request);

				removeExistingImage(person, request);
				storeMainImageFile(person, imageFileItem);
				generateThumbnailAndStore(person, imageFileItem);

				displaySuccess(request, response, person);
			} catch (UserErrorException e) {
				// No need to log it - it's a user error.
				errors.add(e.getMessage());
				displayFailure(request, response, errors);
			} catch (IllegalStateException e) {
				log.error(e);
				errors.add(e.getMessage());
				displayFailure(request, response, errors);
			}
		} catch (FileUploadException e) {
			log.error(e);
			errors.add(e.getMessage());
			displayFailure(rawRequest, response, errors);
		}
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
	 * @throws UserErrorException
	 *             if there is no file, if it is empty, or if it is not an image
	 *             file.
	 */
	@SuppressWarnings("unchecked")
	private FileItem validateImageFromRequest(HttpServletRequest request)
			throws UserErrorException {
		Map<String, List<FileItem>> map = (Map<String, List<FileItem>>) request
				.getAttribute(FileUploadServletRequest.FILE_ITEM_MAP);
		List<FileItem> list = map.get(FILE_FIELD_NAME);
		if ((list == null) || list.isEmpty()) {
			throw new UserErrorException("The form did not contain a '"
					+ FILE_FIELD_NAME + "' field.");
		}

		FileItem file = list.get(0);
		if (file.getSize() == 0) {
			throw new UserErrorException("No file was uploaded in '"
					+ FILE_FIELD_NAME + "'");
		}

		String filename = getSimpleFilename(file);
		String mimeType = getMimeType(file);
		if (!RECOGNIZED_FILE_TYPES.containsValue(mimeType)) {
			throw new UserErrorException("'" + filename
					+ "' is not a recognized image file type. "
					+ "These are the recognized types: "
					+ RECOGNIZED_FILE_TYPES);
		}

		return file;
	}

	/**
	 * The entity URI must be present and non-empty, and must refer to an
	 * existing individual.
	 * 
	 * @throws UserErrorException
	 *             if there is no entity URI, or if it is empty.
	 */
	private Individual validateEntityUriFromRequest(VitroRequest request)
			throws UserErrorException {
		String entityUri = request.getParameter(URI_FIELD_NAME);
		if (entityUri == null) {
			throw new UserErrorException("The form did not contain a '"
					+ URI_FIELD_NAME + "' field.");
		}
		entityUri = entityUri.trim();
		if (entityUri.length() == 0) {
			throw new UserErrorException("The form did not contain a '"
					+ URI_FIELD_NAME + "' field.");
		}

		Individual entity = request.getWebappDaoFactory().getIndividualDao()
				.getIndividualByURI(entityUri);
		if (entity == null) {
			throw new UserErrorException(
					"No entity exists with the provided URI: '" + entityUri
							+ "'");
		}
		return entity;
	}

	/**
	 * If the Individual already has a main image, remove it. If nobody else is
	 * referring to that image, delete it.
	 */
	private void removeExistingImage(Individual person, VitroRequest request) {
		OntModel model = request.getJenaOntModel();
		IndividualDao individualDao = getWebappDaoFactory().getIndividualDao();

		// No existing image? Nothing to do.
		String existingImageUri = person.getMainImageUri();
		if (existingImageUri == null) {
			return;
		}

		// Remove the reference to the existing image.
		person.setMainImageUri(null);
		individualDao.updateIndividual(person);

		// If anyone else is using the image, we are done.
		if (isBeingUsed(model, existingImageUri)) {
			return;
		}

		// Nobody is using the image. Delete the image and its thumbnail.
		Individual existingImage = individualDao
				.getIndividualByURI(existingImageUri);
		Individual existingThumbnail = getRelatedIndividual(existingImage,
				VitroVocabulary.FS_THUMBNAIL_IMAGE);
		if (existingThumbnail != null) {
			deleteStoredFile(existingThumbnail, individualDao);
		}
		deleteStoredFile(existingImage, individualDao);
	}

	/**
	 * Store this image in the model and in the file storage system, and set it
	 * as the main image for this person.
	 */
	private void storeMainImageFile(Individual person, FileItem imageFileItem) {
		InputStream inputStream = null;
		try {
			inputStream = imageFileItem.getInputStream();
			String mimeType = getMimeType(imageFileItem);
			String filename = getSimpleFilename(imageFileItem);

			// Create the file individuals in the model
			Individual byteStream = createByteStreamIndividual();
			Individual file = createFileIndividual(mimeType, filename,
					byteStream);

			// Store the file in the FileStorage system.
			fileStorage.createFile(byteStream.getURI(), filename, inputStream);

			// Set the file as the main image for the person.
			person.setMainImageUri(file.getURI());
			getWebappDaoFactory().getIndividualDao().updateIndividual(person);
		} catch (FileAlreadyExistsException e) {
			throw new IllegalStateException("Can't create the image file: "
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new IllegalStateException("Can't create the image file", e);
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
	 * Generate the thumbnail image from the original, store in the model and in
	 * the file storage system, and set it as the thumbnail on the main image.
	 */
	private void generateThumbnailAndStore(Individual person,
			FileItem imageFileItem) {

		InputStream inputStream = null;
		try {
			inputStream = scaleImageForThumbnail(
					imageFileItem.getInputStream(), THUMBNAIL_WIDTH,
					THUMBNAIL_HEIGHT);
			String mimeType = RECOGNIZED_FILE_TYPES.get(".jpg");
			String filename = createThumbnailFilename(getSimpleFilename(imageFileItem));

			// Create the file individuals in the model
			Individual byteStream = createByteStreamIndividual();
			Individual file = createFileIndividual(mimeType, filename,
					byteStream);

			// Store the file in the FileStorage system.
			fileStorage.createFile(byteStream.getURI(), filename, inputStream);

			// Set the file as the thumbnail on the main image for the person.
			String mainImageUri = person.getMainImageUri();
			getWebappDaoFactory().getObjectPropertyStatementDao()
					.insertNewObjectPropertyStatement(
							new ObjectPropertyStatementImpl(mainImageUri,
									VitroVocabulary.FS_THUMBNAIL_IMAGE, file
											.getURI()));
		} catch (FileAlreadyExistsException e) {
			throw new IllegalStateException("Can't create the image file: "
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new IllegalStateException("Can't create the image file", e);
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
	 * Everything went fine. Forward back to the JSP.
	 */
	private void displaySuccess(VitroRequest request,
			HttpServletResponse response, Individual person)
			throws ServletException, IOException {
		try {
			request.setAttribute(URI_FIELD_NAME, person.getURI());

			String individualURI = person.getURI();
			String recordName = person.getName();
			if ((recordName == null) || recordName.isEmpty()) {
				recordName = "here";
			}
			request.setAttribute("processError",
					"updated individual <a href=\"entity?uri="
							+ java.net.URLEncoder
									.encode(individualURI, "UTF-8") + "\">"
							+ recordName + "</a>");
			request.setAttribute("outputLink", "<img src='"
					+ person.getMainImageUri() + "'>");
			getServletContext().getRequestDispatcher("/uploadimages.jsp")
					.forward(request, response);
		} catch (UnsupportedEncodingException e) {
			log.error("This can't happen.", e);
		}
	}

	/**
	 * Problem! Format the error messages and forward back to the JSP.
	 */
	private void displayFailure(HttpServletRequest request,
			HttpServletResponse response, List<String> errors)
			throws ServletException, IOException {
		String entityUri = request.getParameter(URI_FIELD_NAME);
		request.setAttribute(URI_FIELD_NAME, entityUri);

		StringBuilder formatted = new StringBuilder();
		for (String error : errors) {
			if (formatted.length() > 0) {
				formatted.append("<br/>");
			}
			formatted.append(error);
		}

		request.setAttribute("processError", formatted.toString());
		getServletContext().getRequestDispatcher("/uploadimages.jsp").forward(
				request, response);
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
	 * Is this individual referred to by anyone else in the model?
	 */
	private boolean isBeingUsed(OntModel model, String individualUri) {
		Resource individual = model.getResource(individualUri);
		StmtIterator stmts = model.listStatements(null, null, individual);
		try {
			return stmts.hasNext();
		} finally {
			stmts.close();
		}
	}

	/**
	 * Delete the file, remove the FileByteStream and the File from the model.
	 */
	private void deleteStoredFile(Individual file, IndividualDao individualDao) {
		Individual byteStream = getRelatedIndividual(file,
				VitroVocabulary.FS_DOWNLOAD_LOCATION);
		if (byteStream == null) {
			throw new IllegalStateException(
					"Failed to delete the existing image: "
							+ "no byteStream individual attached to file '"
							+ file.getUrl() + "'");
		}

		try {
			fileStorage.deleteFile(byteStream.getUrl());
		} catch (IOException e) {
			throw new IllegalStateException(
					"Failed to delete the existing image file.", e);
		}

		individualDao.deleteIndividual(byteStream);
		individualDao.deleteIndividual(file);
	}

	/**
	 * Inspect the object properties on this individual and find the object of
	 * the specified property.
	 * 
	 * @return the object of the first such property, or <code>null</code>.
	 */
	private Individual getRelatedIndividual(Individual individual,
			String propertyUri) {
		List<ObjectPropertyStatement> opStmts = individual
				.getObjectPropertyStatements();
		for (ObjectPropertyStatement opStmt : opStmts) {
			if (opStmt.getPropertyURI().equals(propertyUri)) {
				return opStmt.getObject();
			}
		}
		return null;
	}

	/**
	 * Create an individual in the model to represent the file bytestream.
	 */
	private Individual createByteStreamIndividual() {
		IndividualDao individualDao = getWebappDaoFactory().getIndividualDao();

		Individual byteStream = new IndividualImpl();
		byteStream.setVClassURI(VitroVocabulary.FS_BYTESTREAM_CLASS);
		String uri = null;
		try {
			uri = individualDao.insertNewIndividual(byteStream);
		} catch (InsertException e) {
			throw new IllegalStateException(
					"Failed to create the bytestream individual.", e);
		}

		return individualDao.getIndividualByURI(uri);
	}

	/**
	 * Create a file surrogate individual in the model.
	 */
	private Individual createFileIndividual(String mimeType, String filename,
			Individual byteStream) {
		IndividualDao individualDao = getWebappDaoFactory().getIndividualDao();
		DataPropertyStatementDao dataPropertyStatementDao = getWebappDaoFactory()
				.getDataPropertyStatementDao();
		ObjectPropertyStatementDao objectPropertyStatementDao = getWebappDaoFactory()
				.getObjectPropertyStatementDao();

		Individual file = new IndividualImpl();
		file.setVClassURI(VitroVocabulary.FS_FILE_CLASS);

		String uri = null;
		try {
			uri = individualDao.insertNewIndividual(file);
		} catch (InsertException e) {
			throw new IllegalStateException(
					"Failed to create the file individual.", e);
		}

		dataPropertyStatementDao
				.insertNewDataPropertyStatement(new DataPropertyStatementImpl(
						uri, VitroVocabulary.FS_FILENAME, filename));
		dataPropertyStatementDao
				.insertNewDataPropertyStatement(new DataPropertyStatementImpl(
						uri, VitroVocabulary.FS_MIME_TYPE, mimeType));
		objectPropertyStatementDao
				.insertNewObjectPropertyStatement(new ObjectPropertyStatementImpl(
						uri, VitroVocabulary.FS_DOWNLOAD_LOCATION, byteStream
								.getURI()));

		return individualDao.getIndividualByURI(uri);
	}

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
	 * Indicates that we should complain to the user.
	 */
	private static class UserErrorException extends Exception {
		UserErrorException(String message) {
			super(message);
		}
	}
}
