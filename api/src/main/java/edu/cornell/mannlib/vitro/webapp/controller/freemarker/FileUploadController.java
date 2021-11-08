package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest.UNAUTHORIZED;
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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropertyStatement;
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
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;

@WebServlet(name = "FileUploadController", urlPatterns = { "/uploadFile" })
public class FileUploadController extends FreemarkerHttpServlet {
	private static final String TEMPLATE_VAR_FORM_ACTION = "formAction";
	private static final String TEMPLATE_VAR_MAX_FILE_SIZE = "maxFileSize";
	private static final String PARAM_REFERRER = "referrer";
	private static final String TEMPLATE_VAR_SUPPORTED_TYPES = "supportedTypes";
	private static final String TEMPLATE_VAR_SUPPORTED_MIME_TYPES = "supportedMIMETypes";

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
	private static final String ALLOWED_MIME_TYPES = "fileUpload.allowedMIMETypes";
	private static final Long DEFAULT_FILE_SIZE = (long) (10 * 1024 * 1024);
	private static final String PARAMETER_PREDICATE_URI = "predicateUri";
	private static final String DEFAULT_TEMPLATE = "fileUpload-default.ftl";
	private static Long maxFileSize = DEFAULT_FILE_SIZE;
	private FileStorage fileStorage;
	private Set<String> allowedMimeTypes;

	@Override
	public void init() throws ServletException {
		super.init();
		fileStorage = ApplicationUtils.instance().getFileStorage();
		setMaxFileSize();
		setAllowedMimeTypes();
	}

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		RequestedAction ra;
		try {
			Property predicate = new Property(getPredicateUri(vreq));
			final OntModel jenaOntModel = vreq.getJenaOntModel();
			final String subject = getSubjectUri(vreq);
			if (isUpload(vreq)) {
				ra = new AddObjectPropertyStatement(jenaOntModel, subject, predicate,RequestedAction.SOME_URI);
			} else { // delete
				ra = new DropObjectPropertyStatement(jenaOntModel, subject, predicate, getFileUri(vreq));
			}
			return ra;
		} catch (Exception e) {
			return UNAUTHORIZED;
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
		rv.put(TEMPLATE_VAR_SUPPORTED_TYPES, printAllowedFileExtensions());
		rv.put(TEMPLATE_VAR_SUPPORTED_MIME_TYPES, printAllowedMimeTypes());
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
		if (isUpload(vreq)) {
			validateUploadRequest(vreq);
		} else if (isDelete(vreq)) {
			validateDeleteRequest(vreq);
		} else if (hasAction(vreq)) {
			throw new FileUploadException("Only delete and upload actions supported");
		} else {
			throw new FileUploadException("No action specified");
		}
	}

	private void validateDeleteRequest(VitroRequest vreq) throws FileUploadException {
		validateSubjectUri(vreq);
		validatePredicateUri(vreq);
		validateFileUri(vreq);
	}

	private void validateUploadRequest(VitroRequest vreq) throws FileUploadException {
		validateSubjectUri(vreq);
		validatePredicateUri(vreq);
		validateFile(vreq);
	}

	private void validateFile(VitroRequest vreq) throws FileUploadException {
		Map<String, List<FileItem>> map = vreq.getFiles();
		if (map == null) {
			throw new FileUploadException("File to upload not found");
		}
		List<FileItem> list = map.get(PARAMETER_UPLOADED_FILE);
		if ((list == null) || list.isEmpty()) {
			throw new FileUploadException("No file uploaded");
		}
		FileItem file = list.get(0);
		if (file.getSize() == 0) {
			throw new FileUploadException("Uploaded file size is 0");
		}
		if (file.getSize() > maxFileSize) {
			throw new FileUploadException("Uploaded file is too big. Maximum file size is " + maxFileSize
					+ " bytes. Uploaded file is " + file.getSize() + " bytes.");
		}
		validateMimeType(file);
	}

	private void validateMimeType(FileItem file) throws FileUploadException {
		String mime = getMimeType(file);
		if (mime.isEmpty()) {
			throw new FileUploadException("File type is unrecognized");
		}
		String extension = null;
		MimeTypes types = MimeTypes.getDefaultMimeTypes();
		try {
			MimeType mimeType = types.forName(mime);
			extension = mimeType.getExtension();
		} catch (MimeTypeException e) {
			log.error(e.getLocalizedMessage());
		}
		if (extension == null || extension.isEmpty()) {
			throw new FileUploadException("Extension for mime type " + mime + " not found");
		}
		if (!allowedMimeTypes.contains(mime.toLowerCase())) {
			log.error("File mime type is not allowed. " + printAllowedMimeTypes() + " Current mime type is " + mime);
			throw new FileUploadException(
					"File type is not allowed. Allowed file types: " + printAllowedFileExtensions() + 
					" Current file type is " + getExtension(mime).replaceAll("\\.", ""));
		}
	}

	private String printAllowedFileExtensions() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = allowedMimeTypes.iterator(); it.hasNext();) {
			String mimeType = (String) it.next();
			String extension = getExtension(mimeType);
			sb.append(extension.replaceAll("\\.", ""));
			if (it.hasNext()) {
				sb.append(", ");
			} else {
				sb.append(".");
			}
		}
		return sb.toString();
	}
	
	private String printAllowedMimeTypes() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = allowedMimeTypes.iterator(); it.hasNext();) {
			String mimeType = (String) it.next();
			sb.append(mimeType);
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
			fileInfo = fileHelper.createFile(storedFileName, getMimeType(file), file.getInputStream());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new FileUploadException(e.getLocalizedMessage());
		}
		return fileInfo;
	}

	private String createStoredFileName(FileItem file) {
		String mimeString = getMimeType(file);
		int length = 64;
		boolean useLetters = true;
		boolean useNumbers = true;
		String storedFileName = RandomStringUtils.random(length, useLetters, useNumbers) + getExtension(mimeString);
		return storedFileName;
	}

	private String getExtension(String mimeString) {
		String extension = "";
		MimeTypes types = MimeTypes.getDefaultMimeTypes();
		try {
			MimeType mime = types.forName(mimeString);
			extension = mime.getExtension();
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

	private String getMimeType(FileItem file) {
		Tika tika = new Tika();
		InputStream is;
		String mime = "";
		try {
			is = file.getInputStream();
			mime = tika.detect(is);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
		return mime;
	}

	private void validateFileUri(VitroRequest vreq) throws FileUploadException {
		String fileUri = getFileUri(vreq);
		validateUriNotEmpty(fileUri,"file");
		validateIndividual(vreq, fileUri);
	}

	private void validateSubjectUri(VitroRequest vreq) throws FileUploadException {
		String subjectUri = getSubjectUri(vreq);
		validateUriNotEmpty(subjectUri,"subject");
		validateIndividual(vreq, subjectUri);
	}
	
	private void validatePredicateUri(VitroRequest vreq) throws FileUploadException {
		String predicateUri = getPredicateUri(vreq);
		validateUriNotEmpty(predicateUri,"predicate");
		validateIndividual(vreq, predicateUri);
	}

	private void validateIndividual(VitroRequest vreq, String subjectUri) throws FileUploadException {
		Individual subject = vreq.getUnfilteredWebappDaoFactory().getIndividualDao()
				.getIndividualByURI(subjectUri);
		if (subject == null) {
			throw new FileUploadException("Uri " + subjectUri + "doesn't exist");
		}
	}

	private void validateUriNotEmpty(String predicateUri,String name) throws FileUploadException {
		if (predicateUri == null || predicateUri.trim().isEmpty()) {
			throw new FileUploadException("No " + name + " uri was given");
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

	private void setAllowedMimeTypes() {
		ConfigurationProperties config = ConfigurationProperties.getBean(getServletContext());
		String allowedTypes = config.getProperty(ALLOWED_MIME_TYPES, "");
		allowedMimeTypes = new HashSet<String>(Arrays.asList(allowedTypes.toLowerCase().trim().split("\\s*,\\s*")));
	}

	private String getReferrer(VitroRequest vreq) {
		String referrer = vreq.getParameter(PARAM_REFERRER);
		if (referrer == null) {
			referrer = vreq.getHeader("Referer");
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

	static class FileUploadException extends Exception {
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
