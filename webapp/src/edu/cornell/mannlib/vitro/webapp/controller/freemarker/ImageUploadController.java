/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest.UNAUTHORIZED;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ForwardResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor.CropRectangle;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor.Dimensions;
import edu.cornell.mannlib.vitro.webapp.web.images.PlaceholderUtil;

/**
 * Handle adding, replacing or deleting the main image on an Individual.
 */
public class ImageUploadController extends FreemarkerHttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory
			.getLog(ImageUploadController.class);

	private static final String ATTRIBUTE_REFERRING_PAGE = "ImageUploadController.referringPage";

	private static final String ERROR_CODE_UNRECOGNIZED_URI = "imageUpload.errorUnrecognizedURI";
	private static final String ERROR_CODE_NO_URI = "imageUpload.errorNoURI";
	private static final String ERROR_CODE_FILE_TOO_BIG = "imageUpload.errorFileTooBig";

	/** Limit file size to 6 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 6 * 1024 * 1024;

	/** Generated thumbnails will be this big. */
	public static final int THUMBNAIL_HEIGHT = 200;
	public static final int THUMBNAIL_WIDTH = 200;

	/** The form field that tells what we are doing: uploading? deleting? */
	public static final String PARAMETER_ACTION = "action";

	/** The form field that identifies the Individual. */
	public static final String PARAMETER_ENTITY_URI = "entityUri";

	/** The form field of the uploaded file; use as a key to the FileItem map. */
	public static final String PARAMETER_UPLOADED_FILE = "datafile";

	/** Here is the main image file. Hold on to it. */
	public static final String ACTION_UPLOAD = "upload";

	/** Here is the cropping info; we're ready to save the image. */
	public static final String ACTION_SAVE = "save";

	/** A request to delete the file and return to the referring page. */
	public static final String ACTION_DELETE = "delete";

	/** A request to delete the file and return to the "new image" screen. */
	public static final String ACTION_DELETE_EDIT = "deleteEdit";

	public static final String BODY_TITLE = "title";
	public static final String BODY_ENTITY_NAME = "entityName";
	public static final String BODY_MAIN_IMAGE_URL = "imageUrl";
	public static final String BODY_MAIN_IMAGE_HEIGHT = "imageHeight";
	public static final String BODY_MAIN_IMAGE_WIDTH = "imageWidth";
	public static final String BODY_THUMBNAIL_URL = "thumbnailUrl";
	public static final String BODY_CANCEL_URL = "cancelUrl";
	public static final String BODY_DELETE_URL = "deleteUrl";
	public static final String BODY_FORM_ACTION = "formAction";
	public static final String BODY_ERROR_MESSAGE = "errorMessage";
	public static final String BODY_MAX_FILE_SIZE = "maxFileSize";
	public static final String BODY_THUMBNAIL_WIDTH = "thumbnailWidth";
	public static final String BODY_THUMBNAIL_HEIGHT = "thumbnailHeight";

	public static final String TEMPLATE_NEW = "imageUpload-newImage.ftl";
	public static final String TEMPLATE_REPLACE = "imageUpload-replaceImage.ftl";
	public static final String TEMPLATE_CROP = "imageUpload-cropImage.ftl";
	public static final String TEMPLATE_ERROR = "error-standard.ftl";

	private static final String URL_HERE = UrlBuilder.getUrl("/uploadImages");

	private static final String TEXT_STRING_UPLOAD_TITLE = "upload_page_title";
	private static final String TEXT_STRING_UPLOAD_TITLE_WITH_NAME = "upload_page_title_with_name";
	private static final String TEXT_STRING_REPLACE_TITLE = "replace_page_title";
	private static final String TEXT_STRING_REPLACE_TITLE_WITH_NAME = "replace_page_title_with_name";
	private static final String TEXT_STRING_CROP_TITLE = "crop_page_title";
	private static final String TEXT_STRING_CROP_TITLE_WITH_NAME = "crop_page_title_with_name";

	private FileStorage fileStorage;

	/**
	 * When initialized, get a reference to the File Storage system. Without
	 * that, we can do nothing.
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		fileStorage = ApplicationUtils.instance().getFileStorage();
	}
	
	/**
	 * How large an image file will we accept?
	 */
	@Override
	public long maximumMultipartFileSize() {
		return MAXIMUM_FILE_SIZE;
	}

	/**
	 * What will we do if there is a problem parsing the request?
	 */
	@Override
	public boolean stashFileSizeException() {
		return true;
	}

	/**
	 * The required action depends on what we are trying to do.
	 */
	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		try {
			String action = vreq.getParameter(PARAMETER_ACTION);
			Individual entity = validateEntityUri(vreq);
			String imageUri = entity.getMainImageUri();
			
			Property indMainImage = new Property();
			indMainImage.setURI(VitroVocabulary.IND_MAIN_IMAGE);

			RequestedAction ra;
			if (ACTION_DELETE.equals(action)
					|| ACTION_DELETE_EDIT.equals(action)) {
				ra = new DropObjectPropertyStatement(vreq.getJenaOntModel(),
						entity.getURI(), indMainImage,
						imageUri);
			} else if (imageUri != null) {
				ra = new EditObjectPropertyStatement(vreq.getJenaOntModel(),
						entity.getURI(), indMainImage,
						imageUri);
			} else {
				ra = new AddObjectPropertyStatement(vreq.getJenaOntModel(),
						entity.getURI(), indMainImage,
						RequestedAction.SOME_URI);
			}
			return ra;
		} catch (UserMistakeException e) {
			return UNAUTHORIZED;
		}
	}

	/**
	 * Handle the different actions. If not specified, the default action is to
	 * show the intro screen.
	 */
	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Individual entity = null;
		try {
			entity = validateEntityUri(vreq);
			
			checkForFileTooBigException(vreq);
			
			String action = vreq.getParameter(PARAMETER_ACTION);
			if (ACTION_UPLOAD.equals(action)) {
				return doUploadImage(vreq, entity);
			} else if (ACTION_SAVE.equals(action)) {
				return doCreateThumbnail(vreq, entity);
			} else if (ACTION_DELETE.equals(action)) {
				captureReferringUrl(vreq);
				return doDeleteImage(vreq, entity);
			} else if (ACTION_DELETE_EDIT.equals(action)) {
				return doDeleteThenEdit(vreq, entity);
			} else {
				captureReferringUrl(vreq);
				return doIntroScreen(vreq, entity);
			}
		} catch (UserMistakeException e) {
			// Can't find the entity? Image too large? Complain.
			return showAddImagePageWithError(vreq, entity, e.formatMessage(vreq));
		} catch (Exception e) {
			// We weren't expecting this - log it, and apologize to the user.
			return new ExceptionResponseValues(e);
		}
	}

	/**
	 * If our exception handler caught a "file too big" exception, we need to
	 * deal with it before anything else, since we can't trust the other
	 * parameters.
	 */
	private void checkForFileTooBigException(VitroRequest vreq)
			throws UserMistakeException {
		if (vreq.hasFileSizeException()) {
			int limit = MAXIMUM_FILE_SIZE / (1024 * 1024);
			throw new UserMistakeException(ERROR_CODE_FILE_TOO_BIG, limit);
		}
	}

	/**
	 * We are just starting the upload process. Record where we came from, so if
	 * they hit "cancel" we know where to send them. If we have problems, just
	 * clear it.
	 */
	private void captureReferringUrl(VitroRequest vreq) {
		String referrer = vreq.getHeader("Referer");
		if (referrer == null) {
			vreq.getSession().removeAttribute(ATTRIBUTE_REFERRING_PAGE);
		} else {
			vreq.getSession().setAttribute(ATTRIBUTE_REFERRING_PAGE, referrer);
		}
	}

	/**
	 * Show the first screen in the upload process: Add or Replace.
	 */
	private ResponseValues doIntroScreen(VitroRequest vreq, Individual entity) {
		ImageInfo imageInfo = ImageInfo.instanceFromEntityUri(
				vreq.getUnfilteredWebappDaoFactory(), entity);
		if (imageInfo == null) {
			return showAddImagePage(vreq, entity);
		} else {
			return showReplaceImagePage(vreq, entity, imageInfo);
		}
	}

	/**
	 * The user has selected their main image file. Remove any previous main
	 * image (and thumbnail), and attach the new main image.
	 */
	private ResponseValues doUploadImage(VitroRequest vreq, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getUnfilteredWebappDaoFactory(), getServletContext());

		try {
			// Did they provide a file to upload? If not, show an error.
			FileItem fileItem = helper.validateImageFromRequest(vreq);

			// Put it in the file system, and store a reference in the session.
			FileInfo fileInfo = helper.storeNewImage(fileItem, vreq);

			// How big is the new image? If not big enough, show an error.
			Dimensions size = helper.getNewImageSize(fileInfo);

			// Go to the cropping page.
			return showCropImagePage(vreq, entity,
					fileInfo.getBytestreamAliasUrl(), size);
		} catch (UserMistakeException e) {
			return showErrorMessage(vreq, entity, e.formatMessage(vreq));
		}
	}

	/**
	 * Are we writing the error message to the "Add" page or to the "Replace"
	 * page?
	 */
	private ResponseValues showErrorMessage(VitroRequest vreq,
			Individual entity, String message) {

		ImageInfo imageInfo = ImageInfo.instanceFromEntityUri(
				vreq.getUnfilteredWebappDaoFactory(), entity);
		if (imageInfo == null) {
			return showAddImagePageWithError(vreq, entity, message);
		} else {
			return showReplaceImagePageWithError(vreq, entity, imageInfo,
					message);
		}
	}

	/**
	 * The user has specified how to crop the thumbnail. Crop it and attach it
	 * to the main image.
	 */
	private ResponseValues doCreateThumbnail(VitroRequest vreq,
			Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getUnfilteredWebappDaoFactory(), getServletContext());

		try {
			CropRectangle crop = validateCropCoordinates(vreq);
			FileInfo newImage = helper.getNewImageInfo(vreq);
			FileInfo thumbnail = helper.generateThumbnail(crop, newImage);

			helper.removeExistingImage(entity);
			helper.storeImageFiles(entity, newImage, thumbnail);

			return showExitPage(vreq, entity);
		} catch (UserMistakeException e) {
			return showErrorMessage(vreq, entity, e.formatMessage(vreq));
		}
	}

	/**
	 * Delete the main image and the thumbnail, and go back to the referring
	 * page.
	 */
	private ResponseValues doDeleteImage(VitroRequest vreq, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getUnfilteredWebappDaoFactory(), getServletContext());

		helper.removeExistingImage(entity);

		return showExitPage(vreq, entity);
	}

	/**
	 * Delete the main image and the thumbnail, and go to the "add image"
	 * screen.
	 */
	private ResponseValues doDeleteThenEdit(VitroRequest vreq, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getUnfilteredWebappDaoFactory(), getServletContext());

		helper.removeExistingImage(entity);

		return showAddImagePage(vreq, entity);
	}

	/**
	 * We need to be talking about an actual Individual here.
	 */
	private Individual validateEntityUri(VitroRequest vreq)
			throws UserMistakeException {
		String entityUri = vreq.getParameter(PARAMETER_ENTITY_URI);
		if (entityUri == null) {
			throw new UserMistakeException(ERROR_CODE_NO_URI);
		}

		Individual entity = vreq.getUnfilteredWebappDaoFactory().getIndividualDao()
				.getIndividualByURI(entityUri);
		if (entity == null) {
			throw new UserMistakeException(ERROR_CODE_UNRECOGNIZED_URI,
					entityUri);
		}
		return entity;
	}

	/**
	 * Did we get the cropping coordinates?
	 */
	private CropRectangle validateCropCoordinates(VitroRequest vreq) {
		int x = getIntegerParameter(vreq, "x", 0);
		int y = getIntegerParameter(vreq, "y", 0);
		int h = getIntegerParameter(vreq, "h", THUMBNAIL_HEIGHT);
		int w = getIntegerParameter(vreq, "w", THUMBNAIL_WIDTH);
		return new CropRectangle(x, y, h, w);
	}

	/**
	 * We need this parameter on the request, and it must be a valid integer.
	 */
	private int getIntegerParameter(HttpServletRequest req, String key,
			int defaultValue) {
		String string = req.getParameter(key);
		if ((string == null) || (string.isEmpty())) {
			log.debug("No value for '" + key + "'; using default value = "
					+ defaultValue);
			return defaultValue;
		}
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			log.warn("Value for '" + key + "' was not a valid integer: '"
					+ string + "'; using default value = " + defaultValue);
			return defaultValue;
		}
	}

	/**
	 * The individual has no image - go to the Add Image page.
	 * 
	 * @param entity
	 *            if this is null, then all URLs lead to the welcome page.
	 */
	private TemplateResponseValues showAddImagePage(VitroRequest vreq,
			Individual entity) {

		String formAction = (entity == null) ? "" : formAction(entity.getURI(),
				ACTION_UPLOAD);
		String cancelUrl = (entity == null) ? "" : exitPageUrl(vreq,
				entity.getURI());
		String placeholderUrl = (entity == null) ? "" : UrlBuilder
				.getUrl(PlaceholderUtil.getPlaceholderImagePathForIndividual(
						vreq, entity.getURI()));

		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE_NEW);

		rv.put(BODY_THUMBNAIL_URL, placeholderUrl);
		rv.put(BODY_FORM_ACTION, formAction);
		rv.put(BODY_CANCEL_URL, cancelUrl);
		rv.put(BODY_TITLE, figureUploadPageTitle(vreq, entity));
		rv.put(BODY_MAX_FILE_SIZE, MAXIMUM_FILE_SIZE / (1024 * 1024));
		rv.put(BODY_THUMBNAIL_HEIGHT, THUMBNAIL_HEIGHT);
		rv.put(BODY_THUMBNAIL_WIDTH, THUMBNAIL_WIDTH);
		return rv;
	}

	/**
	 * The individual has no image, but the user did something wrong.
	 */
	private TemplateResponseValues showAddImagePageWithError(VitroRequest vreq,
			Individual entity, String message) {
		return showAddImagePage(vreq, entity).put(BODY_ERROR_MESSAGE, message);
	}

	/**
	 * The individual has an image - go to the Replace Image page.
	 */
	private TemplateResponseValues showReplaceImagePage(VitroRequest vreq,
			Individual entity, ImageInfo imageInfo) {
		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE_REPLACE);
		rv.put(BODY_THUMBNAIL_URL, UrlBuilder.getUrl(imageInfo.getThumbnail()
				.getBytestreamAliasUrl()));
		rv.put(BODY_DELETE_URL, formAction(entity.getURI(), ACTION_DELETE_EDIT));
		rv.put(BODY_FORM_ACTION, formAction(entity.getURI(), ACTION_UPLOAD));
		rv.put(BODY_CANCEL_URL, exitPageUrl(vreq, entity.getURI()));
		rv.put(BODY_TITLE, figureReplacePageTitle(vreq, entity));
		rv.put(BODY_MAX_FILE_SIZE, MAXIMUM_FILE_SIZE / (1024 * 1024));
		rv.put(BODY_THUMBNAIL_HEIGHT, THUMBNAIL_HEIGHT);
		rv.put(BODY_THUMBNAIL_WIDTH, THUMBNAIL_WIDTH);
		return rv;
	}

	/**
	 * The individual has an image, but the user did something wrong.
	 */
	private TemplateResponseValues showReplaceImagePageWithError(
			VitroRequest vreq, Individual entity, ImageInfo imageInfo,
			String message) {
		TemplateResponseValues rv = showReplaceImagePage(vreq, entity,
				imageInfo);
		rv.put(BODY_ERROR_MESSAGE, message);
		return rv;
	}

	/**
	 * We got their main image - go to the Crop Image page.
	 */
	private TemplateResponseValues showCropImagePage(VitroRequest vreq,
			Individual entity, String imageUrl, Dimensions dimensions) {
		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE_CROP);
		rv.put(BODY_MAIN_IMAGE_URL, UrlBuilder.getUrl(imageUrl));
		rv.put(BODY_MAIN_IMAGE_HEIGHT, dimensions.height);
		rv.put(BODY_MAIN_IMAGE_WIDTH, dimensions.width);
		rv.put(BODY_FORM_ACTION, formAction(entity.getURI(), ACTION_SAVE));
		rv.put(BODY_CANCEL_URL, exitPageUrl(vreq, entity.getURI()));
		rv.put(BODY_TITLE, figureCropPageTitle(vreq, entity));
		return rv;
	}

	/**
	 * All done - go to the individual display page.
	 */
	private ForwardResponseValues showExitPage(VitroRequest vreq,
			Individual entity) {
		return new ForwardResponseValues(exitPageUrl(vreq, entity.getURI()));
	}

	/**
	 * When we complete the process, by success or by cancellation, go to the
	 * initial referring page. If there wasn't one, go to the individual display
	 * page.
	 */
	private String exitPageUrl(VitroRequest vreq, String entityUri) {
		String referrer = (String) vreq.getSession().getAttribute(
				ATTRIBUTE_REFERRING_PAGE);
		if (referrer != null) {
			return referrer;
		}

		String defaultNamespace = getDefaultNamespace();
		if (defaultNamespace == null) {
			return "";
		} else if (!entityUri.startsWith(defaultNamespace)) {
			return "";
		} else {
			String tail = entityUri.substring(defaultNamespace.length());
			if (!tail.startsWith("/")) {
				tail = "/" + tail;
			}
			return "display" + tail;
		}
	}

	/**
	 * The "action" parameter on the HTML "form" tag should include the path
	 * back to this controller, along with the desired action and the Entity
	 * URI.
	 */
	private String formAction(String entityUri, String action) {
		ParamMap params = new ParamMap(PARAMETER_ENTITY_URI, entityUri,
				PARAMETER_ACTION, action);
		return UrlBuilder.getPath(URL_HERE, params);
	}

	/**
	 * Format the title for the Upload page.
	 */
	private String figureUploadPageTitle(HttpServletRequest req,
			Individual entity) {
		return figurePageTitle(req, entity, TEXT_STRING_UPLOAD_TITLE,
				TEXT_STRING_UPLOAD_TITLE_WITH_NAME);
	}

	/**
	 * Format the title for the Replace page.
	 */
	private String figureReplacePageTitle(HttpServletRequest req,
			Individual entity) {
		return figurePageTitle(req, entity, TEXT_STRING_REPLACE_TITLE,
				TEXT_STRING_REPLACE_TITLE_WITH_NAME);
	}

	/**
	 * Format the title for the Crop page.
	 */
	private String figureCropPageTitle(HttpServletRequest req, Individual entity) {
		return figurePageTitle(req, entity, TEXT_STRING_CROP_TITLE,
				TEXT_STRING_CROP_TITLE_WITH_NAME);
	}

	/**
	 * Format one of two page titles, depending on whether the entity has a
	 * name.
	 */
	private String figurePageTitle(HttpServletRequest req, Individual entity,
			String noNameTitleKey, String nameTitleKey) {
		if (entity != null) {
			String name = entity.getName();
			if (name != null) {
				return I18n.text(req, nameTitleKey, name);
			}
		}
		return I18n.text(req, noNameTitleKey);
	}

	/**
	 * Holds an error message to use as a complaint to the user.
	 */
	static class UserMistakeException extends Exception {
		private final Object[] parameters;

		UserMistakeException(String message, Object... parameters) {
			super(message);
			this.parameters = parameters;
		}

		public String formatMessage(HttpServletRequest req) {
			return I18n.text(req, getMessage(), parameters);
		}
	}

	private String getDefaultNamespace() {
		return ConfigurationProperties.getBean(getServletContext())
				.getProperty("Vitro.defaultNamespace");
	}
}
