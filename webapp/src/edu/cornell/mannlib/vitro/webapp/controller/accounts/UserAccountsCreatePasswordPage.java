/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.user.UserAccountsUserController;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * TODO
 */
public class UserAccountsCreatePasswordPage extends UserAccountsPage {
	private static final Log log = LogFactory
			.getLog(UserAccountsCreatePasswordPage.class);

	private static final String PARAMETER_SUBMIT = "submitCreatePassword";
	private static final String PARAMETER_USER = "user";
	private static final String PARAMETER_KEY = "key";
	private static final String PARAMETER_PASSWORD = "password";
	private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

	private static final String TEMPLATE_NAME = "userAccounts-createPassword.ftl";

	private static final String ERROR_NO_PASSWORD = "errorPasswordIsEmpty";
	private static final String ERROR_WRONG_PASSWORD_LENGTH = "errorPasswordIsWrongLength";
	private static final String ERROR_PASSWORDS_DONT_MATCH = "errorPasswordsDontMatch";

	private boolean submit;
	private String userEmail = "";
	private String key = "";
	private String password = "";
	private String confirmPassword = "";

	private UserAccount userAccount;

	/** The result of checking whether this request is even appropriate. */
	private String bogusMessage = "";

	/** The result of validating a "submit" request. */
	private String errorCode = "";

	public UserAccountsCreatePasswordPage(VitroRequest vreq) {
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
		password = getStringParameter(PARAMETER_PASSWORD, "");
		confirmPassword = getStringParameter(PARAMETER_CONFIRM_PASSWORD, "");
	}

	private void validateUserAccountInfo() {
		userAccount = userAccountsDao.getUserAccountByEmail(userEmail);
		if (userAccount == null) {
			log.warn("Create password for '" + userEmail
					+ "' is bogus: no such user");
			bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
			return;
		}

		if (userAccount.getPasswordLinkExpires() == 0L) {
			log.warn("Create password for '" + userEmail
					+ "' is bogus: password change is not pending.");
			bogusMessage = "The account for " + userEmail
					+ " has already been activated.";
			return;
		}

		Date expirationDate = new Date(userAccount.getPasswordLinkExpires());
		if (expirationDate.before(new Date())) {
			log.warn("Create password for '" + userEmail
					+ "' is bogus: expiration date has passed.");
			bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
			return;
		}

		String expectedKey = userAccount.getPasswordLinkExpiresHash();
		if (!key.equals(expectedKey)) {
			log.warn("Create password for '" + userEmail + "' is bogus: key ("
					+ key + ") doesn't match expected key (" + expectedKey
					+ ")");
			bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
			return;
		}
	}

	private void validateParameters() {
		if (password.isEmpty()) {
			errorCode = ERROR_NO_PASSWORD;
		} else if (!checkPasswordLength(password)) {
			errorCode = ERROR_WRONG_PASSWORD_LENGTH;
		} else if (!password.equals(confirmPassword)) {
			errorCode = ERROR_PASSWORDS_DONT_MATCH;
		}
	}

	private boolean checkPasswordLength(String pw) {
		return pw.length() >= UserAccount.MIN_PASSWORD_LENGTH
				&& pw.length() <= UserAccount.MAX_PASSWORD_LENGTH;
	}

	public boolean isBogus() {
		return bogusMessage.isEmpty();
	}

	public String getBogusMessage() {
		return bogusMessage;
	}

	public boolean isSubmit() {
		return submit;
	}

	public boolean isValid() {
		return errorCode.isEmpty();
	}

	public void createPassword() {
		userAccount.setMd5Password(Authenticator.applyMd5Encoding(password));
		userAccount.setPasswordLinkExpires(0L);
		userAccount.setStatus(Status.ACTIVE);
		userAccountsDao.updateUserAccount(userAccount);
		log.debug("Set password on '" + userAccount.getEmailAddress()
				+ "' to '" + password + "'");
	}

	public final ResponseValues showPage() {
		Map<String, Object> body = new HashMap<String, Object>();

		body.put("minimumLength", UserAccount.MIN_PASSWORD_LENGTH);
		body.put("maximumLength", UserAccount.MAX_PASSWORD_LENGTH);
		body.put("userAccount", userAccount);
		body.put("key", userAccount.getPasswordLinkExpiresHash());
		body.put("password", password);
		body.put("confirmPassword", confirmPassword);
		body.put("formUrls", buildUrlsMap());

		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

}
