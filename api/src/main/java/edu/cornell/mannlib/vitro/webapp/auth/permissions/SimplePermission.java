/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.NamedAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;

public class SimplePermission {

  //Access rules stored here for compatibility reasons

    public static final SimplePermission ACCESS_SPECIAL_DATA_MODELS = new SimplePermission("AccessSpecialDataModels");
    public static final SimplePermission DO_BACK_END_EDITING = new SimplePermission("DoBackEndEditing");
    public static final SimplePermission DO_FRONT_END_EDITING = new SimplePermission("DoFrontEndEditing");
    public static final SimplePermission EDIT_ONTOLOGY = new SimplePermission("EditOntology");
    public static final SimplePermission EDIT_OWN_ACCOUNT = new SimplePermission("EditOwnAccount");
    public static final SimplePermission EDIT_SITE_INFORMATION = new SimplePermission("EditSiteInformation");
    public static final SimplePermission ENABLE_DEVELOPER_PANEL = new SimplePermission("EnableDeveloperPanel");
    public static final SimplePermission LOGIN_DURING_MAINTENANCE = new SimplePermission("LoginDuringMaintenance");
    public static final SimplePermission MANAGE_MENUS = new SimplePermission("ManageMenus");
    public static final SimplePermission MANAGE_OWN_PROXIES = new SimplePermission("ManageOwnProxies");
    public static final SimplePermission MANAGE_PROXIES = new SimplePermission("ManageProxies");
    public static final SimplePermission MANAGE_SEARCH_INDEX = new SimplePermission("ManageSearchIndex");
    public static final SimplePermission MANAGE_ROLES = new SimplePermission("ManageRoles");
    public static final SimplePermission MANAGE_USER_ACCOUNTS = new SimplePermission("ManageUserAccounts");
    public static final SimplePermission QUERY_FULL_MODEL = new SimplePermission("QueryFullModel");
    public static final SimplePermission QUERY_USER_ACCOUNTS_MODEL = new SimplePermission("QueryUserAccountsModel");
    public static final SimplePermission REFRESH_VISUALIZATION_CACHE = new SimplePermission("RefreshVisualizationCache");
    public static final SimplePermission SEE_CONFIGURATION = new SimplePermission("SeeConfiguration");
    public static final SimplePermission SEE_INDVIDUAL_EDITING_PANEL = new SimplePermission("SeeIndividualEditingPanel");
    public static final SimplePermission SEE_REVISION_INFO = new SimplePermission("SeeRevisionInfo");
    public static final SimplePermission SEE_SITE_ADMIN_PAGE = new SimplePermission("SeeSiteAdminPage");
    public static final SimplePermission SEE_STARTUP_STATUS = new SimplePermission("SeeStartupStatus");
    public static final SimplePermission SEE_VERBOSE_PROPERTY_INFORMATION = new SimplePermission("SeeVerbosePropertyInformation");
    public static final SimplePermission USE_ADVANCED_DATA_TOOLS_PAGES = new SimplePermission("UseAdvancedDataToolsPages");
    public static final SimplePermission USE_INDIVIDUAL_CONTROL_PANEL = new SimplePermission("UseIndividualControlPanel");
    public static final SimplePermission USE_SPARQL_QUERY_PAGE = new SimplePermission("UseSparqlQueryPage");
    public static final SimplePermission USE_SPARQL_QUERY_API = new SimplePermission("UseSparqlQueryApi");
    public static final SimplePermission USE_SPARQL_UPDATE_API = new SimplePermission("UseSparqlUpdateApi");

    // ----------------------------------------------------------------------
    // These instances are "catch all" permissions to cover poorly defined
    // groups of actions until better definitions were found. Don't add usages
    // of these, and remove existing usages where possible.
    // ----------------------------------------------------------------------
    
    public static final SimplePermission USE_BASIC_AJAX_CONTROLLERS = new SimplePermission("UseBasicAjaxControllers");
    public static final SimplePermission USE_MISCELLANEOUS_ADMIN_PAGES = new SimplePermission("UseMiscellaneousAdminPages");
    public static final SimplePermission USE_MISCELLANEOUS_CURATOR_PAGES = new SimplePermission("UseMiscellaneousCuratorPages");
    public static final SimplePermission USE_MISCELLANEOUS_PAGES = new SimplePermission("UseMiscellaneousPages");

    // ----------------------------------------------------------------------
    // These instances are permissions that can be specified for a given page
    // created/managed through page management,
    // e.g. this page is viewable only by admins, this page is viewable to anyone
    // who is logged in, etc.
    // ----------------------------------------------------------------------

    public static final SimplePermission PAGE_VIEWABLE_ADMIN = new SimplePermission("PageViewableAdmin");
    public static final SimplePermission PAGE_VIEWABLE_CURATOR = new SimplePermission("PageViewableCurator");
    public static final SimplePermission PAGE_VIEWABLE_LOGGEDIN = new SimplePermission("PageViewableLoggedIn");
    public static final SimplePermission PAGE_VIEWABLE_EDITOR = new SimplePermission("PageViewableEditor");
    public static final SimplePermission PAGE_VIEWABLE_PUBLIC = new SimplePermission("PageViewablePublic");
    
    public static final SimplePermission MANAGE_DATA_DISTRIBUTORS = new SimplePermission("ManageDataDistributors");
    public static final SimplePermission MANAGE_REPORTS = new SimplePermission("ManageReports");
    
    public SimpleAuthorizationRequest ACTION;
    private String uri;

    public String getUri() {
        return uri;
    }

    public static final String NS = "java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#";

    private SimplePermission(String uri) {
        this.uri = SimplePermission.NS + uri;
        NamedAccessObject ao = new NamedAccessObject(this.uri);
        this.ACTION = new SimpleAuthorizationRequest(ao, AccessOperation.EXECUTE);
    }
}
