/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_PUBLIC_URI;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.DynamicPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyStore;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTemplateController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ResourceImpl;

@WebServlet(name = "RolesController", urlPatterns = { "/admin/roles" })
public class RolesController extends FreemarkerHttpServlet {
    protected static final String URI_PARAM = "uri";
    protected static final String LABEL_PARAM = "label";
    protected static final String ACTION_PARAM = "action";
    private static final Log log = LogFactory.getLog(RolesController.class);
    private static final String TEMPLATE = "userRoles-list.ftl";

    enum Action {
        ADD,
        REMOVE,
        EDIT,
        LIST
    };

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doGet(request, response);
    }

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return SimplePermission.MANAGE_ROLES.ACTION;
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
        Action action = getAction(vreq);
        switch (action) {
            case ADD:
                return processAdd(vreq);
            case REMOVE:
                return processRemove(vreq);
            case EDIT:
                return processEdit(vreq);
            // LIST
            default:
                return processList(vreq);
        }
    }

    private Action getAction(VitroRequest vreq) {
        return readEnumValue(Action.class, vreq.getParameter(ACTION_PARAM), Action.LIST);
    }

    public static <T extends Enum<?>> T readEnumValue(Class<T> valueClass, String string, T defaultValue) {
        if (string == null) {
            return defaultValue;
        }
        for (T value : valueClass.getEnumConstants()) {
            if (value.name().compareToIgnoreCase(string) == 0) {
                return value;
            }
        }
        return defaultValue;
    }

    private ResponseValues processRemove(VitroRequest vreq) {
        String uri = vreq.getParameter(URI_PARAM);
        if (StringUtils.isBlank(uri)) {
            TemplateResponseValues response = processList(vreq);
            response.put("errorMessage", I18n.text(vreq, "removed_role_uri_is_invalid", new Object[] { uri }));
            log.error(String.format("Uri '%s' of role to remove is not valid", uri));
            response.setStatusCode(400);
            return response;
        }
        if (ROLE_PUBLIC_URI.equals(uri)) {
            TemplateResponseValues response = processList(vreq);
            String errorMessage = "Removal of the " + uri + " role is not allowed.";
            response.put("errorMessage", errorMessage);
            log.error(errorMessage);
            response.setStatusCode(400);
            return response;
        }
        removeRole(uri);
        return processList(vreq);
    }

    private ResponseValues processEdit(VitroRequest vreq) {
        String uri = vreq.getParameter(URI_PARAM);
        String label = vreq.getParameter(LABEL_PARAM);
        if (StringUtils.isBlank(uri)) {
            TemplateResponseValues response = processList(vreq);
            response.put("errorMessage", I18n.text(vreq, "edited_role_uri_is_invalid", new Object[] { uri }));
            log.error(String.format("Uri '%s' of role to edit is not valid", uri));
            response.setStatusCode(400);
            return response;
        }
        if (StringUtils.isBlank(label)) {
            TemplateResponseValues response = processList(vreq);
            response.put("errorMessage", I18n.text(vreq, "edited_role_label_is_invalid", new Object[] { label }));
            log.error(String.format("New label '%s' of role '%s' to edit is not valid", label, uri));
            response.setStatusCode(400);
            return response;
        }
        editRole(uri, label, vreq);
        TemplateResponseValues response = processList(vreq);
        return response;
    }

    private ResponseValues processAdd(VitroRequest vreq) {
        String label = vreq.getParameter(LABEL_PARAM);
        if (StringUtils.isBlank(label)) {
            TemplateResponseValues response = processList(vreq);
            response.put("errorMessage", I18n.text(vreq, "added_role_label_is_invalid", new Object[] { label }));
            log.error(String.format("Label '%s' of new role to add is not valid", label));
            response.setStatusCode(400);
            return response;
        }
        addRole(label, vreq);
        return processList(vreq);
    }

    private static final String CREATE_PERMISSION_SET_QUERY = ""
            + "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "CONSTRUCT {\n"
            + "  ?uri a auth:PermissionSet .\n"
            + "  ?uri rdfs:label ?label . }\n"
            + "WHERE {\n"
            + "  BIND(?newUri as ?uri)\n"
            + "}";

    private void addRole(String label, VitroRequest vreq) {
        OntModel userAccounts = ModelAccess.getInstance().getOntModel(USER_ACCOUNTS);
        String newUri = createRoleUris(label, userAccounts);
        ParameterizedSparqlString pss = new ParameterizedSparqlString(CREATE_PERMISSION_SET_QUERY);
        pss.setIri("newUri", newUri);
        Locale locale = vreq.getLocale();
        if (locale != null) {
            pss.setLiteral(LABEL_PARAM, label, locale.toLanguageTag().replace("_", "-"));
        } else {
            pss.setLiteral(LABEL_PARAM, label);
        }
        try (QueryExecution qe = QueryExecutionFactory.create(pss.toString(), userAccounts)) {
            Model toAdd = qe.execConstruct();
            userAccounts.add(toAdd);
        }
        Collection<String> dataSetUris = PolicyTemplateController.createRoleDataSets(newUri);
        for (String datasetUri : dataSetUris) {
            DynamicPolicy policy = PolicyLoader.getInstance().loadPolicyFromTemplateDataSet(datasetUri);
            PolicyStore.getInstance().add(policy);
        }
    }

    private String createRoleUris(String label, OntModel userAccounts) {
        String uriSuffix = label.toUpperCase().replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_]+", "");
        String uri = VitroVocabulary.VITRO_AUTH + "ROLE_" + uriSuffix;
        Random random = new Random();
        while (userAccounts.containsResource(new ResourceImpl(uri))) {
            uri += random.nextInt(9);
        }
        return uri;
    }

    private static final String GET_PERMISSION_SET_TO_REMOVE_QUERY = ""
            + "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "CONSTRUCT {\n"
            + "  ?uri ?p ?o . }\n"
            + "WHERE {\n"
            + "  ?uri a auth:PermissionSet .\n"
            + "  ?uri ?p ?o .\n"
            + "}";

    private void removeRole(String uri) {
        OntModel m = ModelAccess.getInstance().getOntModel(USER_ACCOUNTS);
        ParameterizedSparqlString pss = new ParameterizedSparqlString(GET_PERMISSION_SET_TO_REMOVE_QUERY);
        pss.setIri(URI_PARAM, uri);
        try (QueryExecution qe = QueryExecutionFactory.create(pss.toString(), m)) {
            Model toRemove = qe.execConstruct();
            m.remove(toRemove);
        }
        PolicyTemplateController.removeRoleDataSets(uri);
    }

    private static final String GET_PERMISSION_SET_LABELS_QUERY = ""
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "CONSTRUCT {\n"
            + "  ?uri rdfs:label ?label .}\n"
            + "WHERE {\n"
            + "  ?uri rdfs:label ?label .\n"
            + "  FILTER(lang(?label) = ?language)"
            + "}";

    private static final String NEW_PERMISSION_SET_LABEL_QUERY = ""
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "CONSTRUCT {\n"
            + "  ?uri rdfs:label ?label .}\n"
            + "WHERE {\n"
            + "  BIND(?newLabel as ?label)\n"
            + "}";

    private void editRole(String uri, String newLabel, VitroRequest vreq) {
        OntModel userAccounts = ModelAccess.on(vreq).getOntModel(USER_ACCOUNTS);
        ParameterizedSparqlString removeLabelQuery = new ParameterizedSparqlString(GET_PERMISSION_SET_LABELS_QUERY);
        removeLabelQuery.setIri(URI_PARAM, uri);
        String languageTag = "";
        Locale locale = vreq.getLocale();
        if (locale != null) {
            languageTag = locale.toLanguageTag().replace("_", "-");
        }
        removeLabelQuery.setLiteral("language", languageTag);
        Model toRemove = null;
        try (QueryExecution qe = QueryExecutionFactory.create(removeLabelQuery.toString(), userAccounts)) {
            toRemove = qe.execConstruct();
        }
        ParameterizedSparqlString addLabelQuery = new ParameterizedSparqlString(NEW_PERMISSION_SET_LABEL_QUERY);
        addLabelQuery.setIri(URI_PARAM, uri);
        addLabelQuery.setLiteral("newLabel", newLabel, languageTag);
        Model toAdd = null;
        try (QueryExecution qe = QueryExecutionFactory.create(addLabelQuery.toString(), userAccounts)) {
            toAdd = qe.execConstruct();
        }
        if (toAdd != null && toRemove != null) {
            userAccounts.remove(toRemove);
            userAccounts.add(toAdd);
        }
    }

    private TemplateResponseValues processList(VitroRequest vreq) {
        WebappDaoFactory wadf = ModelAccess.on(vreq).getWebappDaoFactory();
        List<PermissionSet> roles = new ArrayList<>(wadf.getUserAccountsDao().getAllPermissionSets());
        TemplateResponseValues response = new TemplateResponseValues(TEMPLATE);
        response.put("roles", roles);
        return response;
    }
}