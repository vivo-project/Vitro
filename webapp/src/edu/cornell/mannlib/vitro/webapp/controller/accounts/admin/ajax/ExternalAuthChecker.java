/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.ajax;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;

/**
 * What is our reaction to this possible External Auth ID?
 * 
 * Is somebody already using it (other than ourselves)? Does it match an
 * existing Profile? Neither?
 * 
 * If we are creating a new account, the userAccountUri will be empty, so if
 * someone is using the externalAuthID, their URI won't match ours, which is
 * what we want.
 * 
 * If the externalAuthId is empty, or if there is any error, say "neither".
 */
class ExternalAuthChecker extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(ExternalAuthChecker.class);

	private static final String PARAMETER_USER_ACCOUNT_URI = "userAccountUri";
	private static final String PARAMETER_ETERNAL_AUTH_ID = "externalAuthId";
	private static final String RESPONSE_ID_IN_USE = "idInUse";
	private static final String RESPONSE_MATCHES_PROFILE = "matchesProfile";
	private static final String RESPONSE_PROFILE_URI = "profileUri";
	private static final String RESPONSE_PROFILE_URL = "profileUrl";
	private static final String RESPONSE_PROFILE_LABEL = "profileLabel";

	private final String userAccountUri;
	private final String externalAuthId;

	private Individual matchingProfile;

	public ExternalAuthChecker(HttpServlet parent, VitroRequest vreq,
			HttpServletResponse resp) {
		super(parent, vreq, resp);
		userAccountUri = getStringParameter(PARAMETER_USER_ACCOUNT_URI, "");
		externalAuthId = getStringParameter(PARAMETER_ETERNAL_AUTH_ID, "");
	}

	@Override
	public String prepareResponse() throws IOException, JSONException {
		if (someoneElseIsUsingThisExternalAuthId()) {
			return respondExternalAuthIdAlreadyUsed();
		}

		checkForMatchingProfile();
		if (matchingProfile != null) {
			return respondWithMatchingProfile();
		}

		return EMPTY_RESPONSE;
	}

	private boolean someoneElseIsUsingThisExternalAuthId() {
		if (externalAuthId.isEmpty()) {
			log.debug("externalAuthId is empty.");
			return false;
		}

		UserAccount user = uaDao.getUserAccountByExternalAuthId(externalAuthId);
		if (user == null) {
			log.debug("no user account has externalAuthId='" + externalAuthId
					+ "'");
			return false;
		}

		if (user.getUri().equals(userAccountUri)) {
			log.debug("externalAuthId '" + externalAuthId
					+ "' belongs to current user '" + userAccountUri + "'");
			return false;
		}

		log.debug(user.getEmailAddress() + " is already using externalAuthId '"
				+ externalAuthId + "'");
		return true;
	}

	private String respondExternalAuthIdAlreadyUsed() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(RESPONSE_ID_IN_USE, true);
		return jsonObject.toString();
	}

	private void checkForMatchingProfile() {
		List<Individual> inds = SelfEditingConfiguration.getBean(vreq)
				.getAssociatedIndividuals(indDao, externalAuthId);
		if (inds.isEmpty()) {
			log.debug("No profiles match '" + externalAuthId + "'");
			return;
		}

		this.matchingProfile = inds.get(0);
	}

	private String respondWithMatchingProfile() throws JSONException {
		String uri = matchingProfile.getURI();
		String url = UrlBuilder.getIndividualProfileUrl(uri, vreq);
		String label = matchingProfile.getRdfsLabel();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(RESPONSE_MATCHES_PROFILE, true);
		jsonObject.put(RESPONSE_PROFILE_URI, uri);
		jsonObject.put(RESPONSE_PROFILE_URL, url);
		jsonObject.put(RESPONSE_PROFILE_LABEL, label);
		return jsonObject.toString();
	}

}