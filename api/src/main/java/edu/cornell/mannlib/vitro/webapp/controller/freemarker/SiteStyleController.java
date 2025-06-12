/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FileUploadController.FileUploadException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
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

    public static final String TEMPLATE = "siteAdmin/siteAdmin-siteStyle.ftl";

    public static final String URL_HERE = UrlBuilder.getUrl("/siteStyle");
    private static final String PARAMETER_ACTION = "action";

    public static final String BODY_BACK_LOCATION = "backLocation";
    public static final String BODY_FORM_ACTION_UPLOAD = "actionUpload";
    public static final String BODY_FORM_ACTION_REMOVE = "actionRemove";
    public static final String ACTION_UPLOAD = "upload";
    public static final String ACTION_REMOVE = "remove";


    public static boolean customCssUrlLoaded = false;
    public static String customCssUrl = null;


    private FileStorage fileStorage;
    private RefererHelper refererHelper;

    /**
     * When initialized, get a reference to the File Storage system. Without
     * that, we can do nothing.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        fileStorage = ApplicationUtils.instance().getFileStorage();
        refererHelper = new RefererHelper("siteStyle", "editForm?controller=ApplicationBean");
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
    protected ResponseValues processRequest(VitroRequest vreq) throws FileUploadException {
        String action = vreq.getParameter(PARAMETER_ACTION);

        if (Objects.equals(vreq.getMethod(), "POST")) {

            if (action.equals("upload")) {
                return uploadCssFile(vreq);
            } else if (action.equals("remove")) {
                return removeCssFile(vreq);
            } else {
                this.refererHelper.captureReferringUrl(vreq);
                return showMainStyleEditPage(vreq);
            }
        }

        this.refererHelper.captureReferringUrl(vreq);
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
        FileInfo fileInfo;
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

        String cssFilePath = UrlBuilder.getUrl(fileInfo.getBytestreamAliasUrl());
        updateCssFileDisplayModel(cssFilePath);

        SiteStyleController.customCssUrlLoaded = true;
        SiteStyleController.customCssUrl = cssFilePath;

        return showMainStyleEditPage(vreq);
    }

    private TemplateResponseValues removeCssFile(VitroRequest vreq) {
        removeCssFileDisplayModel();
        resetCustomCssCache();
        return showMainStyleEditPage(vreq);
    }

    private TemplateResponseValues showMainStyleEditPage(VitroRequest vreq) {
        TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE);

        rv.put(BODY_BACK_LOCATION, refererHelper.getExitUrl(vreq));
        rv.put(BODY_FORM_ACTION_UPLOAD, UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_UPLOAD)));
        rv.put(BODY_FORM_ACTION_REMOVE, UrlBuilder.getPath(URL_HERE, new ParamMap(PARAMETER_ACTION, ACTION_REMOVE)));

        return rv;
    }


    private void updateCssFileDisplayModel(String cssFilePath) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource portalResource = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);

        String propertyUri = VitroVocabulary.PORTAL_CUSTOMCSSPATH;
        Property property = ResourceFactory.createProperty(propertyUri);

        displayModel.removeAll(portalResource, property, null);

        if (!cssFilePath.isEmpty() && !cssFilePath.equals("null")) {
            Statement statement =
                new StatementImpl(portalResource, property, ResourceFactory.createTypedLiteral(cssFilePath));
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


    private static void updateCustomCssUrl() {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        Resource s = ResourceFactory.createResource(VitroVocabulary.PROPERTY_CUSTOMSTYLE);
        Property customCssPathProperty = ResourceFactory.createProperty(VitroVocabulary.PORTAL_CUSTOMCSSPATH);
        StmtIterator iter = displayModel.listStatements(s, customCssPathProperty, (RDFNode) null);

        if (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            RDFNode object = stmt.getObject();

            if (object.isLiteral()) {
                customCssUrl = object.asLiteral().getString();
            } else {
                customCssUrl = null;
            }
        } else {
            customCssUrl = null;
        }
        customCssUrlLoaded = true;
    }

    public static void resetCustomCssCache() {
        customCssUrlLoaded = false;
        customCssUrl = null;
    }

    public static String getCustomCssUrlCache() {
        if (!customCssUrlLoaded) {
            updateCustomCssUrl();
        }
        return customCssUrl;
    }
}
