/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerStatus;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexController;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class BaseSiteAdminController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(BaseSiteAdminController.class);
    protected static final String TEMPLATE_DEFAULT = "siteAdmin-main.ftl";

    public static final AuthorizationRequest REQUIRED_ACTIONS = SimplePermission.SEE_SITE_ADMIN_PAGE.ACTION;

    private static final List<AdminUrl> siteMaintenanceUrls = new ArrayList<>();
    private static final List<AdminUrl> siteConfigData = new ArrayList<>();

    public static void registerSiteMaintenanceUrl(String key, String url, ParamMap urlParams, AuthorizationRequest permission) {
        AdminUrl adminUrl = new AdminUrl();

        adminUrl.key = key;
        adminUrl.url = url;
        adminUrl.urlParams = urlParams;
        adminUrl.permission = permission;

        siteMaintenanceUrls.add(adminUrl);
    }

    public static void registerSiteConfigData(String key, String url, ParamMap urlParams, AuthorizationRequest permission) {
        AdminUrl adminUrl = new AdminUrl();

        adminUrl.key = key;
        adminUrl.url = url;
        adminUrl.urlParams = urlParams;
        adminUrl.permission = permission;

        siteConfigData.add(adminUrl);
    }

    static {
        registerSiteMaintenanceUrl("recomputeInferences", "/RecomputeInferences", null, SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION);
        registerSiteMaintenanceUrl("rebuildSearchIndex", "/SearchIndex", null, IndexController.REQUIRED_ACTIONS);
        registerSiteMaintenanceUrl("startupStatus", "/startupStatus", null, SimplePermission.SEE_STARTUP_STATUS.ACTION);
        registerSiteMaintenanceUrl("restrictLogins", "/admin/restrictLogins", null, SimplePermission.LOGIN_DURING_MAINTENANCE.ACTION);
        registerSiteMaintenanceUrl("activateDeveloperPanel", "javascript:new DeveloperPanel(developerAjaxUrl).setupDeveloperPanel({developer_enabled: true});", null, SimplePermission.ENABLE_DEVELOPER_PANEL.ACTION);

        registerSiteConfigData("userRoles", "/admin/roles", null, SimplePermission.MANAGE_ROLES.ACTION);
        registerSiteConfigData("userAccounts", "/accountsAdmin", null, SimplePermission.MANAGE_USER_ACCOUNTS.ACTION);
        registerSiteConfigData("manageProxies", "/manageProxies", null, SimplePermission.MANAGE_PROXIES.ACTION);
        registerSiteConfigData("manageDataDistributors", "/admin/datadistributor", null, SimplePermission.MANAGE_DATA_DISTRIBUTORS.ACTION);
        registerSiteConfigData("manageReports", "/admin/reporting", null, SimplePermission.MANAGE_REPORTS.ACTION);

        registerSiteConfigData("siteInfo", "/editForm", new ParamMap(new String[] {"controller", "ApplicationBean"}), SimplePermission.EDIT_SITE_INFORMATION.ACTION);

        //TODO: Add specific permissions for page management
        registerSiteConfigData("menuManagement", "/individual", new ParamMap(new String[] {
                "uri", "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#DefaultMenu",
                "switchToDisplayModel", "true" }), SimplePermission.MANAGE_MENUS.ACTION);

        registerSiteConfigData("pageManagement", "/pageList", null, SimplePermission.MANAGE_MENUS.ACTION);
    }

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return REQUIRED_ACTIONS;
	}

	@Override
	public String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Site Administration";
	}

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();

        body.put("dataInput", getDataInputData(vreq));
        body.put("siteConfig", getSiteConfigData(vreq));
        body.put("siteMaintenance", getSiteMaintenanceUrls(vreq));
        body.put("ontologyEditor", getOntologyEditorData(vreq));
        body.put("dataTools", getDataToolsUrls(vreq));

        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }

    protected Map<String, Object> getSiteMaintenanceUrls(VitroRequest vreq) {

        Map<String, Object> urls = new HashMap<>();

        for (AdminUrl adminUrl : siteMaintenanceUrls) {
            if (adminUrl.permission == null || PolicyHelper.isAuthorizedForActions(vreq, adminUrl.permission)) {
                if (adminUrl.url.startsWith("javascript:")) {
                    urls.put(adminUrl.key, adminUrl.url);
                } else {
                    if (adminUrl.urlParams != null) {
                        urls.put(adminUrl.key, UrlBuilder.getUrl(adminUrl.url, adminUrl.urlParams));
                    } else {
                        urls.put(adminUrl.key, UrlBuilder.getUrl(adminUrl.url));
                    }
                }
            }
        }

        if (PolicyHelper.isAuthorizedForActions(vreq, SimplePermission.SEE_STARTUP_STATUS.ACTION)) {
        	urls.put("startupStatusAlert", !StartupStatus.getBean(getServletContext()).allClear());
        }

        return urls;
    }

    protected Map<String, Object> getDataInputData(VitroRequest vreq) {

        Map<String, Object> map = new HashMap<String, Object>();

		if (PolicyHelper.isAuthorizedForActions(vreq,
				SimplePermission.DO_BACK_END_EDITING.ACTION)) {

            map.put("formAction", UrlBuilder.getUrl("/editRequestDispatch"));

            WebappDaoFactory wadf = vreq.getUnfilteredWebappDaoFactory();

            // Create map for data input entry form options list
            List<VClassGroup> classGroups = wadf.getVClassGroupDao().getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals

            Set<String> seenGroupNames = new HashSet<String>();

            Iterator<VClassGroup> classGroupIt = classGroups.iterator();
            LinkedHashMap<String, List<Option>> orderedClassGroups = new LinkedHashMap<String, List<Option>>(classGroups.size());
            while (classGroupIt.hasNext()) {
                VClassGroup group = classGroupIt.next();
                List<Option> opts = FormUtils.makeOptionListFromBeans(group.getVitroClassList(),"URI","PickListName",null,null,false);
                if( seenGroupNames.contains(group.getPublicName() )){
                    //have a duplicate classgroup name, stick in the URI
                    orderedClassGroups.put(group.getPublicName() + " ("+group.getURI()+")", opts);
                }else if( group.getPublicName() == null ){
                    //have an unlabeled group, stick in the URI
                    orderedClassGroups.put("unnamed group ("+group.getURI()+")", opts);
                }else{
                    orderedClassGroups.put(group.getPublicName(),opts);
                    seenGroupNames.add(group.getPublicName());
                }
            }

            map.put("groupedClassOptions", orderedClassGroups);
        }
        return map;
    }

    protected Map<String, Object> getSiteConfigData(VitroRequest vreq) {

        Map<String, Object> data = new HashMap<String, Object>();

        for (AdminUrl adminUrl : siteConfigData) {
            if (adminUrl.permission == null || PolicyHelper.isAuthorizedForActions(vreq, adminUrl.permission)) {
                if (adminUrl.url.startsWith("javascript:")) {
                    data.put(adminUrl.key, adminUrl.url);
                } else {
                    if (adminUrl.urlParams != null) {
                        data.put(adminUrl.key, UrlBuilder.getUrl(adminUrl.url, adminUrl.urlParams));
                    } else {
                        data.put(adminUrl.key, UrlBuilder.getUrl(adminUrl.url));
                    }
                }
            }
        }

        return data;
    }

    protected Map<String, Object> getOntologyEditorData(VitroRequest vreq) {

        Map<String, Object> map = new HashMap<String, Object>();

        if (PolicyHelper.isAuthorizedForActions(vreq, SimplePermission.EDIT_ONTOLOGY.ACTION)) {

            String error = null;
            String explanation = null;
            try {
                TBoxReasonerStatus status = ApplicationUtils.instance().getTBoxReasonerModule().getStatus();
                if (!status.isConsistent()) {
                    error = "INCONSISTENT ONTOLOGY: reasoning halted.";
                    explanation = status.getExplanation();
                } else if ( status.isInErrorState() ) {
                    error = "An error occurred during reasoning. Reasoning has been halted. See error log for details.";
                }
            } catch (IllegalStateException e) {
                error = "The inferencing engine is disabled. Data entered manually may exhibit unexpected behavior prior to inferencing.";
                log.debug("Status of reasoner could not be determined. It is likely disabled.", e);
            }

            if (error != null) {
                Map<String, String> tboxReasonerStatus = new HashMap<String, String>();
                tboxReasonerStatus.put("error", error);
                if (explanation != null) {
                    tboxReasonerStatus.put("explanation", explanation);
                }
                map.put("tboxReasonerStatus", tboxReasonerStatus);
            }

            Map<String, String> urls = new HashMap<String, String>();

            urls.put("ontologies", UrlBuilder.getUrl("/listOntologies"));
            urls.put("classHierarchy", UrlBuilder.getUrl("/showClassHierarchy"));
            urls.put("classGroups", UrlBuilder.getUrl("/listGroups"));
            urls.put("dataPropertyHierarchy", UrlBuilder.getUrl("/showDataPropertyHierarchy"));
            urls.put("fauxPropertyList", UrlBuilder.getUrl("/listFauxProperties"));
            urls.put("propertyGroups", UrlBuilder.getUrl("/listPropertyGroups"));
            urls.put("objectPropertyHierarchy", UrlBuilder.getUrl("/showObjectPropertyHierarchy", new ParamMap("iffRoot", "true")));
            map.put("urls", urls);
        }

        return map;
    }

    protected Map<String, String> getDataToolsUrls(VitroRequest vreq) {

        Map<String, String> urls = new HashMap<String, String>();

        if (PolicyHelper.isAuthorizedForActions(vreq, SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
            urls.put("ingest", UrlBuilder.getUrl("/ingest"));
            urls.put("rdfData", UrlBuilder.getUrl("/uploadRDFForm"));
            urls.put("rdfExport", UrlBuilder.getUrl("/export"));
//            urls.put("sparqlQueryBuilder", UrlBuilder.getUrl("/admin/sparqlquerybuilder"));
        }
        if (PolicyHelper.isAuthorizedForActions(vreq, SimplePermission.USE_SPARQL_QUERY_PAGE.ACTION)) {
        	urls.put("sparqlQuery", UrlBuilder.getUrl("/admin/sparqlquery"));
        }

        return urls;
    }

    final static class AdminUrl {
        String key;
        String url;
        ParamMap urlParams;
        AuthorizationRequest permission;
    }
}
