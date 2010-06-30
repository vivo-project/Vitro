/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileModelHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileServingHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;
import freemarker.template.Configuration;

/**
 * Handle adding, replacing or deleting the main image on an Individual.
 */
public class ImageUploadController extends FreeMarkerHttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory
			.getLog(ImageUploadController.class);

	private static final String DEFAULT_NAMESPACE = ConfigurationProperties
			.getProperty("Vitro.defaultNamespace");

	public static final String DUMMY_THUMBNAIL_URL = "/images/dummyImages/person.thumbnail.jpg";

	/** Limit file size to 50 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 50 * 1024 * 1024;
	
	/** Generated thumbnails will be this big. */
	public static final int THUMBNAIL_HEIGHT = 115;
	public static final int THUMBNAIL_WIDTH = 115;

	public static final String PARAMETER_ACTION = "action";
	public static final String PARAMETER_ENTITY_URI = "entityUri";
	public static final String PARAMETER_UPLOADED_FILE = "datafile";

	public static final String ACTION_SAVE = "save";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_DELETE = "delete";

	public static final String BODY_TITLE = "title";
	public static final String BODY_ENTITY_NAME = "entityName";
	public static final String BODY_MAIN_IMAGE_URL = "imageUrl";
	public static final String BODY_THUMBNAIL_URL = "thumbnailUrl";
	public static final String BODY_CANCEL_URL = "cancelUrl";
	public static final String BODY_DELETE_URL = "deleteUrl";
	public static final String BODY_FORM_ACTION = "formAction";
	public static final String BODY_ERROR_MESSAGE = "errorMessage";

	public static final String TEMPLATE_NEW = "imageUpload/newImage.ftl";
	public static final String TEMPLATE_REPLACE = "imageUpload/replaceImage.ftl";
	public static final String TEMPLATE_CROP = "imageUpload/cropImage.ftl";
	public static final String TEMPLATE_BOGUS = "imageUpload/bogus.ftl"; // TODO
	// This
	// is
	// BOGUS!!

	private static final String URL_HERE = UrlBuilder.getUrl("/uploadImages");

	private FileStorage fileStorage;

	/**
	 * When initialized, get a reference to the File Storage system. Without
	 * that, we can do nothing.
	 */
	@Override
	public void init() throws ServletException {
		super.init();
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
	 * <p>
	 * Parse the multi-part request before letting the
	 * {@link FreeMarkerHttpServlet} do its tricks.
	 * </p>
	 * <p>
	 * If the request was a multi-part file upload, it will parse to a
	 * normal-looking request with a "file_item_map" attribute.
	 * </p>
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			FileUploadServletRequest parsedRequest = FileUploadServletRequest
					.parseRequest(request, MAXIMUM_FILE_SIZE);
			if (log.isTraceEnabled()) {
				dumpRequestDetails(parsedRequest);
			}

			super.doGet(parsedRequest, response);

		} catch (FileUploadException e) {
			// Swallow throw an exception here. Test for FILE_ITEM_MAP later.
			log.error("Failed to parse the multi-part HTTP request", e);
		}
	}

	protected String getTitle(String siteName) {
		return "Photo Upload " + siteName;
	}

	/**
	 * Handle the different possible actions - default action is to show the
	 * intro screen.
	 */
	protected String getBody(VitroRequest vreq, Map<String, Object> body,
			Configuration config) {
		String action = vreq.getParameter(PARAMETER_ACTION);
		try {
			Individual entity = validateEntityUri(vreq);

			if (ACTION_UPLOAD.equals(action)) {
				return doUploadImage(vreq, body, config, entity);
			} else if (ACTION_SAVE.equals(action)) {
				return doCreateThumbnail(vreq, body, config, entity);
			} else if (ACTION_DELETE.equals(action)) {
				return doDeleteImage(body, config, entity);
			} else {
				return doIntroScreen(body, config, entity);
			}
		} catch (UserMistakeException e) {
			return showAddImagePageWithError(body, config, null, e.getMessage());
		} catch (Exception e) {
			// We weren't expecting this - dump as much info as possible.
			log.error(e, e);
			return doError(e.toString(), body, config);
		}
	}

	/**
	 * Show the first screen in the upload process: Add or Replace.
	 */
	private String doIntroScreen(Map<String, Object> body,
			Configuration config, Individual entity) {

		String thumbUrl = getThumbnailUrl(entity);

		if (thumbUrl == null) {
			return showAddImagePage(body, config, entity);
		} else {
			return showReplaceImagePage(body, config, entity, thumbUrl);
		}
	}

	/**
	 * The user has selected their main image file. Remove any previous main
	 * image (and thumbnail), and attach the new main image.
	 */
	private String doUploadImage(VitroRequest vreq, Map<String, Object> body,
			Configuration config, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				getWebappDaoFactory());

		// Did they provide a file to upload? If not, show an error.
		FileItem fileItem;
		try {
			fileItem = helper.validateImageFromRequest(vreq);
		} catch (UserMistakeException e) {
			String thumbUrl = getThumbnailUrl(entity);
			String message = e.getMessage();
			if (thumbUrl == null) {
				return showAddImagePageWithError(body, config, entity, message);
			} else {
				return showReplaceImagePageWithError(body, config, entity,
						thumbUrl, message);
			}
		}

		// Remove the old main image (if any) and store the new one.
		helper.removeExistingImage(entity);
		helper.storeMainImageFile(entity, fileItem);

		// The entity Individual is stale - get another one;
		String entityUri = entity.getURI();
		entity = getWebappDaoFactory().getIndividualDao().getIndividualByURI(
				entityUri);

		// Go to the cropping page.
		return showCropImagePage(body, config, entity, getMainImageUrl(entity));
	}

	/**
	 * The user has specified how to crop the thumbnail. Crop it and attach it
	 * to the main image.
	 */
	private String doCreateThumbnail(VitroRequest vreq,
			Map<String, Object> body, Configuration config, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				getWebappDaoFactory());

		validateMainImage(entity);
		CropRectangle crop = validateCropCoordinates(vreq);

		helper.removeExistingThumbnail(entity);
		helper.generateThumbnailAndStore(entity, crop);

		return showIndividualDisplayPage(body, config, entity);
	}

	/**
	 * Delete the main image and the thumbnail from the individual.
	 */
	private String doDeleteImage(Map<String, Object> body,
			Configuration config, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				getWebappDaoFactory());

		helper.removeExistingImage(entity);

		return showIndividualDisplayPage(body, config, entity);
	}

	/**
	 * Display a error message to the user.
	 * 
	 * @message The text of the error message.
	 */
	private String doError(String message, Map<String, Object> body,
			Configuration config) {
		String bodyTemplate = "errorMessage.ftl";
		body.put("errorMessage", message);
		return mergeBodyToTemplate(bodyTemplate, body, config);
	}

	/**
	 * We need to be talking about an actual Individual here.
	 */
	private Individual validateEntityUri(VitroRequest vreq)
			throws UserMistakeException {
		String entityUri = vreq.getParameter(PARAMETER_ENTITY_URI);
		if (entityUri == null) {
			throw new UserMistakeException("No entity URI was provided");
		}

		Individual entity = getWebappDaoFactory().getIndividualDao()
				.getIndividualByURI(entityUri);
		if (entity == null) {
			throw new UserMistakeException(
					"This URI is not recognized as belonging to anyone: '"
							+ entityUri + "'");
		}
		return entity;
	}

	/**
	 * We can't do a thumbnail if there is no main image.
	 */
	private void validateMainImage(Individual entity) {
		if (entity.getMainImageUri() == null) {
			throw new IllegalStateException("Can't store a thumbnail "
					+ "on an individual with no main image: '"
					+ showEntity(entity) + "'");
		}
	}

	/**
	 * Did we get the cropping coordinates?
	 */
	private CropRectangle validateCropCoordinates(VitroRequest vreq) {
		int x = getRequiredIntegerParameter(vreq, "x");
		int y = getRequiredIntegerParameter(vreq, "y");
		int h = getRequiredIntegerParameter(vreq, "h");
		int w = getRequiredIntegerParameter(vreq, "w");
		return new CropRectangle(x, y, h, w);
	}

	/**
	 * We need this parameter on the request, and it must be a valid integer.
	 */
	private int getRequiredIntegerParameter(HttpServletRequest req, String key) {
		String string = req.getParameter(key);
		if (string == null) {
			throw new IllegalStateException(
					"Request did not contain a value for '" + key + "'");
		}
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Value for '" + key
					+ "' was not a valid integer: '" + string + "'");
		}
	}

	/**
	 * Get the URL that will serve this entity's main image, or null.
	 */
	private String getMainImageUrl(Individual entity) {
		String imageUri = FileModelHelper.getMainImageBytestreamUri(entity);
		String imageFilename = FileModelHelper.getMainImageFilename(entity);
		return FileServingHelper.getBytestreamAliasUrl(imageUri, imageFilename);
	}

	/**
	 * Get the URL that will serve this entity's thumbnail image, or null.
	 */
	private String getThumbnailUrl(Individual entity) {
		String thumbUri = FileModelHelper.getThumbnailBytestreamUri(entity);
		String thumbFilename = FileModelHelper.getThumbnailFilename(entity);
		return FileServingHelper.getBytestreamAliasUrl(thumbUri, thumbFilename);
	}

	/**
	 * The individual has no image - go to the Add Image page.
	 * 
	 * @param entity
	 *            if this is null, then all URLs lead to the welcome page.
	 */
	private String showAddImagePage(Map<String, Object> body,
			Configuration config, Individual entity) {
		String formAction = (entity == null) ? "/" : formAction(
				entity.getURI(), ACTION_UPLOAD);
		String cancelUrl = (entity == null) ? "/" : displayPageUrl(entity
				.getURI());

		body.put(BODY_THUMBNAIL_URL, UrlBuilder.getUrl(DUMMY_THUMBNAIL_URL));
		body.put(BODY_FORM_ACTION, formAction);
		body.put(BODY_CANCEL_URL, cancelUrl);
		body.put(BODY_TITLE, "Upload image" + forName(entity));
		return mergeBodyToTemplate(TEMPLATE_NEW, body, config);
	}

	/**
	 * The individual has no image, but the user did something wrong.
	 */
	private String showAddImagePageWithError(Map<String, Object> body,
			Configuration config, Individual entity, String message) {
		body.put(BODY_ERROR_MESSAGE, message);
		return showAddImagePage(body, config, entity);
	}

	/**
	 * The individual has an image - go to the Replace Image page.
	 */
	private String showReplaceImagePage(Map<String, Object> body,
			Configuration config, Individual entity, String thumbUrl) {
		body.put(BODY_THUMBNAIL_URL, UrlBuilder.getUrl(thumbUrl));
		body.put(BODY_DELETE_URL, formAction(entity.getURI(), ACTION_DELETE));
		body.put(BODY_FORM_ACTION, formAction(entity.getURI(), ACTION_UPLOAD));
		body.put(BODY_CANCEL_URL, displayPageUrl(entity.getURI()));
		body.put(BODY_TITLE, "Replace image" + forName(entity));
		return mergeBodyToTemplate(TEMPLATE_REPLACE, body, config);
	}

	/**
	 * The individual has an image, but the user did something wrong.
	 */
	private String showReplaceImagePageWithError(Map<String, Object> body,
			Configuration config, Individual entity, String thumbUrl,
			String message) {
		body.put(BODY_ERROR_MESSAGE, message);
		return showReplaceImagePage(body, config, entity, thumbUrl);
	}

	/**
	 * We got their main image - go to the Crop Image page.
	 */
	private String showCropImagePage(Map<String, Object> body,
			Configuration config, Individual entity, String imageUrl) {
		body.put(BODY_MAIN_IMAGE_URL, UrlBuilder.getUrl(imageUrl));
		body.put(BODY_FORM_ACTION, formAction(entity.getURI(), ACTION_SAVE));
		body.put(BODY_CANCEL_URL, displayPageUrl(entity.getURI()));
		body.put(BODY_TITLE, "Crop Photo" + forName(entity));
		return mergeBodyToTemplate(TEMPLATE_CROP, body, config);
	}

	/**
	 * All done - go to the individual display page.
	 */
	private String showIndividualDisplayPage(Map<String, Object> body,
			Configuration config, Individual entity) {
		return mergeBodyToTemplate(TEMPLATE_BOGUS, body, config);
	}

	/**
	 * When we complete the process, by success or by cancellation, we go to the
	 * individual display page.
	 */
	private String displayPageUrl(String entityUri) {
		if (DEFAULT_NAMESPACE == null) {
			return UrlBuilder.getUrl("");
		} else if (!entityUri.startsWith(DEFAULT_NAMESPACE)) {
			return UrlBuilder.getUrl("");
		} else {
			String tail = entityUri.substring(DEFAULT_NAMESPACE.length());
			if (!tail.startsWith("/")) {
				tail = "/" + tail;
			}
			return UrlBuilder.getUrl("display" + tail);
		}
	}

	/**
	 * The "action" parameter on the HTML "form" tag should include the path
	 * back to this controller, along with the desired action and the Entity
	 * URI.
	 */
	private String formAction(String entityUri, String action) {
		UrlBuilder.Params params = new UrlBuilder.Params(PARAMETER_ENTITY_URI,
				entityUri, PARAMETER_ACTION, action);
		return UrlBuilder.getPath(URL_HERE, params);
	}

	/**
	 * Format an entity for display in a message.
	 */
	private String showEntity(Individual entity) {
		if (entity == null) {
			return String.valueOf(null);
		} else if (entity.getName() == null) {
			return "'no name' (" + entity.getURI() + ")";
		} else {
			return "'" + entity.getName() + "' (" + entity.getURI() + ")";
		}
	}

	/**
	 * Format the entity's name for display as part of the page title.
	 */
	private String forName(Individual entity) {
		if (entity != null) {
			String name = entity.getName();
			if (name != null) {
				return " for " + name;
			}
		}
		return "";
	}

	/**
	 * Holds an error message to use as a complaint to the user.
	 */
	static class UserMistakeException extends Exception {
		UserMistakeException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the coordinates that we use to crop the main image.
	 */
	static class CropRectangle {
		final int x;
		final int y;
		final int height;
		final int width;

		private CropRectangle(int x, int y, int height, int width) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
		}

	}

	/**
	 * For debugging, dump all sorts of information about the request.
	 * 
	 * WARNING: if this request represents a Multi-part request which has not
	 * yet been parsed, just reading these parameters will consume them.
	 */
	@SuppressWarnings("unchecked")
	private void dumpRequestDetails(HttpServletRequest req) {
		log.trace("Request is " + req.getClass().getName());

		Map<String, String[]> parms = req.getParameterMap();
		for (Entry<String, String[]> entry : parms.entrySet()) {
			log.trace("Parameter '" + entry.getKey() + "'="
					+ Arrays.deepToString(entry.getValue()));
		}

		Enumeration<String> attrs = req.getAttributeNames();
		while (attrs.hasMoreElements()) {
			String key = attrs.nextElement();
			String valueString = String.valueOf(req.getAttribute(key));
			String valueOneLine = valueString.replace("\n", " | ");
			log.trace("Attribute '" + key + "'=" + valueOneLine);
		}
	}
}
