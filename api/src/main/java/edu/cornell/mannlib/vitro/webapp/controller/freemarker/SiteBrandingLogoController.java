/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.UserMistakeException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;


/**
 * Handle adding, replacing or deleting the custom css file.
 */
@WebServlet(name = "SiteBrandingLogoController", urlPatterns = {"/site-branding-logo"})
public class SiteBrandingLogoController extends FreemarkerHttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory
        .getLog(SiteBrandingLogoController.class);

    /**
     * Limit file size to 6 megabytes.
     */
    public static final int MAXIMUM_FILE_SIZE = 6 * 1024 * 1024;

    /**
     * The form field of the uploaded file; use as a key to the FileItem map.
     */

    public static final String URL_HERE = UrlBuilder.getUrl("/site-branding-logo");
    private static final String PARAMETER_ACTION = "action";

    public static final String ACTION_UPLOAD = "upload";
    public static final String ACTION_REMOVE = "remove";

    public static final String LOGO_PARAMETER_ACTION = "portalLogoAction";
    public static final String MOBILE_LOGO_PARAMETER_ACTION = "mobilePortalLogoAction";

    public static boolean logoUrlLoaded = false;
    public static String logoUrl = null;
    public static String mobileLogoUrl = null;


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
     * How large css file will we accept?
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
     *
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        VitroRequest vreq = new VitroRequest(request);

        if (!isAuthorizedToDisplayPage(request, response,
            requiredActions(vreq))) {
            return;
        }

        String action = vreq.getParameter(PARAMETER_ACTION);

        if (Objects.equals(vreq.getMethod(), "POST")) {

            if (action.equals("upload")) {
                uploadLogoFiles(vreq);
            }
        }

        printDefaultPage(request, response, "");
    }

    private void uploadLogoFiles(VitroRequest vreq) {
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

    }

    private void printDefaultPage(HttpServletRequest request, HttpServletResponse response, String info) {
        try {
            response.setContentType("text/html");
            response.getWriter().println("<html><body><h2>Site Style</h2><pre>logoUrl: "
                + (logoUrl != null ? logoUrl : "none") + "</pre><pre>mobileLogoUrl: "
                + (mobileLogoUrl != null ? mobileLogoUrl : "none") + "</pre><pre>Info: "
                + (info != null ? info : "") + "</pre></body></html>");
        } catch (IOException e) {
            log.error("Error writing sample data to response", e);
        }
    }

    private void updateLogoPath(String propertyUri, String value) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource portalResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);
        Property property = ResourceFactory.createProperty(propertyUri);

        displayModel.removeAll(portalResource, property, null);
        if (!value.isEmpty() && !value.equals("null")) {
            Statement statement =
                new StatementImpl(portalResource, property, ResourceFactory.createTypedLiteral(value));
            displayModel.add(statement);
        }
    }

    private void updateDesktopLogo(String cssFilePath) {
        updateLogoPath(VitroVocabulary.PORTAL_LOGOURL, cssFilePath);
    }

    private void updateMobileLogo(String cssFilePath) {
        updateLogoPath(VitroVocabulary.PORTAL_LOGOSMALLURL, cssFilePath);
    }

    public static String getLogoUploadUrlString() {
        return UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_UPLOAD));
    }

    private static String getLogo(String propertyUrl) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource s = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);
        Property customCssPathProperty = ResourceFactory.createProperty(propertyUrl);
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

    public static String getLogoUrlCache() {
        if (!logoUrlLoaded) {
            updateLogoUrlCache();
        }
        return logoUrl;
    }

    public static String getMobileLogoUrlCache() {
        if (!logoUrlLoaded) {
            updateLogoUrlCache();
        }
        return mobileLogoUrl;
    }
}
