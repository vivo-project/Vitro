/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Routines in common to the "Create Password" and "Reset Password" pages.
 */
public abstract class UserAccountsPasswordBasePage extends UserAccountsPage {
	private static final Log log = LogFactory
			.getLog(UserAccountsPasswordBasePage.class);

	public static final String BOGUS_MESSAGE_NO_SUCH_ACCOUNT = "The account you are trying to set a password on is no longer available. Please contact your system administrator if you think this is an error.";

	private static final String PARAMETER_SUBMIT = "submit";
	private static final String PARAMETER_USER = "user";
	private static final String PARAMETER_KEY = "key";
	private static final String PARAMETER_NEW_PASSWORD = "newPassword";
	private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

	private static final String ERROR_NO_PASSWORD = "errorPasswordIsEmpty";
	private static final String ERROR_WRONG_PASSWORD_LENGTH = "errorPasswordIsWrongLength";
	private static final String ERROR_PASSWORDS_DONT_MATCH = "errorPasswordsDontMatch";

	protected boolean submit;
	protected String userEmail = "";
	protected String key = "";
	protected String newPassword = "";
	protected String confirmPassword = "";

	protected UserAccount userAccount;

	/** The result of checking whether this request is even appropriate. */
	private String bogusMessage = "";

	/** The result of validating a "submit" request. */
	private String errorCode = "";

	private boolean loggedIn;

	protected UserAccountsPasswordBasePage(VitroRequest vreq) {
		super(vreq);

		parseRequestParameters();
		validateUserAccountInfo();

		if (isSubmit() && !isBogus()) {
			validateParameters();
		}
	}

	private void parseRequestParameters() {
		submit = isFlagOnRequest(PARAMETER_SUBMIT);
		userEmail = getStringParameter(PARAMETER_USER, "");
		key = getStringParameter(PARAMETER_KEY, "");
		newPassword = getStringParameter(PARAMETER_NEW_PASSWORD, "");
		confirmPassword = getStringParameter(PARAMETER_CONFIRM_PASSWORD, "");
	}

	public boolean isSubmit() {
		return submit;
	}

	private void validateUserAccountInfo() {
		userAccount = userAccountsDao.getUserAccountByEmail(userEmail);
		if (userAccount == null) {
			log.warn("Password request for '" + userEmail
					+ "' is bogus: no such user");
			bogusMessage = BOGUS_MESSAGE_NO_SUCH_ACCOUNT;
			return;
		}

		if (userAccount.getPasswordLinkExpires() == 0L) {
			log.info("Password request for '" + userEmail
					+ "' is bogus: password change is not pending.");
			bogusMessage = passwordChangeNotPendingMessage();
			return;
		}

		if (userAccount.isExternalAuthOnly()) {
			log.info("Password request for '" + userEmail
					+ "' is bogus: account is external auth only.");
			bogusMessage = passwordChangeNotPendingMessage();
			return;
		}

		Date expirationDate = new Date(userAccount.getPasswordLinkExpires());
		if (expirationDate.before(new Date())) {
			log.info("Password request for '" + userEmail
					+ "' is bogus: expiration date has passed.");
			bogusMessage = passwordChangeNotPendingMessage();
			return;
		}

		String expectedKey = userAccount.getPasswordLinkExpiresHash();
		if (!key.equals(expectedKey)) {
			log.warn("Password request for '" + userEmail + "' is bogus: key ("
					+ key + ") doesn't match expected key (" + expectedKey
					+ ")");
			bogusMessage = passwordChangeNotPendingMessage();
			return;
		}

		UserAccount currentUser = LoginStatusBean.getCurrentUser(vreq);
		if (currentUser != null) {
			loggedIn = true;
			String currentUserEmail = currentUser.getEmailAddress();
			if (!userEmail.equals(currentUserEmail)) {
				log.info("Password request for '" + userEmail
						+ "' when already logged in as '" + currentUserEmail
						+ "'");
				bogusMessage = alreadyLoggedInMessage(currentUserEmail);
				return;
			}
		}
	}

	public boolean isBogus() {
		return !bogusMessage.isEmpty();
	}

	public String getBogusMessage() {
		return bogusMessage;
	}

	private void validateParameters() {
		if (newPassword.isEmpty()) {
			errorCode = ERROR_NO_PASSWORD;
		} else if (!checkPasswordLength(newPassword)) {
			errorCode = ERROR_WRONG_PASSWORD_LENGTH;
		} else if (!newPassword.equals(confirmPassword)) {
			errorCode = ERROR_PASSWORDS_DONT_MATCH;
		}
	}

	public boolean isValid() {
		return errorCode.isEmpty();
	}

	public final ResponseValues showPage() {
		Map<String, Object> body = new HashMap<String, Object>();

		body.put("minimumLength", UserAccount.MIN_PASSWORD_LENGTH);
		body.put("maximumLength", UserAccount.MAX_PASSWORD_LENGTH);
		body.put("userAccount", userAccount);
		body.put("key", userAccount.getPasswordLinkExpiresHash());
		body.put("newPassword", newPassword);
		body.put("confirmPassword", confirmPassword);
		body.put("formUrls", buildUrlsMap());

		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}

		return new TemplateResponseValues(templateName(), body);
	}

	public String getSuccessMessage() {
		if (loggedIn) {
			return "Your password has been saved.";
		} else {
			return "Your password has been saved. Please log in.";
		}
	}

	protected abstract String alreadyLoggedInMessage(String currentUserEmail);

	protected abstract String passwordChangeNotPendingMessage();

	protected abstract String templateName();
}
