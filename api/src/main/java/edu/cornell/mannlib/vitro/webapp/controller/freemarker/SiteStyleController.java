/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import org.apache.tika.Tika;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FileUploadController.FileUploadException;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;


import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Handle adding, replacing or deleting the custom css file.
 */
@WebServlet(name = "SiteStyleController", urlPatterns = { "/siteStyle" })
public class SiteStyleController extends FreemarkerHttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory
			.getLog(SiteStyleController.class);

	/** Limit file size to 6 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 6 * 1024 * 1024;

	/** The form field of the uploaded file; use as a key to the FileItem map. */
	public static final String PARAMETER_UPLOADED_FILE = "fileUpload";

	public static final String TEMPLATE = "siteAdmin/siteAdmin-siteStyle.ftl";

	public static final String URL_HERE = UrlBuilder.getUrl("/siteStyle");
	private static final String PARAMETER_ACTION = "action";

	public static final String BODY_BACK_LOCATION = "backLocation";
	public static final String BODY_FORM_ACTION_UPLOAD = "actionUpload";
	public static final String BODY_FORM_ACTION_REMOVE = "actionRemove";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_REMOVE = "remove";


	private FileStorage fileStorage;
	private ReferrerHelper referrerHelper;
	/**
	 * When initialized, get a reference to the File Storage system. Without
	 * that, we can do nothing.
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		fileStorage = ApplicationUtils.instance().getFileStorage();
		referrerHelper = new ReferrerHelper("siteStyle", "editForm?controller=ApplicationBean");
	}

	/**
	 * How large an css file will we accept?
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
		return SimplePermission.EDIT_SITE_INFORMATION.ACTION;
	}

	/**
	 * Handle the different actions. If not specified, the default action is to
	 * show the intro screen.
	 * @throws FileUploadException 
	 */
	@Override
	protected ResponseValues processRequest(VitroRequest vreq) throws FileUploadException {
		String action = vreq.getParameter(PARAMETER_ACTION);

		if (Objects.equals(vreq.getMethod(), "POST")) {

			if (action.equals("upload")) {
				return uploadCssFile(vreq);
			} else if (action.equals("remove")) {
				return removeCssFile(vreq);
			} else {
				this.referrerHelper.captureReferringUrl(vreq);
				return showMainStyleEditPage(vreq);
			}
		}

		this.referrerHelper.captureReferringUrl(vreq);
		return showMainStyleEditPage(vreq);
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

	private FileItem getUploadedFile(VitroRequest vreq) throws FileUploadException {
		if (vreq.getFiles().isEmpty() || vreq.getFiles().get(PARAMETER_UPLOADED_FILE) == null) {
			throw new FileUploadController.FileUploadException("Wrong file type uploaded or file is too big.");
		}
		return vreq.getFiles().get(PARAMETER_UPLOADED_FILE).get(0);
	}

	private FileInfo createFile(FileItem file, String storedFileName, UploadedFileHelper fileHelper)
			throws FileUploadController.FileUploadException {
		FileInfo fileInfo = null;
		try {
			fileInfo = fileHelper.createFile(storedFileName, getMediaType(file), file.getInputStream());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new FileUploadController.FileUploadException(e.getLocalizedMessage());
		}
		return fileInfo;
	}

	private ResponseValues uploadCssFile(VitroRequest vreq) throws FileUploadController.FileUploadException {
		FileItem file = getUploadedFile(vreq);

		String mediaType = getMediaType(file);
		if (!"text/css".equals(mediaType) && !"text/plain".equals(mediaType)) {
			throw new FileUploadController.FileUploadException("Uploaded file is not a CSS or plain text file.");
		}

		WebappDaoFactory webAppDaoFactory = vreq.getUnfilteredWebappDaoFactory();
		UploadedFileHelper fileHelper = new UploadedFileHelper(fileStorage, webAppDaoFactory, getServletContext());
		FileInfo fileInfo = createFile(file, "custom-style.css", fileHelper);

		updateCssFileDispalyModel(UrlBuilder.getUrl(fileInfo.getBytestreamAliasUrl()));

		return showMainStyleEditPage(vreq);
	}

	private TemplateResponseValues removeCssFile(VitroRequest vreq) {
		removeCssFileDisplayModel();
		return showMainStyleEditPage(vreq);
	}

	private TemplateResponseValues showMainStyleEditPage(VitroRequest vreq) {
		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE);

		rv.put(BODY_BACK_LOCATION, referrerHelper.getExitUrl(vreq));
		rv.put(BODY_FORM_ACTION_UPLOAD, UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_UPLOAD)));
		rv.put(BODY_FORM_ACTION_REMOVE, UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_REMOVE)));

		return rv;
	}


	private void updateCssFileDispalyModel(String cssFilePath) {
		ContextModelAccess cma = ModelAccess.getInstance();
		OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

		Resource portalResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);

		String propertyUri = VitroVocabulary.PORTAL_CUSTOMCSSPATH;
		String value = cssFilePath;
		Property property = ResourceFactory.createProperty(propertyUri);

		displayModel.removeAll(portalResource, property, null);

		if ( !value.isEmpty() && !value.equals("null")) {
			Statement statement = new StatementImpl(portalResource, property, ResourceFactory.createTypedLiteral(value));
			displayModel.add(statement);
		}
	}


	private void removeCssFileDisplayModel() {
		ContextModelAccess cma = ModelAccess.getInstance();
		OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

		Resource themeResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);

		Property property = ResourceFactory.createProperty(VitroVocabulary.PORTAL_CUSTOMCSSPATH);
		displayModel.removeAll(themeResource, property, null);
	}

}
