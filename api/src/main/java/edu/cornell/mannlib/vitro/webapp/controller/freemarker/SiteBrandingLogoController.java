/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import org.apache.tika.Tika;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
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
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.UserMistakeException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;


import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;



/**
 * Handle adding, replacing or deleting the custom css file.
 */
@WebServlet(name = "SiteBrandingLogoController", urlPatterns = { "/site-branding-logo" })
public class SiteBrandingLogoController extends FreemarkerHttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory
			.getLog(SiteBrandingLogoController.class);

	/** Limit file size to 6 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 6 * 1024 * 1024;

	/** The form field of the uploaded file; use as a key to the FileItem map. */
	public static final String PARAMETER_UPLOADED_FILE = "fileUpload";

	public static final String TEMPLATE = "siteAdmin/siteAdmin-logoUpload.ftl";

	public static final String URL_HERE = UrlBuilder.getUrl("/site-branding-logo");
	private static final String PARAMETER_ACTION = "action";

	public static final String BODY_BACK_LOCATION = "backLocation";
	public static final String BODY_FORM_ACTION_UPLOAD = "actionUpload";
	public static final String BODY_FORM_ACTION_REMOVE = "actionRemove";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_REMOVE = "remove";

	public static final String LOGO_PARAMETER_ACTION = "portalLogoAction";
	public static final String MOBILE_LOGO_PARAMETER_ACTION = "mobilePortalLogoAction";

	public static boolean logoUrlLoaded = false;
	public static String logoUrl = null;
	public static String mobileLogoUrl = null;


    private ReferrerHelper referrerHelper;
	private FileStorage fileStorage;
	// private ReferrerHelper referrerHelper;
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
				return uploadLogoFiles(vreq);
			} else {
				this.referrerHelper.captureReferringUrl(vreq);
				return showMainStyleEditPage(vreq);
			}
		}

		// this.referrerHelper.captureReferringUrl(vreq);
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


	private ResponseValues uploadLogoFiles(VitroRequest vreq) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getUnfilteredWebappDaoFactory(), getServletContext());


		String desktopLogoAction = vreq.getParameter(LOGO_PARAMETER_ACTION);
		String mobileLogoAction = vreq.getParameter(MOBILE_LOGO_PARAMETER_ACTION);

        try {
            if (desktopLogoAction.equals("update")) {
                FileItem desktopLogoFileItem = helper.validateImageFromRequest(vreq, "portalLogo");
                FileInfo desktopLogoFileInfo = helper.storeNewImage(desktopLogoFileItem, vreq, false);
                String desktopLogoUrl = UrlBuilder.getUrl(desktopLogoFileInfo.getBytestreamAliasUrl());
                updateDesktopLogo(desktopLogoUrl);
            } else if (desktopLogoAction.equals("reset")) {
                updateDesktopLogo("");
            }

            if (mobileLogoAction.equals("update")) {
                FileItem mobileLogoFileItem = helper.validateImageFromRequest(vreq, "mobilePortalLogo");
                FileInfo mobileLogoFileInfo = helper.storeNewImage(mobileLogoFileItem, vreq, false);
                String mobileLogoUrl = UrlBuilder.getUrl(mobileLogoFileInfo.getBytestreamAliasUrl());
                updateMobileLogo(mobileLogoUrl);
            } else if (mobileLogoAction.equals("reset")) {
                updateMobileLogo("");
            }
            updateLogoUrlCache();

        } catch (UserMistakeException e) {
            log.error("Error validating image from request: " + e.getMessage());
            // Handle the exception appropriately, e.g., return an error response or rethrow it
        }

		return showMainStyleEditPage(vreq);
	}


	private TemplateResponseValues showMainStyleEditPage(VitroRequest vreq) {
		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE);

		rv.put(BODY_BACK_LOCATION, referrerHelper.getExitUrl(vreq));
		rv.put(BODY_FORM_ACTION_UPLOAD, UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_UPLOAD)));
		rv.put(BODY_FORM_ACTION_REMOVE, UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_REMOVE)));

		return rv;
	}

	private void updateLogoPath(String propertyUri, String value) {
		ContextModelAccess cma = ModelAccess.getInstance();
		OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

		Resource portalResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);
		Property property = ResourceFactory.createProperty(propertyUri);

		displayModel.removeAll(portalResource, property, null);
		if (!value.isEmpty() && !value.equals("null")) {
			Statement statement = new StatementImpl(portalResource, property, ResourceFactory.createTypedLiteral(value));
			displayModel.add(statement);
		}
	}

	private void updateDesktopLogo(String cssFilePath) {
		updateLogoPath(VitroVocabulary.PORTAL_LOGOURL, cssFilePath);
	}

	private void updateMobileLogo(String cssFilePath) {
		updateLogoPath(VitroVocabulary.PORTAL_LOGOSMALLURL, cssFilePath);
	}


	private static String getLogo(String properyUrl) {
		ContextModelAccess cma = ModelAccess.getInstance();
		OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

		Resource s = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);
		Property customCssPathProperty = ResourceFactory.createProperty(properyUrl);
		StmtIterator iter = displayModel.listStatements(s, customCssPathProperty, (RDFNode) null);

		if (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			RDFNode object = stmt.getObject();

			if (object.isLiteral()) {
				return object.asLiteral().getString();
			}
		}
		return null;	
	}

	private static void updateLogoUrlCache() {
		logoUrl = getLogo(VitroVocabulary.PORTAL_LOGOURL);
		mobileLogoUrl = getLogo(VitroVocabulary.PORTAL_LOGOSMALLURL);
		logoUrlLoaded = true;
	}

	public static void resetCustomCssCache() {
		logoUrlLoaded = false;
		logoUrl = null;
		mobileLogoUrl = null;
	}

	public static String getLogoUrlCache() {
		if (logoUrlLoaded == false) {
			updateLogoUrlCache();
		}
		return logoUrl;
	}

	public static String getMobileLogoUrlCache() {
		if (logoUrlLoaded == false) {
			updateLogoUrlCache();
		}
		return mobileLogoUrl;
	}
}
