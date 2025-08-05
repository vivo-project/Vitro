/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.controller.edit.ReorderController;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.atlas.json.JsonObject;
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
@WebServlet(name = "SiteStyleController", urlPatterns = {"/siteBranding"})
public class SiteBrandingController extends VitroAjaxController {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ReorderController.class);

    public static boolean themeBrandingLoaded = false;
    public static Map<String, String> themeBranding = null;

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return SimplePermission.EDIT_SITE_INFORMATION.ACTION;
    }

    @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response) throws IOException {
        String method = vreq.getMethod();

        switch (method.toUpperCase()) {
            case "GET":
                listBrandingColors(vreq, response);
                break;

            case "POST":
                handlePostRequest(vreq, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Only GET and POST methods are supported");
        }
    }

    private void handlePostRequest(VitroRequest vreq, HttpServletResponse response) throws IOException {
        String action = vreq.getParameter("action");

        switch (action != null ? action.toLowerCase() : "") {
            case "update":
                updateBrandingColor(vreq, response);
                break;

            case "remove-all":
                removeAllBrandingColors(vreq, response);
                response.setStatus(HttpServletResponse.SC_OK);
                break;

            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action parameter");
        }

        String currentTheme = vreq.getParameter("theme");
        if (currentTheme == null) {
            currentTheme = getCurrentTheme(vreq);
        }
        updateThemeBrandingCache(currentTheme);
    }

    private void listBrandingColors(VitroRequest vreq, HttpServletResponse response) {
        String currentTheme = vreq.getParameter("theme");
        if (currentTheme == null) {
            currentTheme = getCurrentTheme(vreq);
        }

        Map<String, String> brandingColors = getBrandingColors(currentTheme);

        JsonObject jsonResponse = new JsonObject();
        brandingColors.forEach(jsonResponse::put);

        response.setContentType("application/json");
        try {
            response.getWriter().write(jsonResponse.toString());
        } catch (IOException e) {
            log.error("Error writing response", e);
        }
    }


    private static Map<String, String> getBrandingColors(String theme) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);


        Resource s = ResourceFactory.createResource(VitroVocabulary.vitroURI + theme);
        StmtIterator iter = displayModel.listStatements(s, null, (RDFNode) null);

        Map<String, String> brandingColors = new HashMap<>();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Property property = stmt.getPredicate();
            RDFNode object = stmt.getObject();

            if (object.isLiteral()) {
                String propertyName =
                    property.getURI().contains("#") ? property.getURI().split("#")[1] : property.getURI();
                brandingColors.put(propertyName, object.asLiteral().getString());
            }
        }

        return brandingColors;
    }


    private String getRequestTheme(VitroRequest vreq) {
        String currentTheme = vreq.getParameter("theme");
        if (currentTheme == null) {
            currentTheme = getCurrentTheme(vreq);
        }
        return currentTheme;
    }

    private OntModel getDisplayModel() {
        ContextModelAccess cma = ModelAccess.getInstance();
        return cma.getOntModel(ModelNames.DISPLAY);
    }

    private void updateBrandingColor(VitroRequest vreq, HttpServletResponse response) {
        String currentTheme = getRequestTheme(vreq);
        OntModel displayModel = getDisplayModel();
        Resource themeResource = ResourceFactory.createResource(VitroVocabulary.vitroURI + currentTheme);

        Map<String, String> colorParams = new HashMap<>();
        colorParams.put(VitroVocabulary.PORTAL_THEMEPRIMARYCOLOR, vreq.getParameter("themePrimaryColor"));
        colorParams.put(VitroVocabulary.PORTAL_THEMEPRIMARYCOLORLIGHTER, vreq.getParameter("themePrimaryColorLighter"));
        colorParams.put(VitroVocabulary.PORTAL_THEMEPRIMARYCOLORDARKER, vreq.getParameter("themePrimaryColorDarker"));
        colorParams.put(VitroVocabulary.PORTAL_THEMESECONDARYCOLOR, vreq.getParameter("themeSecondaryColor"));
        colorParams.put(VitroVocabulary.PORTAL_THEMEACCENTCOLOR, vreq.getParameter("themeAccentColor"));
        colorParams.put(VitroVocabulary.PORTAL_THEMELINKCOLOR, vreq.getParameter("themeLinkColor"));
        colorParams.put(VitroVocabulary.PORTAL_THEMETEXTCOLOR, vreq.getParameter("themeTextColor"));
        colorParams.put(VitroVocabulary.PORTAL_THEMEBANNERCOLOR, vreq.getParameter("themeBannerColor"));

        for (Map.Entry<String, String> entry : colorParams.entrySet()) {
            String propertyUri = entry.getKey();
            String value = entry.getValue();
            Property property = ResourceFactory.createProperty(propertyUri);

            if (value != null) {
                displayModel.removeAll(themeResource, property, null);

                if (!value.isEmpty() && !value.equals("null")) {
                    Statement statement =
                        new StatementImpl(themeResource, property, ResourceFactory.createTypedLiteral(value));
                    displayModel.add(statement);
                }
            }
        }

        listBrandingColors(vreq, response);
    }


    private void removeAllBrandingColors(VitroRequest vreq, HttpServletResponse response) {
        String currentTheme = getRequestTheme(vreq);
        OntModel displayModel = getDisplayModel();
        Resource themeResource = ResourceFactory.createResource(VitroVocabulary.vitroURI + currentTheme);

        Property[] propertiesToRemove = {
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMEPRIMARYCOLOR),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMEPRIMARYCOLORLIGHTER),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMEPRIMARYCOLORDARKER),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMESECONDARYCOLOR),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMEACCENTCOLOR),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMELINKCOLOR),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMETEXTCOLOR),
            ResourceFactory.createProperty(VitroVocabulary.PORTAL_THEMEBANNERCOLOR)
        };

        for (Property property : propertiesToRemove) {
            displayModel.removeAll(themeResource, property, null);
        }

        listBrandingColors(vreq, response);
    }


    public static String getCurrentTheme(VitroRequest vreq) {
        WebappDaoFactory wadf = ModelAccess.on(vreq).getWebappDaoFactory();
        return wadf.getApplicationDao().getApplicationBean().getThemeDir();
    }

    public static void updateThemeBrandingCache(String theme) {
        themeBranding = getBrandingColors(theme);
        themeBrandingLoaded = true;
    }


    public static Map<String, String> getThemeBrandingCache(String theme) {
        if (!themeBrandingLoaded) {
            updateThemeBrandingCache(theme);
        }
        return themeBranding;
    }

}