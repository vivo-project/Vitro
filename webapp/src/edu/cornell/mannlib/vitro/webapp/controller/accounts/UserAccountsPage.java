/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;

/**
 * Common routines for the page controllers.
 */
public abstract class UserAccountsPage {
	private static final Log log = LogFactory.getLog(UserAccountsPage.class);

	protected final VitroRequest vreq;
	protected final ServletContext ctx;
	protected final OntModel userAccountsModel;
	protected final UserAccountsDao userAccountsDao;

	protected UserAccountsPage(VitroRequest vreq) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();

		OntModelSelector oms = (OntModelSelector) this.ctx
				.getAttribute("baseOntModelSelector");
		userAccountsModel = oms.getUserAccountsModel();

		WebappDaoFactory wdf = (WebappDaoFactory) this.ctx
				.getAttribute("webappDaoFactory");
		userAccountsDao = wdf.getUserAccountsDao();
	}

	protected static boolean isEmailEnabled(HttpServletRequest req) {
		return FreemarkerEmailFactory.isConfigured(req);
	}

	protected String getStringParameter(String key, String defaultValue) {
		String value = vreq.getParameter(key);
		return (value == null) ? defaultValue : value;
	}

	protected int getIntegerParameter(String key, int defaultValue) {
		String value = vreq.getParameter(key);
		if (value == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			log.warn("Invalid integer for parameter '" + key + "': " + value);
			return defaultValue;
		}
	}

	/**
	 * Check for the presence of a parameter, regardless of its value, even if
	 * it's an empty string.
	 */
	protected boolean isFlagOnRequest(String key) {
		String value = vreq.getParameter(key);
		return (value != null);
	}

	/**
	 * Create a list of all known PermissionSets.
	 */
	protected List<PermissionSet> buildRolesList() {
		List<PermissionSet> list = new ArrayList<PermissionSet>();
		list.addAll(userAccountsDao.getAllPermissionSets());
		Collections.sort(list, new Comparator<PermissionSet>() {
			@Override
			public int compare(PermissionSet ps1, PermissionSet ps2) {
				return ps1.getUri().compareTo(ps2.getUri());
			}
		});
		return list;
	}

	/**
	 * Make these URLs available to all of the pages.
	 */
	protected Map<String, String> buildUrlsMap() {
		Map<String, String> map = new HashMap<String, String>();

		map.put("list", UrlBuilder.getUrl("/accountsAdmin/list"));
		map.put("add", UrlBuilder.getUrl("/accountsAdmin/add"));
		map.put("delete", UrlBuilder.getUrl("/accountsAdmin/delete"));
		map.put("createPassword", UrlBuilder.getUrl("/accounts/createPassword"));

		return map;
	}

	protected static String editAccountUrl(String uri) {
		return UrlBuilder.getUrl("/accountsAdmin/edit",
				new ParamMap("editAccount", uri));
	}
}
