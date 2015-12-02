package edu.ucsf.vitro.opensocial;

import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProfile;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualRequestAnalysisContextImpl;
import edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualRequestAnalyzer;
import edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualRequestInfo;

public class OpenSocialManager {
	public static final String SHINDIG_URL_PROP = "OpenSocial.shindigURL";
	
	public static final String OPENSOCIAL_DEBUG = "OPENSOCIAL_DEBUG";
	public static final String OPENSOCIAL_NOCACHE = "OPENSOCIAL_NOCACHE";
	public static final String OPENSOCIAL_GADGETS = "OPENSOCIAL_GADGETS";

	public static final String JSON_PERSONID_CHANNEL = "JSONPersonIds";
	public static final String JSON_PMID_CHANNEL = "JSONPubMedIds";
	public static final String TAG_NAME = "openSocial";

	private static final String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";
	
    private static final Log log = LogFactory.getLog(OpenSocialManager.class);	

	// for performance
	private static Map<String, GadgetSpec> gadgetCache;
	
	private List<PreparedGadget> gadgets = new ArrayList<PreparedGadget>();
	private Map<String, String> pubsubdata = new HashMap<String, String>();
	private String viewerId = null;
	private String ownerId = null;
	private boolean isDebug = false;
	private boolean noCache = false;
	private String pageName;
	private ConfigurationProperties configuration;

	private BasicDataSource dataSource;

	public OpenSocialManager(VitroRequest vreq, String pageName) throws SQLException, IOException {
		this(vreq, pageName, false);
	}
	
	public OpenSocialManager(VitroRequest vreq, String pageName, boolean editMode) throws SQLException, IOException {
		this.isDebug = vreq.getSession() != null
				&& Boolean.TRUE.equals(vreq.getSession().getAttribute(OPENSOCIAL_DEBUG));
		this.noCache = vreq.getSession() != null
				&& Boolean.TRUE.equals(vreq.getSession().getAttribute(OPENSOCIAL_NOCACHE));
		this.pageName = pageName;

		configuration = ConfigurationProperties.getBean(vreq.getSession()
				.getServletContext());

		if (configuration.getProperty(SHINDIG_URL_PROP) == null) {
			// do nothing
			return;
		}

		// Analyze the request to figure out whose page we are viewing.
		this.ownerId = figureOwnerId(vreq);

		// If we are authorized to edit on behalf of the page owner,
		// set the viewer ID to be the owner ID, so it looks like we are the page owner.
		if (editMode) {
			this.viewerId = ownerId;
		}
		else {
			// If we have a profile page, use that URI. Otherwise, use the URI of the logged-in user account, if any.
			UserAccount viewer = LoginStatusBean.getCurrentUser(vreq);
			Collection<String> profileUris = HasProfile.getProfileUris(RequestIdentifiers.getIdBundleForRequest(vreq));
			if (!profileUris.isEmpty()) {
				this.viewerId = profileUris.iterator().next();
			} else if (viewer != null) {
				this.viewerId = viewer.getUri();
			} else {
				this.viewerId = null;
			}
		}
		
		String requestAppId = vreq.getParameter("appId");

		dataSource = new BasicDataSource();
		dataSource.setDriverClassName(DEFAULT_DRIVER);
		dataSource.setUsername(configuration
				.getProperty("VitroConnection.DataSource.username"));
		dataSource.setPassword(configuration
				.getProperty("VitroConnection.DataSource.password"));
		dataSource.setUrl(configuration
				.getProperty("VitroConnection.DataSource.url"));

		// Load gadgets from the DB first
		Map<String, GadgetSpec> allDBGadgets = getAllDBGadgets(!noCache);

		// Add sandbox gadgets if they are present
		if (vreq.getSession() != null && vreq.getSession().getAttribute(OPENSOCIAL_GADGETS) != null) {		
			gadgets = getSandboxGadgets(vreq, allDBGadgets, requestAppId);
		}
		else { 		
			// if no manual one were added, use the ones from the DB
			for (GadgetSpec gadgetSpec : allDBGadgets.values()) {
				// only add ones that are visible in this context!
				int moduleId = 0;
				if (
						(
								(requestAppId == null && gadgetSpec.isEnabled()) || 
								(requestAppId != null && gadgetSpec.getAppId() ==  Integer.parseInt(requestAppId))
						) && 
						gadgetSpec.show(viewerId, ownerId, pageName, dataSource)
						) {
					String securityToken = socketSendReceive(viewerId, ownerId, "" + gadgetSpec.getAppId());
					gadgets.add(new PreparedGadget(gadgetSpec, this, moduleId++, securityToken));
				}
			}
		}

		// sort the gadgets
		Collections.sort(gadgets);
	}
	
	public static void clearCache() {
		gadgetCache = null;
	}

	private String figureOwnerId(VitroRequest vreq) {
		IndividualRequestAnalyzer requestAnalyzer = new IndividualRequestAnalyzer(vreq,
				new IndividualRequestAnalysisContextImpl(vreq));
		IndividualRequestInfo requestInfo = requestAnalyzer.analyze();
		Individual owner = requestInfo.getIndividual();
		return owner != null ? owner.getURI() : null;
	}
	
	private String getGadgetFileNameFromURL(String url) {
		String[] urlbits = url.split("/");
		return urlbits[urlbits.length - 1];
	}

	public boolean isDebug() {
		return isDebug;
	}

	public boolean noCache() {
		return noCache;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public boolean hasGadgetListeningTo(String channel) {
		for (PreparedGadget gadget : getVisibleGadgets()) {
			if (gadget.getGadgetSpec().listensTo(channel)) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getOpenSocialId(List<Individual> individuals) {
		List<String> personIds = new ArrayList<String>();
		for (Individual ind : individuals) {
			personIds.add(ind.getURI());
		}
		return personIds;
	}

	// JSON Helper Functions
	public static String buildJSONPersonIds(List<String> personIds,
			String message) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("message", message);
		json.put("personIds", personIds);
		return json.toString();
	}
	
	public static String buildJSONPersonIds(String personId, String message) throws JSONException {
		List<String> personIds = new ArrayList<String>();
		personIds.add(personId);
		return buildJSONPersonIds(personIds, message);
	}

	public static String buildJSONPersonIds(Individual ind, String message) throws JSONException {
		List<String> personIds = new ArrayList<String>();
		personIds.add(ind.getURI());
		return buildJSONPersonIds(personIds, message);
	}

	public void setPubsubData(String key, String value) {
		if (pubsubdata.containsKey(key)) {
			pubsubdata.remove(key);
		}
		if (value != null && !value.isEmpty()) {
			pubsubdata.put(key, value);
		}
	}

	public Map<String, String> getPubsubData() {
		return pubsubdata;
	}

	public void removePubsubGadgetsWithoutData() {
		// if any visible gadgets depend on pubsub data that isn't present,
		// throw them out
		List<PreparedGadget> removedGadgets = new ArrayList<PreparedGadget>();
		for (PreparedGadget gadget : gadgets) {
			for (String channel : gadget.getGadgetSpec().getChannels()) {
				if (!pubsubdata.containsKey(channel)) {
					removedGadgets.add(gadget);
					break;
				}
			}
		}
		for (PreparedGadget gadget : removedGadgets) {
			gadgets.remove(gadget);
		}
	}

	public void removeGadget(String name) {
		// if any visible gadgets depend on pubsub data that isn't present,
		// throw them out
		PreparedGadget gadgetToRemove = null;
		for (PreparedGadget gadget : gadgets) {
			if (name.equals(gadget.getName())) {
				gadgetToRemove = gadget;
				break;
			}
		}
		gadgets.remove(gadgetToRemove);
	}

	public String getPageName() {
		return pageName;
	}

	public String getIdToUrlMapJavascript() {
		String retval = "var idToUrlMap = {";
		for (PreparedGadget gadget : gadgets) {
			// retval += gadget.GetAppId() + ":'" + gadget.GetGadgetURL() +
			// "', ";
			retval += "'remote_iframe_" + gadget.getAppId() + "':'"
					+ gadget.getGadgetURL() + "', ";
		}
		return retval.substring(0, retval.length() - 2) + "};";
	}

	public boolean isVisible() {
		// always have turned on for ProfileDetails.aspx because we want to
		// generate the "profile was viewed" in Javascript (bot proof)
		// regardless of any gadgets being visible, and we need this to be True
		// for the shindig javascript libraries to load
		return (configuration.getProperty(SHINDIG_URL_PROP) != null
				&& (getVisibleGadgets().size() > 0) || getPageName().equals(
				"/display"));
	}

	public List<PreparedGadget> getVisibleGadgets() {
		return gadgets;
	}

	public void postActivity(int userId, String title) throws SQLException {
		postActivity(userId, title, null, null, null);
	}

	public void postActivity(int userId, String title, String body) throws SQLException {
		postActivity(userId, title, body, null, null);
	}

	public void postActivity(int userId, String title, String body,
			String xtraId1Type, String xtraId1Value) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String sqlCommand = "INSERT INTO orng_activity (userId, activity, xtraId1Type, xtraId1Value) VALUES ('"
				+ userId +  "','<activity xmlns=\"http://ns.opensocial.org/2008/opensocial\"><postedTime>"
				+ System.currentTimeMillis() + "</postedTime><title>" + title + "</title>" 
				+ (body != null ? "<body>" + body + "</body>" : "") + "</activity>','"
				+ xtraId1Type + "','" + xtraId1Value + "');";		
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlCommand);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
			}
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
			
	}

	private String socketSendReceive(String viewer, String owner, String gadget)
			throws IOException {
		// These keys need to match what you see in
		// edu.ucsf.orng.shindig.service.SecureTokenGeneratorService in
		// Shindig
		String[] tokenService = configuration.getProperty(
				"OpenSocial.tokenService").split(":");
		String request = "c=default" + (viewer != null ? "&v=" + URLEncoder.encode(viewer, "UTF-8") : "") + 
				(owner != null ? "&o=" + URLEncoder.encode(owner, "UTF-8") : "") + "&g=" + gadget + "\r\n";

		// Create a socket connection with the specified server and port.
		Socket s = new Socket(tokenService[0],
				Integer.parseInt(tokenService[1]));

		// Send request to the server.
		s.getOutputStream().write(request.getBytes());

		// Receive the encoded content.
		int bytes = 0;
		String page = "";
		byte[] bytesReceived = new byte[256];

		// The following will block until the page is transmitted.
		while ((bytes = s.getInputStream().read(bytesReceived)) > 0) {
			page += new String(bytesReceived, 0, bytes);
		};

		return page;
	}
	
	public String getContainerJavascriptSrc() {
		return configuration.getProperty(SHINDIG_URL_PROP)
				+ "/gadgets/js/core:dynamic-height:osapi:pubsub:rpc:views:rdf:shindig-container.js?c=1"
				+ (isDebug ? "&debug=1" : "");
	}

	public String getGadgetJavascript() {
		String lineSeparator = System.getProperty("line.separator");
		String gadgetScriptText = "var my = {};" + lineSeparator
				+ "my.gadgetSpec = function(appId, name, url, secureToken, view, chrome_id, opt_params, visible_scope) {"
				+ lineSeparator + "this.appId = appId;" + lineSeparator
				+ "this.name = name;" + lineSeparator + "this.url = url;"
				+ lineSeparator + "this.secureToken = secureToken;"
				+ lineSeparator + "this.view = view || 'default';"
				+ lineSeparator + "this.chrome_id = chrome_id;" 
				+ lineSeparator + "this.opt_params = opt_params;" + lineSeparator
				+ "this.visible_scope = visible_scope;" + lineSeparator + "};"
				+ lineSeparator + "my.pubsubData = {};" + lineSeparator;
		for (String key : getPubsubData().keySet()) {
			gadgetScriptText += "my.pubsubData['" + key + "'] = '"
					+ getPubsubData().get(key) + "';" + lineSeparator;
		}
		gadgetScriptText += "my.openSocialURL = '"
				+ configuration.getProperty(SHINDIG_URL_PROP) + "';"
				+ lineSeparator + "my.debug = " + (isDebug() ? "1" : "0") + ";"
				+ lineSeparator + "my.noCache = " + (noCache() ? "1" : "0")
				+ ";" + lineSeparator + "my.gadgets = [";
		for (PreparedGadget gadget : getVisibleGadgets()) {
			gadgetScriptText += "new my.gadgetSpec(" + gadget.getAppId() + ",'"
					+ gadget.getName() + "','" + gadget.getGadgetURL() + "','"
					+ gadget.getSecurityToken() + "','" + gadget.getView()
					+ "','" + gadget.getChromeId() + "'," + gadget.getOptParams() + ",'"
					+ gadget.getGadgetSpec().getVisibleScope() + "'), ";
		}
		gadgetScriptText = gadgetScriptText.substring(0,
				gadgetScriptText.length() - 2)
				+ "];"
				+ lineSeparator;

		return gadgetScriptText;
	}
	
	Map<String, GadgetSpec> getAllDBGadgets(boolean useCache) throws SQLException 
	{
		// great place to add cache
        // check cache first
		Map<String, GadgetSpec> allDBGadgets = useCache ? gadgetCache : null;		
		if (allDBGadgets == null) {
			allDBGadgets = new HashMap<String, GadgetSpec>();
			Connection conn = null;
			Statement stmt = null;
			ResultSet rset = null;
			try {
	
				String sqlCommand = "select appId, name, url, channels, enabled from orng_apps";
	
				conn = dataSource.getConnection();
				stmt = conn.createStatement();
				rset = stmt.executeQuery(sqlCommand);
	
				while (rset.next()) {
					String channelsStr = rset.getString(4);
					List<String> channels = Arrays.asList(channelsStr != null && channelsStr.length() > 0 ? channelsStr.split(" ") : new String[0]);
					GadgetSpec spec = new GadgetSpec(rset.getInt(1),
							rset.getString(2), rset.getString(3), channels, dataSource, rset.getBoolean(5), false);
					String gadgetFileName = getGadgetFileNameFromURL(rset.getString(3));
	
					allDBGadgets.put(gadgetFileName, spec);
				}
			} 
			finally {
				try {
					rset.close();
				} catch (Exception e) {
				}
				try {
					stmt.close();
				} catch (Exception e) {
				}
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
			if (useCache) {
				gadgetCache = allDBGadgets;
			}
		}

		return allDBGadgets;
	}
	
	private List<PreparedGadget> getSandboxGadgets(VitroRequest vreq, Map<String, GadgetSpec> allDBGadgets, String requestAppId) throws SQLException, IOException {
		List<PreparedGadget> sandboxGadgets = new ArrayList<PreparedGadget>();
		// Note that this block of code only gets executed after someone fills in the 
		// gadget/sandbox form!
		String openSocialGadgetURLS = (String) vreq.getSession()
				.getAttribute(OPENSOCIAL_GADGETS);
		String[] urls = openSocialGadgetURLS.split(System.getProperty("line.separator"));
		for (String openSocialGadgetURL : urls) {
			openSocialGadgetURL = openSocialGadgetURL.trim();
			if (openSocialGadgetURL.length() == 0)
				continue;
			int appId = 0; // if URL matches one in the DB, use DB provided
							// appId, otherwise generate one
			String gadgetFileName = getGadgetFileNameFromURL(openSocialGadgetURL);
			String name = gadgetFileName;
			List<String> channels = new ArrayList<String>();
			boolean unknownGadget = true;
			if (allDBGadgets.containsKey(gadgetFileName)) {
				appId = allDBGadgets.get(gadgetFileName).getAppId();
				name = allDBGadgets.get(gadgetFileName).getName();
				channels = allDBGadgets.get(gadgetFileName).getChannels();
				unknownGadget = false;
			} else {
				log.warn("Could not find " + gadgetFileName + " in " + allDBGadgets.keySet());
				appId = Math.abs(openSocialGadgetURL.hashCode());
			}
			// if they asked for a specific one, only let it in
			if (requestAppId != null && Integer.parseInt(requestAppId) != appId) {
				continue;
			}
			GadgetSpec gadget = new GadgetSpec(appId, name,
					openSocialGadgetURL, channels, dataSource, true, unknownGadget);
			// only add ones that are visible in this context!
			int moduleId = 0;
			if (unknownGadget
					|| gadget.show(viewerId, ownerId, pageName, dataSource)) {
				String securityToken = socketSendReceive(viewerId, ownerId,
						"" + gadget.getAppId());
				sandboxGadgets.add(new PreparedGadget(gadget, this, moduleId++,
						securityToken));
			}
		}
		return sandboxGadgets;
		
	}
}
