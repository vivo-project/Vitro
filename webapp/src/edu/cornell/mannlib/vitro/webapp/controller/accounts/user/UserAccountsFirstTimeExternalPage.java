/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Handle the first-time login of an Externally Authenticated user who has no
 * UserAccount - let's create one!
 * 
 * If they get here from the login, there should be an ExternalLoginInfo waiting
 * in the session. Otherwise, they should get here by submitting the form, which
 * will have the info in hidden fields.
 */
public class UserAccountsFirstTimeExternalPage extends UserAccountsPage {
	private static final String PARAMETER_SUBMIT = "submit";
	private static final String PARAMETER_EXTERNAL_AUTH_ID = "externalAuthId";
	private static final String PARAMETER_AFTER_LOGIN_URL = "afterLoginUrl";
	private static final String PARAMETER_EMAIL_ADDRESS = "emailAddress";
	private static final String PARAMETER_FIRST_NAME = "firstName";
	private static final String PARAMETER_LAST_NAME = "lastName";

	private static final String ERROR_NO_EMAIL = "errorEmailIsEmpty";
	private static final String ERROR_EMAIL_IN_USE = "errorEmailInUse";
	private static final String ERROR_EMAIL_INVALID_FORMAT = "errorEmailInvalidFormat";
	private static final String ERROR_NO_FIRST_NAME = "errorFirstNameIsEmpty";
	private static final String ERROR_NO_LAST_NAME = "errorLastNameIsEmpty";

	private static final String TEMPLATE_NAME = "userAccounts-firstTimeExternal.ftl";

	private static final String ATTRIBUTE_EXTERNAL_LOGIN_INFO = UserAccountsFirstTimeExternalPage.class
			.getName();

	/**
	 * Let some other request set the External Auth ID and the afterLogin URL
	 * before redirecting to here.
	 */
	public static void setExternalLoginInfo(HttpServletRequest req,
			String externalAuthId, String afterLoginUrl) {
		req.getSession().setAttribute(ATTRIBUTE_EXTERNAL_LOGIN_INFO,
				new ExternalLoginInfo(externalAuthId, afterLoginUrl));
	}

	private final UserAccountsFirstTimeExternalPageStrategy strategy;

	private String externalAuthId = "";
	private String afterLoginUrl = "";

	private boolean submit = false;
	private String emailAddress = "";
	private String firstName = "";
	private String lastName = "";

	private String errorCode = "";
	private String bogusMessage = "";

	protected UserAccountsFirstTimeExternalPage(VitroRequest vreq) {
		super(vreq);

		this.strategy = UserAccountsFirstTimeExternalPageStrategy.getInstance(
				vreq, this, isEmailEnabled());

		checkSessionForExternalLoginInfo();
		if (externalAuthId.isEmpty()) {
			parseRequestParameters();
		}

		validateExternalAuthId();

		if (isSubmit() && !isBogus()) {
			validateParameters();
		}
	}

	private void checkSessionForExternalLoginInfo() {
		HttpSession session = vreq.getSession();

		Object o = session.getAttribute(ATTRIBUTE_EXTERNAL_LOGIN_INFO);
		session.removeAttribute(ATTRIBUTE_EXTERNAL_LOGIN_INFO);

		if (o instanceof ExternalLoginInfo) {
			externalAuthId = ((ExternalLoginInfo) o).externalAuthId;
			afterLoginUrl = ((ExternalLoginInfo) o).afterLoginUrl;
			if (afterLoginUrl == null) {
				afterLoginUrl = "";
			}
		}
	}

	private void parseRequestParameters() {
		externalAuthId = getStringParameter(PARAMETER_EXTERNAL_AUTH_ID, "");
		afterLoginUrl = getStringParameter(PARAMETER_AFTER_LOGIN_URL, "");

		submit = isFlagOnRequest(PARAMETER_SUBMIT);
		emailAddress = getStringParameter(PARAMETER_EMAIL_ADDRESS, "");
		firstName = getStringParameter(PARAMETER_FIRST_NAME, "");
		lastName = getStringParameter(PARAMETER_LAST_NAME, "");
	}

	private void validateExternalAuthId() {
		if (externalAuthId.isEmpty()) {
			bogusMessage = "Login failed - External ID is not found.";
			return;
		}
		if (null != userAccountsDao
				.getUserAccountByExternalAuthId(externalAuthId)) {
			bogusMessage = "User account already exists for '" + externalAuthId
					+ "'";
			return;
		}
	}

	public boolean isBogus() {
		return !bogusMessage.isEmpty();
	}

	public String getBogusMessage() {
		return bogusMessage;
	}

	public boolean isSubmit() {
		return submit;
	}

	private void validateParameters() {
		if (firstName.isEmpty()) {
			errorCode = ERROR_NO_FIRST_NAME;
		} else if (lastName.isEmpty()) {
			errorCode = ERROR_NO_LAST_NAME;
		} else if (emailAddress.isEmpty()) {
			errorCode = ERROR_NO_EMAIL;
		} else if (isEmailInUse()) {
			errorCode = ERROR_EMAIL_IN_USE;
		} else if (!isEmailValidFormat()) {
			errorCode = ERROR_EMAIL_INVALID_FORMAT;
		}
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

	public final ResponseValues showPage() {
		Map<String, Object> body = new HashMap<String, Object>();

		body.put("externalAuthId", externalAuthId);
		body.put("afterLoginUrl", afterLoginUrl);

		body.put("emailAddress", emailAddress);
		body.put("firstName", firstName);
		body.put("lastName", lastName);
		body.put("formUrls", buildUrlsMap());

		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}

		strategy.addMoreBodyValues(body);

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	public UserAccount createAccount() {
		UserAccount u = new UserAccount();
		u.setEmailAddress(emailAddress);
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setExternalAuthId(externalAuthId);
		u.setPasswordChangeRequired(false);
		u.setPasswordLinkExpires(0);
		u.setExternalAuthOnly(true);
		u.setLoginCount(0);
		u.setStatus(Status.ACTIVE);
		u.setPermissionSetUris(Collections
				.singleton(PermissionSetsLoader.URI_SELF_EDITOR));

		userAccountsDao.insertUserAccount(u);

		strategy.notifyUser(u);

		return u;
	}

	/**
	 * If the afterLoginUrl is missing, go to the home page. If it is relative,
	 * make sure it doesn't start with the cotext path.
	 */
	public String getAfterLoginUrl() {
		if (StringUtils.isEmpty(afterLoginUrl)) {
			return null;
		}
		return afterLoginUrl;
	}

	private static class ExternalLoginInfo {
		final String externalAuthId;
		final String afterLoginUrl;

		public ExternalLoginInfo(String externalAuthId, String afterLoginUrl) {
			this.externalAuthId = externalAuthId;
			this.afterLoginUrl = afterLoginUrl;
		}
	}

}
