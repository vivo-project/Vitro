/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.AbstractPageHandler;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * This is not really a page, in that it doesn't display anything. It's just a
 * way to separate out some of the logic of the ManageProxies list page.
 */
public class ManageProxiesEditPage extends AbstractPageHandler {
	private static final Log log = LogFactory
			.getLog(ManageProxiesEditPage.class);

	private static final String PARAMETER_DELETE_PROXY = "deleteProxy";
	private static final String PARAMETER_EDIT_PROFILES = "modifyProfileList";
	private static final String PARAMETER_PROXY_URI = "proxyUri";
	private static final String PARAMETER_PROFILE_URI = "profileUri";

	private enum Function {
		DELETE_PROXY, EDIT_PROFILES, UNKNOWN
	}

	private Function function;
	private List<String> proxyUris;
	private List<UserAccount> proxyAccounts;

	private List<String> profileUris;

	/** The result of checking whether this request is valid. */
	private boolean valid = true;

	protected ManageProxiesEditPage(VitroRequest vreq) {
		super(vreq);
		parseParameters();
	}

	private void parseParameters() {
		proxyUris = getStringParameters(PARAMETER_PROXY_URI);
		profileUris = getStringParameters(PARAMETER_PROFILE_URI);

		try {
			if (isFlagOnRequest(PARAMETER_EDIT_PROFILES)) {
				function = Function.EDIT_PROFILES;
				proxyAccounts = findSingleProxyAccount(PARAMETER_EDIT_PROFILES);
				validateProfileUris();
			} else if (isFlagOnRequest(PARAMETER_DELETE_PROXY)) {
				function = Function.DELETE_PROXY;
				proxyAccounts = findSingleProxyAccount(PARAMETER_EDIT_PROFILES);
			} else {
				function = Function.UNKNOWN;
			}
		} catch (InvalidParametersException e) {
			log.error(e.getMessage());
			valid = false;
		}
	}

	private List<UserAccount> findSingleProxyAccount(String functionParameter)
			throws InvalidParametersException {
		if (proxyUris.isEmpty()) {
			throw new InvalidParametersException("'" + functionParameter
					+ "' was requested, but no '" + PARAMETER_PROXY_URI
					+ "' parameter was found.");
		}

		if (proxyUris.size() > 1) {
			throw new InvalidParametersException("'" + functionParameter
					+ "' was requested, but there were " + proxyUris.size()
					+ "'" + PARAMETER_PROXY_URI + "' parameters.");
		}

		String proxyUri = proxyUris.get(0);
		UserAccount proxy = userAccountsDao.getUserAccountByUri(proxyUri);
		if (proxy == null) {
			throw new InvalidParametersException(
					"Found no User Account for proxyUri='" + proxyUri + "'");
		}

		return Collections.singletonList(proxy);
	}

	private void validateProfileUris() throws InvalidParametersException {
		for (String profileUri : profileUris) {
			Individual ind = indDao.getIndividualByURI(profileUri);
			if (ind == null) {
				throw new InvalidParametersException(
						"Found no Individual for profileUri='" + profileUri
								+ "'");
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public void applyEdits() {
		if (!valid) {
			return;
		}

		if (function == Function.DELETE_PROXY) {
			deleteRelationshipsFromProxy();
		} else if (function == Function.EDIT_PROFILES) {
			editRelationshipsOnProxy();
		}
	}

	private void deleteRelationshipsFromProxy() {
		UserAccount proxyAccount = proxyAccounts.get(0);
		proxyAccount.setProxiedIndividualUris(Collections.<String> emptyList());
		userAccountsDao.updateUserAccount(proxyAccount);
	}

	private void editRelationshipsOnProxy() {
		UserAccount proxyAccount = proxyAccounts.get(0);
		proxyAccount.setProxiedIndividualUris(profileUris);
		userAccountsDao.updateUserAccount(proxyAccount);
	}

}
