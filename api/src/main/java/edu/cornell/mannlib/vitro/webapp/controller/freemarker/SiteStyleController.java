/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FileUploadController.FileUploadException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
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
import org.apache.tika.Tika;

/**
 * Handle adding, replacing or deleting the custom css file.
 */
@WebServlet(name = "SiteStyleController", urlPatterns = {"/siteStyle"})
public class SiteStyleController extends FreemarkerHttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory
        .getLog(SiteStyleController.class);

    /**
     * Limit file size to 6 megabytes.
     */
    public static final int MAXIMUM_FILE_SIZE = 6 * 1024 * 1024;

    /**
     * The form field of the uploaded file; use as a key to the FileItem map.
     */
    public static final String PARAMETER_UPLOADED_FILE = "fileUpload";

    public static final String URL_HERE = UrlBuilder.getUrl("/siteStyle");
    private static final String PARAMETER_ACTION = "action";

    public static final String ACTION_UPLOAD = "upload";
    public static final String ACTION_REMOVE = "remove";


    public static boolean customCssUrlLoaded = false;
    public static String customCssUrl = null;
    public static String customCssFileUri = null;
    public static String customCssVersion = null;


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
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        VitroRequest vreq = new VitroRequest(request);

        if (!isAuthorizedToDisplayPage(request, response,
            requiredActions(vreq))) {
            return;
        }

        String action = vreq.getParameter(PARAMETER_ACTION);

        if ("remove".equals(action)) {
            removeCssFile(vreq);
            showMainStyleEditPage(request, response, "CSS file removed.");
            return;
        }

        showMainStyleEditPage(request, response, "");

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        VitroRequest vreq = new VitroRequest(request);

        if (!isAuthorizedToDisplayPage(request, response,
            requiredActions(vreq))) {
            return;
        }

        String action = vreq.getParameter(PARAMETER_ACTION);

        if ("remove".equals(action)) {
            removeCssFile(vreq);
            showMainStyleEditPage(request, response, "CSS file removed.");
        } else if ("upload".equals(action)) {
            try {
                uploadCssFile(vreq);
            } catch (FileUploadException e) {
                throw new ServletException(e);
            }
            showMainStyleEditPage(request, response, "Uploaded CSS file successfully.");
        } else {
            showMainStyleEditPage(request, response, "No action specified.");
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
            log.error(e, e);
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
        FileInfo fileInfo;
        try {
            fileInfo = fileHelper.createFile(storedFileName, getMediaType(file), file.getInputStream());
        } catch (Exception e) {
            log.error(e, e);
            throw new FileUploadController.FileUploadException(e.getLocalizedMessage());
        }
        return fileInfo;
    }

    private void uploadCssFile(VitroRequest vreq) throws FileUploadController.FileUploadException {
        FileItem file = getUploadedFile(vreq);

        String mediaType = getMediaType(file);
        if (!"text/css".equals(mediaType) && !"text/plain".equals(mediaType)) {
            throw new FileUploadController.FileUploadException("Uploaded file is not a CSS or plain text file.");
        }

        WebappDaoFactory webAppDaoFactory = vreq.getUnfilteredWebappDaoFactory();
        UploadedFileHelper fileHelper = new UploadedFileHelper(fileStorage, webAppDaoFactory, getServletContext());
        removeCssFileDisplayModel(fileHelper);
        FileInfo fileInfo = createFile(file, "custom-style.css", fileHelper);

        updateCssFileDisplayModel(fileInfo);
        fileHelper.attachFileToSubject(fileInfo, VitroVocabulary.PROPERTY_CUSTOMSTYLE,
            VitroVocabulary.PORTAL_CUSTOMCSSFILEURI);
    }

    private void removeCssFile(VitroRequest vreq) {
        WebappDaoFactory webAppDaoFactory = vreq.getUnfilteredWebappDaoFactory();
        UploadedFileHelper fileHelper = new UploadedFileHelper(fileStorage, webAppDaoFactory, getServletContext());

        removeCssFileDisplayModel(fileHelper);
        resetCustomCssCache();
    }

    private void showMainStyleEditPage(HttpServletRequest request, HttpServletResponse response, String info) {
        try {
            response.setContentType("text/html");
            response.getWriter().println("<html><body><h2>Site Style</h2><pre>customCssUrl: "
                + (customCssUrl != null ? customCssUrl : "none") + "</pre><pre>Info: "
                + (info != null ? info : "") + "</pre></body></html>");
        } catch (IOException e) {
            log.error("Error writing sample data to response", e);
        }
    }

    public static String getRemovePathString() {
        return UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_REMOVE));
    }

    public static String getUploadPathString() {
        return UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_UPLOAD));
    }


    private void updateCssFileDisplayModel(FileInfo fileInfo) {
        String cssFilePath = UrlBuilder.getUrl(fileInfo.getBytestreamAliasUrl());


        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource portalResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);

        String propertyUri = VitroVocabulary.PORTAL_CUSTOMCSSPATH;
        Property property = ResourceFactory.createProperty(propertyUri);

        String propertyUriFileUri = VitroVocabulary.PORTAL_CUSTOMCSSFILEURI;
        Property propertyFileUri = ResourceFactory.createProperty(propertyUriFileUri);
        String propertyUriVersion = VitroVocabulary.PORTAL_CUSTOMCSSVERSION;
        Property propertyVersion = ResourceFactory.createProperty(propertyUriVersion);

        if (!cssFilePath.isEmpty()) {
            Statement statementImageUrl =
                new StatementImpl(portalResource, property, ResourceFactory.createTypedLiteral(cssFilePath));
            displayModel.add(statementImageUrl);
            Statement statementUri = new StatementImpl(portalResource, propertyFileUri,
                ResourceFactory.createTypedLiteral(fileInfo.getUri()));
            displayModel.add(statementUri);

            String versionValue =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(ZonedDateTime.now(ZoneOffset.UTC));
            Statement statementVersion = new StatementImpl(portalResource, propertyVersion,
                ResourceFactory.createTypedLiteral(versionValue));
            displayModel.add(statementVersion);

            SiteStyleController.customCssUrlLoaded = true;
            SiteStyleController.customCssVersion = versionValue;
            SiteStyleController.customCssUrl = appendVersion(cssFilePath, versionValue);
            SiteStyleController.customCssFileUri = fileInfo.getUri();

        }
    }


    private void removeCssFileDisplayModel(UploadedFileHelper fileHelper) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource themeResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);

        Property property = ResourceFactory.createProperty(VitroVocabulary.PORTAL_CUSTOMCSSPATH);
        displayModel.removeAll(themeResource, property, null);
        Property propertyFileUri = ResourceFactory.createProperty(VitroVocabulary.PORTAL_CUSTOMCSSFILEURI);
        displayModel.removeAll(themeResource, propertyFileUri, null);
        Property propertyVersion = ResourceFactory.createProperty(VitroVocabulary.PORTAL_CUSTOMCSSVERSION);
        displayModel.removeAll(themeResource, propertyVersion, null);

        String url = SiteStyleController.getCustomCssUrlCache();
        String fileUri = SiteStyleController.customCssFileUri;
        if (url != null) {

            fileHelper.removeUploadedFile(VitroVocabulary.PROPERTY_CUSTOMSTYLE, VitroVocabulary.PORTAL_CUSTOMCSSPATH,
                fileUri);
        }
    }


    private static void updateCustomCssUrl() {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource styleResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);
        Property customCssPathProperty = ResourceFactory.createProperty(VitroVocabulary.PORTAL_CUSTOMCSSPATH);
        Property customCssFileUriProperty = ResourceFactory.createProperty(
            VitroVocabulary.PORTAL_CUSTOMCSSFILEURI);
        Property customCssVersionProperty = ResourceFactory.createProperty(
            VitroVocabulary.PORTAL_CUSTOMCSSVERSION);

        String basePath = getLiteralProperty(displayModel, styleResource, customCssPathProperty);
        customCssFileUri = getLiteralProperty(displayModel, styleResource, customCssFileUriProperty);
        customCssVersion = getLiteralProperty(displayModel, styleResource, customCssVersionProperty);
        customCssUrl = appendVersion(basePath, customCssVersion);

        customCssUrlLoaded = true;
    }

    private static String appendVersion(String basePath, String version) {
        if (basePath == null || basePath.isEmpty()) {
            return basePath;
        }
        if (version == null || version.isEmpty()) {
            return basePath;
        }
        String separator = basePath.contains("?") ? "&" : "?";
        return basePath + separator + "version=" + version;
    }

    private static String getLiteralProperty(OntModel model, Resource subject, Property property) {
        StmtIterator stmtIterator = model.listStatements(subject, property, (RDFNode) null);
        try {
            if (stmtIterator.hasNext()) {
                RDFNode object = stmtIterator.nextStatement().getObject();
                return object.isLiteral() ? object.asLiteral().getString() : null;
            }
        } finally {
            stmtIterator.close();
        }
        return null;
    }


    public static void resetCustomCssCache() {
        customCssUrlLoaded = false;
        customCssUrl = null;
        customCssFileUri = null;
        customCssVersion = null;
    }

    public static String getCustomCssUrlCache() {
        if (!customCssUrlLoaded) {
            updateCustomCssUrl();
        }
        return customCssUrl;
    }
}
