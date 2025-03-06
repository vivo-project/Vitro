/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import org.apache.jena.atlas.json.JsonObject;

import org.apache.jena.rdf.model.*;

import org.apache.jena.rdf.model.impl.StatementImpl;


import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;


import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.controller.edit.ReorderController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle adding, replacing or deleting the custom css file.
 */
@WebServlet(name = "SiteStyleController", urlPatterns = { "/siteBranding" })
public class SiteBrandingController extends VitroAjaxController {

	private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ReorderController.class);

	@Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	// return SimplePermission.SEE_SITE_ADMIN_PAGE.ACTION;
        return SimplePermission.PAGE_VIEWABLE_PUBLIC.ACTION;

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
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Only GET and POST methods are supported");
        }
    }

    private void handlePostRequest(VitroRequest vreq, HttpServletResponse response) throws IOException {
        String action = vreq.getParameter("action");

        switch (action != null ? action.toLowerCase() : "") {
            case "update":
                updateBrandingColor(vreq, response);
                break;

            case "removeall":
                removeAllBrandingColors(vreq, response);
                response.setStatus(HttpServletResponse.SC_OK);
                break;

            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action parameter");
        }
    }

    private void listBrandingColors(VitroRequest vreq, HttpServletResponse response) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);


        String currentTheme = vreq.getParameter("theme");
        if (currentTheme == null) {
            currentTheme = getCurrentTheme(vreq);
        }

        Resource s = ResourceFactory.createResource(VitroVocabulary.vitroURI + currentTheme);
        StmtIterator iter = displayModel.listStatements(s, null, (RDFNode) null);
    
        JsonObject jsonResponse = new JsonObject();
    
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Property property = stmt.getPredicate();
            RDFNode object = stmt.getObject();
    
            if (object.isLiteral()) {
                String propertyName = property.getURI().contains("#") ? property.getURI().split("#")[1] : property.getURI();
                jsonResponse.put(propertyName, object.asLiteral().getString());
            }
        }
    
        response.setContentType("application/json");
        try {
            response.getWriter().write(jsonResponse.toString());
        } catch (IOException e) {
            log.error("Error writing response", e);
        }
    }
    

    private void updateBrandingColor(VitroRequest vreq, HttpServletResponse response) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        String currentTheme = vreq.getParameter("theme");
        if (currentTheme == null) {
            currentTheme = getCurrentTheme(vreq);
        }

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

                if ( !value.isEmpty() && !value.equals("null")) {
                    Statement statement = new StatementImpl(themeResource, property, ResourceFactory.createTypedLiteral(value));
                    displayModel.add(statement);
                }
            }
        }

        listBrandingColors(vreq, response);
    }


    private void removeAllBrandingColors(VitroRequest vreq, HttpServletResponse response) {
        ContextModelAccess cma = ModelAccess.getInstance();
        OntModel displayModel = cma.getOntModel(ModelNames.DISPLAY);

        String currentTheme = vreq.getParameter("theme");
        if (currentTheme == null) {
            currentTheme = getCurrentTheme(vreq);
        }

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
    

    private String getCurrentTheme(VitroRequest vreq) {
        WebappDaoFactory wadf = ModelAccess.on(vreq).getWebappDaoFactory();
        return wadf.getApplicationDao().getApplicationBean().getThemeDir();
    }

}