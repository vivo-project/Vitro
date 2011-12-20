/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageOwnProxies;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.UserAccountsEditPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Handle the "My Account" form display and submission.
 */
public class UserAccountsMyAccountPage extends UserAccountsPage {
	private static final Log log = LogFactory
			.getLog(UserAccountsEditPage.class);

	private static final String PARAMETER_SUBMIT = "submitMyAccount";
	private static final String PARAMETER_EMAIL_ADDRESS = "emailAddress";
	private static final String PARAMETER_FIRST_NAME = "firstName";
	private static final String PARAMETER_LAST_NAME = "lastName";
	private static final String PARAMETER_PROXY_URI = "proxyUri";

	private static final String ERROR_NO_EMAIL = "errorEmailIsEmpty";
	private static final String ERROR_EMAIL_IN_USE = "errorEmailInUse";
	private static final String ERROR_EMAIL_INVALID_FORMAT = "errorEmailInvalidFormat";
	private static final String ERROR_NO_FIRST_NAME = "errorFirstNameIsEmpty";
	private static final String ERROR_NO_LAST_NAME = "errorLastNameIsEmpty";

	private static final String TEMPLATE_NAME = "userAccounts-myAccount.ftl";

	private static final String PROPERTY_PROFILE_TYPES = "proxy.eligibleTypeList";

	private final UserAccountsMyAccountPageStrategy strategy;

	private final UserAccount userAccount;

	/* The request parameters */
	private boolean submit;
	private String emailAddress = "";
	private String firstName = "";
	private String lastName = "";
	private List<String> proxyUris = new ArrayList<String>();

	/** The result of validating a "submit" request. */
	private String errorCode = "";

	/** The result of updating the account. */
	private String confirmationCode = "";

	public UserAccountsMyAccountPage(VitroRequest vreq) {
		super(vreq);

		this.userAccount = LoginStatusBean.getCurrentUser(vreq);
		this.strategy = UserAccountsMyAccountPageStrategy.getInstance(vreq,
				this, isExternalAccount());

		parseRequestParameters();

		if (isSubmit()) {
			validateParameters();
		}
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	private void parseRequestParameters() {
		submit = isFlagOnRequest(PARAMETER_SUBMIT);
		emailAddress = getStringParameter(PARAMETER_EMAIL_ADDRESS, "");
		firstName = getStringParameter(PARAMETER_FIRST_NAME, "");
		lastName = getStringParameter(PARAMETER_LAST_NAME, "");
		proxyUris = getStringParameters(PARAMETER_PROXY_URI);

		strategy.parseAdditionalParameters();
	}

	public boolean isSubmit() {
		return submit;
	}

	private void validateParameters() {
		if (emailAddress.isEmpty()) {
			errorCode = ERROR_NO_EMAIL;
		} else if (emailIsChanged() && isEmailInUse()) {
			errorCode = ERROR_EMAIL_IN_USE;
		} else if (!isEmailValidFormat()) {
			errorCode = ERROR_EMAIL_INVALID_FORMAT;
		} else if (firstName.isEmpty()) {
			errorCode = ERROR_NO_FIRST_NAME;
		} else if (lastName.isEmpty()) {
			errorCode = ERROR_NO_LAST_NAME;
		} else {
			errorCode = strategy.additionalValidations();
		}
	}

	private boolean emailIsChanged() {
		return !emailAddress.equals(userAccount.getEmailAddress());
	}

	private boolean isEmailInUse() {
		return userAccountsDao.getUserAccountByEmail(emailAddress) != null;
	}

	private boolean isEmailValidFormat() {
		return Authenticator.isValidEmailAddress(emailAddress);
	}

	public boolean isValid() {
		return errorCode.isEmpty();
	}

	private boolean isExternalAccount() {
		return LoginStatusBean.getBean(vreq).hasExternalAuthentication();
	}

	public final ResponseValues showPage() {
		Map<String, Object> body = new HashMap<String, Object>();

		if (isSubmit()) {
			body.put("emailAddress", emailAddress);
			body.put("firstName", firstName);
			body.put("lastName", lastName);
			body.put("proxies", buildOngoingProxyList());
		} else {
			body.put("emailAddress", userAccount.getEmailAddress());
			body.put("firstName", userAccount.getFirstName());
			body.put("lastName", userAccount.getLastName());
			body.put("proxies", buildOriginalProxyList());
		}
		body.put("formUrls", buildUrlsMap());
		body.put("myAccountUri", userAccount.getUri());
		body.put("profileTypes", buildProfileTypesString());

		// Could I do this without exposing this mechanism? But how to search
		// for an associated profile in AJAX?
		body.put("matchingProperty", SelfEditingConfiguration.getBean(vreq)
				.getMatchingPropertyUri());

		if (userAccount.isExternalAuthOnly()) {
			body.put("externalAuthOnly", Boolean.TRUE);
		}
		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}
		if (!confirmationCode.isEmpty()) {
			body.put(confirmationCode, Boolean.TRUE);
		}
		if (isProxyPanelAuthorized()) {
			body.put("showProxyPanel", Boolean.TRUE);
		}

		strategy.addMoreBodyValues(body);

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	private String buildProfileTypesString() {
		return ConfigurationProperties.getBean(vreq).getProperty(
				PROPERTY_PROFILE_TYPES, "http://www.w3.org/2002/07/owl#Thing");
	}

	public void updateAccount() {
		userAccount.setEmailAddress(emailAddress);
		userAccount.setFirstName(firstName);
		userAccount.setLastName(lastName);

		Individual profilePage = getProfilePage(userAccount);
		if (profilePage != null) {
			userAccountsDao.setProxyAccountsOnProfile(profilePage.getURI(),
					proxyUris);
		}

		strategy.setAdditionalProperties(userAccount);

		userAccountsDao.updateUserAccount(userAccount);

		strategy.notifyUser();
		confirmationCode = strategy.getConfirmationCode();
	}

	boolean isProxyPanelAuthorized() {
		return PolicyHelper
				.isAuthorizedForActions(vreq, new ManageOwnProxies())
				&& (getProfilePage(userAccount) != null);
	}

	boolean isExternalAuthOnly() {
		return (userAccount != null) && userAccount.isExternalAuthOnly();
	}

	private List<ProxyInfo> buildOngoingProxyList() {
		List<UserAccount> proxyUsers = new ArrayList<UserAccount>();
		for (String proxyUri : proxyUris) {
			UserAccount proxyUser = userAccountsDao
					.getUserAccountByUri(proxyUri);
			if (proxyUser == null) {
				log.warn("No UserAccount found for proxyUri: " + proxyUri);
			} else {
				proxyUsers.add(proxyUser);
			}
		}

		return buildProxyListFromUserAccounts(proxyUsers);
	}

	private List<ProxyInfo> buildOriginalProxyList() {
		Collection<UserAccount> proxyUsers;

		Individual profilePage = getProfilePage(userAccount);
		if (profilePage == null) {
			log.debug("no profile page");
			proxyUsers = Collections.emptyList();
		} else {
			String uri = profilePage.getURI();
			log.debug("profile page at " + uri);
			proxyUsers = userAccountsDao.getUserAccountsWhoProxyForPage(uri);
			if (log.isDebugEnabled()) {
				log.debug(getUrisFromUserAccounts(proxyUsers));
			}
		}

		return buildProxyListFromUserAccounts(proxyUsers);
	}

	private Individual getProfilePage(UserAccount ua) {
		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(vreq);
		List<Individual> profilePages = sec
				.getAssociatedIndividuals(indDao, ua);
		if (profilePages.isEmpty()) {
			return null;
		} else {
			return profilePages.get(0);
		}
	}

	private List<ProxyInfo> buildProxyListFromUserAccounts(
			Collection<UserAccount> proxyUsers) {
		List<ProxyInfo> proxyInfos = new ArrayList<ProxyInfo>();
		for (UserAccount proxyUser : proxyUsers) {
			proxyInfos.add(assembleProxyInfoForUser(proxyUser));
		}
		return proxyInfos;
	}

	private ProxyInfo assembleProxyInfoForUser(UserAccount proxyUser) {
		String userUri = proxyUser.getUri();
		String label = assembleUserAccountLabel(proxyUser);
		String classLabel = "";
		String imageUrl = "";

		// Does this user have a profile? Can we get better info?
		Individual proxyProfilePage = getProfilePage(proxyUser);
		if (proxyProfilePage != null) {
			String thumbUrl = proxyProfilePage.getThumbUrl();
			if ((thumbUrl != null) && (!thumbUrl.isEmpty())) {
				imageUrl = UrlBuilder.getUrl(thumbUrl);
			}
			classLabel = getMostSpecificTypeLabel(proxyProfilePage.getURI());
		}
		return new ProxyInfo(userUri, label, classLabel, imageUrl);
	}

	private String assembleUserAccountLabel(UserAccount ua) {
		String last = ua.getLastName();
		String first = ua.getFirstName();
		if (last.isEmpty()) {
			return first;
		} else if (first.isEmpty()) {
			return last;
		} else {
			return last + ", " + first;
		}
	}

	private String getMostSpecificTypeLabel(String uri) {
		Map<String, String> types = opsDao
				.getMostSpecificTypesInClassgroupsForIndividual(uri);
		if (types.isEmpty()) {
			return "";
		} else {
			return types.values().iterator().next();
		}
	}

	private List<String> getUrisFromUserAccounts(
			Collection<UserAccount> proxyUsers) {
		List<String> list = new ArrayList<String>();
		for (UserAccount u : proxyUsers) {
			list.add(u.getUri());
		}
		return list;
	}

	public static class ProxyInfo {
		private final String uri;
		private final String label;
		private final String classLabel;
		private final String imageUrl;

		public ProxyInfo(String uri, String label, String classLabel,
				String imageUrl) {
			this.uri = uri;
			this.label = label;
			this.classLabel = classLabel;
			this.imageUrl = imageUrl;
		}

		public String getUri() {
			return uri;
		}

		public String getLabel() {
			return label;
		}

		public String getClassLabel() {
			return classLabel;
		}

		public String getImageUrl() {
			return imageUrl;
		}
	}
}
