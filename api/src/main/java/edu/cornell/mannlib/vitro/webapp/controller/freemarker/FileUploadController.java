package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.PARAMETER_UPLOADED_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;

@WebServlet(name = "FileUploadController", urlPatterns = { "/uploadFile" })
public class FileUploadController extends FreemarkerHttpServlet {
	private static final String REFERER_HEADER = "Referer";
	private static final String TEMPLATE_VAR_FORM_ACTION = "formAction";
	private static final String TEMPLATE_VAR_MAX_FILE_SIZE = "maxFileSize";
	private static final String PARAM_REFERRER = "referrer";
	private static final String TEMPLATE_VAR_SUPPORTED_MEDIA_TYPES = "supportedMediaTypes";

	private static final String TEMPLATE_VAR_ERROR_MESSAGE = "errorMessage";
	private static final String DEFAULT_FILE_NAME = "fileName";
	/**
	 * 
	 */
	private static final Log log = LogFactory.getLog(FileUploadController.class);
	private static final long serialVersionUID = 1L;
	private static final String PARAMETER_ACTION = "action";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_UPLOAD = "upload";
	private static final String PARAMETER_FILE_URI = "fileUri";
	private static final String PARAMETER_SUBJECT_URI = "subjectUri";
	private static final String CONFIG_MAX_FILE_SIZE = "fileUpload.maxFileSize";
	private static final String ALLOWED_MEDIA_TYPES = "fileUpload.allowedMIMETypes";
	private static final Long DEFAULT_FILE_SIZE = (long) (10 * 1024 * 1024);
	private static final String PARAMETER_PREDICATE_URI = "predicateUri";
	private static final String DEFAULT_TEMPLATE = "fileUpload-default.ftl";
	private static Long maxFileSize = DEFAULT_FILE_SIZE;
	private FileStorage fileStorage;
	private Set<String> allowedMediaTypes;

	@Override
	public void init() throws ServletException {
		super.init();
		fileStorage = ApplicationUtils.instance().getFileStorage();
		setMaxFileSize();
		setAllowedMediaTypes();
	}

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        AccessObject accessObject;
        AccessOperation accessOperation;
        try {
            Property predicate = new Property(getPredicateUri(vreq));
            final OntModel jenaOntModel = vreq.getJenaOntModel();
            final String subject = getSubjectUri(vreq);
            if (isUpload(vreq)) {
                accessObject = new ObjectPropertyStatementAccessObject(jenaOntModel, subject, predicate,
                        AccessObject.SOME_URI);
                accessOperation = AccessOperation.ADD;
            } else { // delete
                accessObject =
                        new ObjectPropertyStatementAccessObject(jenaOntModel, subject, predicate, getFileUri(vreq));
                accessOperation = AccessOperation.DROP;
            }
            return new SimpleAuthorizationRequest(accessObject, accessOperation);
        } catch (Exception e) {
            return AuthorizationRequest.UNAUTHORIZED;
        }
    }

	private String getFileUri(VitroRequest vreq) {
		return vreq.getParameter(PARAMETER_FILE_URI);
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		
		try {
			getReferrer(vreq);
			validateRequest(vreq);
			if (isUpload(vreq)) {
				uploadFile(vreq);
			} else if (isDelete(vreq)) {
				deleteFile(vreq);
			}
		} catch (Exception e) {
			return pageResponse(vreq, e.getMessage());
		}
		return new RedirectResponseValues(getNextPageUrl(vreq), HttpServletResponse.SC_SEE_OTHER);
	}
	private String getNextPageUrl(VitroRequest vreq) {
		return getReferrer(vreq);
	}
	
	private TemplateResponseValues pageResponse(VitroRequest vreq, String error) {
		TemplateResponseValues rv = new TemplateResponseValues(DEFAULT_TEMPLATE);
		rv.put(PARAMETER_ACTION, vreq.getParameter(PARAMETER_ACTION));
		rv.put(TEMPLATE_VAR_FORM_ACTION, formatRequestUrl(vreq));
		if (!error.isEmpty()) {
			rv.put(TEMPLATE_VAR_ERROR_MESSAGE, error);
		}
		rv.put(PARAM_REFERRER,getReferrer(vreq));
		rv.put(TEMPLATE_VAR_SUPPORTED_MEDIA_TYPES, printAllowedMediaTypes());
		rv.put(TEMPLATE_VAR_MAX_FILE_SIZE, maxFileSizeInMegabytes());
		return rv;
	}

	private String formatRequestUrl(VitroRequest vreq) {
		String result = vreq.getRequestURL().toString() + "?";
		String query =  vreq.getQueryString();
		if (query != null) {
			result += query;
		}
		String referrer = vreq.getParameter(PARAM_REFERRER);
		if (referrer == null || referrer.isEmpty()) {
			result += "&" + PARAM_REFERRER + "=" + getReferrer(vreq);
		}
		return result;
	}

	private Double maxFileSizeInMegabytes() {
		if (maxFileSize > 0) {
			return ((double) maxFileSize/1048576);	
		} else {
			return 0.0;
		}
	}

	private void validateRequest(VitroRequest vreq) throws FileUploadException {
		I18nBundle i18nBundle = I18n.bundle(vreq);
		if (isUpload(vreq)) {
			validateUploadRequest(vreq,i18nBundle);
		} else if (isDelete(vreq)) {
			validateDeleteRequest(vreq,i18nBundle);
		} else if (hasAction(vreq)) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_supported_actions"));
		} else {
			throw new FileUploadException(i18nBundle.text("file_upload_error_no_action"));
		}
	}

	private void validateDeleteRequest(VitroRequest vreq, I18nBundle i18nBundle) throws FileUploadException {
		validateSubjectUri(vreq, i18nBundle);
		validatePredicateUri(vreq, i18nBundle);
		validateFileUri(vreq,i18nBundle);
	}

	private void validateUploadRequest(VitroRequest vreq, I18nBundle i18nBundle) throws FileUploadException {
		validateSubjectUri(vreq ,i18nBundle);
		validatePredicateUri(vreq , i18nBundle);
		validateFile(vreq, i18nBundle);
	}

	private void validateFile(VitroRequest vreq, I18nBundle i18nBundle) throws FileUploadException {
		Map<String, List<FileItem>> map = vreq.getFiles();
		if (map == null) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_file_not_found"));
		}
		List<FileItem> list = map.get(PARAMETER_UPLOADED_FILE);
		if ((list == null) || list.isEmpty()) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_file_not_found"));
		}
		FileItem file = list.get(0);
		if (file.getSize() == 0) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_file_size_is_zero"));
		}
		if (file.getSize() > maxFileSize) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_file_is_too_big", maxFileSize, file.getSize()));
		}
		validateMediaType(file, i18nBundle);
	}

	private void validateMediaType(FileItem file, I18nBundle i18nBundle) throws FileUploadException {
		String mediaType = getMediaType(file);
		if (mediaType.isEmpty()) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_file_type_not_recognized"));
		}
		
		if (!allowedMediaTypes.contains(mediaType.toLowerCase())) {
			String errorMessage = i18nBundle.text("file_upload_error_media_type_not_allowed", mediaType);
			log.error(errorMessage);
			throw new FileUploadException(errorMessage);
		}
	}

	private String printAllowedMediaTypes() {
		StringBuilder sb = new StringBuilder();
		if (allowedMediaTypes.isEmpty()) {
			return sb.toString();
		}
		for (Iterator<String> it = allowedMediaTypes.iterator(); it.hasNext();) {
			String mediaType = (String) it.next();
			sb.append(mediaType);
			if (it.hasNext()) {
				sb.append(", ");
			} else {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	private boolean hasAction(VitroRequest vreq) {
		return vreq.getParameter(PARAMETER_ACTION) != null;
	}

	private void deleteFile(VitroRequest vreq) {
		String subjectUri = getSubjectUri(vreq);
		String predicateUri = getPredicateUri(vreq);
		String fileUri = getFileUri(vreq);
		WebappDaoFactory webAppDaoFactory = vreq.getUnfilteredWebappDaoFactory();
		UploadedFileHelper fileHelper = new UploadedFileHelper(fileStorage, webAppDaoFactory, getServletContext());
		fileHelper.removeUploadedFile(subjectUri, predicateUri, fileUri);
	}

	private void uploadFile(VitroRequest vreq) throws FileUploadException {
		String subjectUri = getSubjectUri(vreq);
		String predicateUri = getPredicateUri(vreq);
		FileItem file = getUploadedFile(vreq);
		String uploadedFileName = getUploadedFileName(file);
		String storedFileName = createStoredFileName(file);
		WebappDaoFactory webAppDaoFactory = vreq.getUnfilteredWebappDaoFactory();
		UploadedFileHelper fileHelper = new UploadedFileHelper(fileStorage, webAppDaoFactory, getServletContext());
		FileInfo fileInfo = createFile(file, storedFileName, fileHelper);
		fileHelper.attachFileToSubject(fileInfo, subjectUri, predicateUri);
		fileHelper.setPublicFileName(fileInfo, uploadedFileName);
	}

	private FileInfo createFile(FileItem file, String storedFileName, UploadedFileHelper fileHelper)
			throws FileUploadException {
		FileInfo fileInfo = null;
		try {
			fileInfo = fileHelper.createFile(storedFileName, getMediaType(file), file.getInputStream());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new FileUploadException(e.getLocalizedMessage());
		}
		return fileInfo;
	}

	private String createStoredFileName(FileItem file) {
		String mediaType = getMediaType(file);
		int length = 64;
		boolean useLetters = true;
		boolean useNumbers = true;
		String storedFileName = RandomStringUtils.random(length, useLetters, useNumbers) + getExtension(mediaType);
		return storedFileName;
	}

	private String getExtension(String mediaType) {
		String extension = "";
		MimeTypes types = MimeTypes.getDefaultMimeTypes();
		try {
			MimeType mimeType = types.forName(mediaType);
			extension = mimeType.getExtension();
		} catch (MimeTypeException e) {
			log.error(e.getLocalizedMessage());
		}
		return extension;
	}

	private String getUploadedFileName(FileItem file) {
		String fileName = file.getName();
		if (fileName == null) {
			return DEFAULT_FILE_NAME;
		} else {
			return FilenameUtils.getName(fileName);
		}
	}

	private String getMediaType(FileItem file) {
		Tika tika = new Tika();
		InputStream is;
		String mediaType = "";
		try {
			is = file.getInputStream();
			mediaType = tika.detect(is);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
		return mediaType;
	}

	private void validateFileUri(VitroRequest vreq, I18nBundle i18nBundle) throws FileUploadException {
		String fileUri = getFileUri(vreq);
		validateUriNotEmpty(fileUri, i18nBundle.text("file_upload_file"), i18nBundle);
		validateIndividual(vreq, fileUri, i18nBundle);
	}

	private void validateSubjectUri(VitroRequest vreq, I18nBundle i18nBundle) throws FileUploadException {
		String subjectUri = getSubjectUri(vreq);
		validateUriNotEmpty(subjectUri, i18nBundle.text("file_upload_subject"), i18nBundle);
		validateIndividual(vreq, subjectUri, i18nBundle);
	}
	
	private void validatePredicateUri(VitroRequest vreq, I18nBundle i18nBundle) throws FileUploadException {
		String predicateUri = getPredicateUri(vreq);
		validateUriNotEmpty(predicateUri, i18nBundle.text("file_upload_predicate"), i18nBundle);
		validateIndividual(vreq, predicateUri, i18nBundle);
	}

	private void validateIndividual(VitroRequest vreq, String name, I18nBundle i18nBundle) throws FileUploadException {
		Individual subject = vreq.getUnfilteredWebappDaoFactory().getIndividualDao()
				.getIndividualByURI(name);
		if (subject == null) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_uri_not_exists", name));
		}
	}

	private void validateUriNotEmpty(String predicateUri, String name, I18nBundle i18nBundle) throws FileUploadException {
		if (predicateUri == null || predicateUri.trim().isEmpty()) {
			throw new FileUploadException(i18nBundle.text("file_upload_error_uri_not_given", name));
		}
	}

	private String getPredicateUri(VitroRequest vreq) {
		return vreq.getParameter(PARAMETER_PREDICATE_URI);
	}

	private String getSubjectUri(VitroRequest vreq) {
		return vreq.getParameter(PARAMETER_SUBJECT_URI);
	}

	private FileItem getUploadedFile(VitroRequest vreq) {
		return vreq.getFiles().get(PARAMETER_UPLOADED_FILE).get(0);
	}

	private boolean isUpload(VitroRequest vreq) {
		String action = vreq.getParameter(PARAMETER_ACTION);
		return ACTION_UPLOAD.equals(action);
	}

	private boolean isDelete(VitroRequest vreq) {
		String action = vreq.getParameter(PARAMETER_ACTION);
		return ACTION_DELETE.equals(action);
	}

	private void setAllowedMediaTypes() {
		ConfigurationProperties config = ConfigurationProperties.getBean(getServletContext());
		String allowedTypes = config.getProperty(ALLOWED_MEDIA_TYPES, "");
		if (allowedTypes.isEmpty()) {
			allowedMediaTypes = new HashSet<String>();
		} else {
			allowedMediaTypes = new HashSet<String>(Arrays.asList(allowedTypes.toLowerCase().trim().split("\\s*,\\s*")));	
		}
	}

	private String getReferrer(VitroRequest vreq) {
		String referrer = vreq.getParameter(PARAM_REFERRER);
		if (referrer == null) {
			referrer = vreq.getHeader(REFERER_HEADER);
		}
		if (referrer == null) {
			referrer =  "/";
		}
		return referrer;
	}
	
	private void setMaxFileSize() {
		ConfigurationProperties config = ConfigurationProperties.getBean(getServletContext());
		String configFileSize = config.getProperty(CONFIG_MAX_FILE_SIZE, DEFAULT_FILE_SIZE.toString());
		try {
			maxFileSize = Long.parseLong(configFileSize);
		} catch (NumberFormatException e) {
			log.error("maxFileSize parsing failed");
			log.error(e);
		}
	}

	public static class FileUploadException extends Exception {
		private static final long serialVersionUID = 1L;
		private final Object[] parameters;

		FileUploadException(String message, Object... parameters) {
			super(message);
			this.parameters = parameters;
		}

		public String formatMessage(HttpServletRequest req) {
			return I18n.text(req, getMessage(), parameters);
		}
	}
}
