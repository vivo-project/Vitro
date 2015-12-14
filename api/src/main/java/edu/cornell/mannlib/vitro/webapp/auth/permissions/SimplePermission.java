/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleRequestedAction;

/**
 * A class of simple permissions. Each instance holds a RequestedAction, and
 * will only authorize that RequestedAction (or one with the same URI).
 */
public class SimplePermission extends Permission {
	private static final Log log = LogFactory.getLog(SimplePermission.class);

	private static final String NAMESPACE = "java:"
			+ SimplePermission.class.getName() + "#";

	private static final Map<String, SimplePermission> allInstances = new HashMap<String, SimplePermission>();

	public static final SimplePermission ACCESS_SPECIAL_DATA_MODELS = new SimplePermission(
		NAMESPACE + "AccessSpecialDataModels");
	public static final SimplePermission DO_BACK_END_EDITING = new SimplePermission(
		NAMESPACE + "DoBackEndEditing");
	public static final SimplePermission DO_FRONT_END_EDITING = new SimplePermission(
		NAMESPACE + "DoFrontEndEditing");
	public static final SimplePermission EDIT_ONTOLOGY = new SimplePermission(
		NAMESPACE + "EditOntology");
	public static final SimplePermission EDIT_OWN_ACCOUNT = new SimplePermission(
		NAMESPACE + "EditOwnAccount");
	public static final SimplePermission EDIT_SITE_INFORMATION = new SimplePermission(
		NAMESPACE + "EditSiteInformation");
	public static final SimplePermission ENABLE_DEVELOPER_PANEL = new SimplePermission(
		NAMESPACE + "EnableDeveloperPanel");
	public static final SimplePermission LOGIN_DURING_MAINTENANCE = new SimplePermission(
		NAMESPACE + "LoginDuringMaintenance");
	public static final SimplePermission MANAGE_MENUS = new SimplePermission(
		NAMESPACE + "ManageMenus");
	public static final SimplePermission MANAGE_OWN_PROXIES = new SimplePermission(
		NAMESPACE + "ManageOwnProxies");
	public static final SimplePermission MANAGE_PROXIES = new SimplePermission(
		NAMESPACE + "ManageProxies");
	public static final SimplePermission MANAGE_SEARCH_INDEX = new SimplePermission(
		NAMESPACE + "ManageSearchIndex");
	public static final SimplePermission MANAGE_USER_ACCOUNTS = new SimplePermission(
		NAMESPACE + "ManageUserAccounts");
	public static final SimplePermission QUERY_FULL_MODEL = new SimplePermission(
		NAMESPACE + "QueryFullModel");
	public static final SimplePermission QUERY_USER_ACCOUNTS_MODEL = new SimplePermission(
		NAMESPACE + "QueryUserAccountsModel");
	public static final SimplePermission REFRESH_VISUALIZATION_CACHE = new SimplePermission(
		NAMESPACE + "RefreshVisualizationCache");
	public static final SimplePermission SEE_CONFIGURATION = new SimplePermission(
		NAMESPACE + "SeeConfiguration");
	public static final SimplePermission SEE_INDVIDUAL_EDITING_PANEL = new SimplePermission(
		NAMESPACE + "SeeIndividualEditingPanel");
	public static final SimplePermission SEE_REVISION_INFO = new SimplePermission(
		NAMESPACE + "SeeRevisionInfo");
	public static final SimplePermission SEE_SITE_ADMIN_PAGE = new SimplePermission(
		NAMESPACE + "SeeSiteAdminPage");
	public static final SimplePermission SEE_STARTUP_STATUS = new SimplePermission(
		NAMESPACE + "SeeStartupStatus");
	public static final SimplePermission SEE_VERBOSE_PROPERTY_INFORMATION = new SimplePermission(
		NAMESPACE + "SeeVerbosePropertyInformation");
	public static final SimplePermission USE_ADVANCED_DATA_TOOLS_PAGES = new SimplePermission(
		NAMESPACE + "UseAdvancedDataToolsPages");
	public static final SimplePermission USE_INDIVIDUAL_CONTROL_PANEL = new SimplePermission(
		NAMESPACE + "UseIndividualControlPanel");
	public static final SimplePermission USE_SPARQL_QUERY_PAGE = new SimplePermission(
		NAMESPACE + "UseSparqlQueryPage");
	public static final SimplePermission USE_SPARQL_QUERY_API = new SimplePermission(
		NAMESPACE + "UseSparqlQueryApi");
	public static final SimplePermission USE_SPARQL_UPDATE_API = new SimplePermission(
		NAMESPACE + "UseSparqlUpdateApi");

	// ----------------------------------------------------------------------
	// These instances are "catch all" permissions to cover poorly defined
	// groups of actions until better definitions were found. Don't add usages
	// of these, and remove existing usages where possible.
	// ----------------------------------------------------------------------

	public static final SimplePermission USE_BASIC_AJAX_CONTROLLERS = new SimplePermission(
		NAMESPACE + "UseBasicAjaxControllers");
	public static final SimplePermission USE_MISCELLANEOUS_ADMIN_PAGES = new SimplePermission(
		NAMESPACE + "UseMiscellaneousAdminPages");
	public static final SimplePermission USE_MISCELLANEOUS_CURATOR_PAGES = new SimplePermission(
		NAMESPACE + "UseMiscellaneousCuratorPages");
	public static final SimplePermission USE_MISCELLANEOUS_PAGES = new SimplePermission(
		NAMESPACE + "UseMiscellaneousPages");

	// ----------------------------------------------------------------------
	// These instances are permissions that can be specified for a given page created/managed through page management,
	// e.g. this page is viewable only by admins, this page is viewable to anyone who is logged in, etc.
	// ----------------------------------------------------------------------
	public static final SimplePermission PAGE_VIEWABLE_ADMIN = new SimplePermission(
			NAMESPACE + "PageViewableAdmin");
		public static final SimplePermission PAGE_VIEWABLE_CURATOR = new SimplePermission(
			NAMESPACE + "PageViewableCurator");
		public static final SimplePermission PAGE_VIEWABLE_LOGGEDIN = new SimplePermission(
			NAMESPACE + "PageViewableLoggedIn");
		public static final SimplePermission PAGE_VIEWABLE_EDITOR = new SimplePermission(
			NAMESPACE + "PageViewableEditor");
		public static final SimplePermission PAGE_VIEWABLE_PUBLIC = new SimplePermission(
			NAMESPACE + "PageViewablePublic");
	
	
	public static List<SimplePermission> getAllInstances() {
		return new ArrayList<SimplePermission>(allInstances.values());
	}

	//private final String localName;
	public final RequestedAction ACTION;

	public SimplePermission(String uri) {
		super(uri);
		
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}

		this.ACTION = new SimpleRequestedAction(uri);

		if (allInstances.containsKey(this.uri)) {
			throw new IllegalStateException("A SimplePermission named '"
					+ this.uri + "' already exists.");
		}
		allInstances.put(uri, this);
	}

	@Override
	public boolean isAuthorized(RequestedAction whatToAuth) {
		if (whatToAuth != null) {
			if (ACTION.getURI().equals(whatToAuth.getURI())) {
				log.debug(this + " authorizes " + whatToAuth);
				return true;
			}
		}
		log.debug(this + " does not authorize " + whatToAuth);
		return false;
	}

	@Override
	public String toString() {
		return "SimplePermission['" + uri+ "']";
	}

}
