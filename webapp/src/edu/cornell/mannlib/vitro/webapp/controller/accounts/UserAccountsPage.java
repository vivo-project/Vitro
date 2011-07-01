/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;

/**
 * Common routines for the page controllers.
 */
public abstract class UserAccountsPage {
	private static final Log log = LogFactory.getLog(UserAccountsPage.class);

	private static final String PERSON_CLASS_URI = "http://xmlns.com/foaf/0.1/Person";

	/**
	 * After the account is created, or the password is reset, the user has this
	 * many days to repond to the email.
	 */
	protected static final int DAYS_TO_USE_PASSWORD_LINK = 90;

	protected final VitroRequest vreq;
	protected final ServletContext ctx;
	protected final OntModel userAccountsModel;
	protected final UserAccountsDao userAccountsDao;
	protected final VClassDao vclassDao;
	protected final IndividualDao indDao;
	protected final DataPropertyStatementDao dpsDao;

	protected UserAccountsPage(VitroRequest vreq) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();

		OntModelSelector oms = (OntModelSelector) this.ctx
				.getAttribute("baseOntModelSelector");
		userAccountsModel = oms.getUserAccountsModel();

		WebappDaoFactory wdf = (WebappDaoFactory) this.ctx
				.getAttribute("webappDaoFactory");
		userAccountsDao = wdf.getUserAccountsDao();
		vclassDao = wdf.getVClassDao();
		indDao = wdf.getIndividualDao();
		dpsDao = wdf.getDataPropertyStatementDao();
	}

	protected boolean isEmailEnabled() {
		return FreemarkerEmailFactory.isConfigured(vreq);
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
	 * Treat the presence of a certain parameter, with a desired value, as a
	 * boolean flag.
	 * 
	 * An example would be radio buttons with values of "yes" and "no". The
	 * expected value would be "yes".
	 */
	protected boolean isParameterAsExpected(String key, String expected) {
		return expected.equals(getStringParameter(key, ""));
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
	 * Create a list of possible profile types.
	 * 
	 * TODO Right now, these are foaf:Person and it's sub-classes. What will it
	 * be for Vitro?
	 */
	protected SortedMap<String, String> buildProfileTypesList() {
		String seedClassUri = PERSON_CLASS_URI;
		List<String> classUris = vclassDao.getAllSubClassURIs(seedClassUri);
		classUris.add(seedClassUri);
		
		SortedMap<String, String> types = new TreeMap<String, String>();
		for (String classUri: classUris) {
			VClass vclass = vclassDao.getVClassByURI(classUri);
			if (vclass != null) {
				types.put(classUri, vclass.getName());
			}
		}
		return types;
	}

	/**
	 * Make these URLs available to all of the pages.
	 */
	protected Map<String, String> buildUrlsMap() {
		Map<String, String> map = new HashMap<String, String>();

		map.put("list", UrlBuilder.getUrl("/accountsAdmin/list"));
		map.put("add", UrlBuilder.getUrl("/accountsAdmin/add"));
		map.put("delete", UrlBuilder.getUrl("/accountsAdmin/delete"));
		map.put("myAccount", UrlBuilder.getUrl("/accounts/myAccount"));
		map.put("createPassword", UrlBuilder.getUrl("/accounts/createPassword"));
		map.put("resetPassword", UrlBuilder.getUrl("/accounts/resetPassword"));
		map.put("firstTimeExternal",
				UrlBuilder.getUrl("/accounts/firstTimeExternal"));
		map.put("accountsAjax", UrlBuilder.getUrl("/accountsAjax"));

		return map;
	}

	protected static String editAccountUrl(String uri) {
		return UrlBuilder.getUrl("/accountsAdmin/edit", new ParamMap(
				"editAccount", uri));
	}

	protected Date figureExpirationDate() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, DAYS_TO_USE_PASSWORD_LINK);
		return c.getTime();
	}

	protected boolean checkPasswordLength(String pw) {
		return pw.length() >= UserAccount.MIN_PASSWORD_LENGTH
				&& pw.length() <= UserAccount.MAX_PASSWORD_LENGTH;
	}

}
