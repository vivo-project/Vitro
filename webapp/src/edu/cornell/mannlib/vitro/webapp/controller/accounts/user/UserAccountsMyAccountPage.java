/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.UserAccountsEditPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
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

	private static final String ERROR_NO_EMAIL = "errorEmailIsEmpty";
	private static final String ERROR_EMAIL_IN_USE = "errorEmailInUse";
	private static final String ERROR_EMAIL_INVALID_FORMAT = "errorEmailInvalidFormat";
	private static final String ERROR_NO_FIRST_NAME = "errorFirstNameIsEmpty";
	private static final String ERROR_NO_LAST_NAME = "errorLastNameIsEmpty";

	private static final String TEMPLATE_NAME = "userAccounts-myAccount.ftl";

	private final UserAccountsMyAccountPageStrategy strategy;

	private final UserAccount userAccount;

	/* The request parameters */
	private boolean submit;
	private String emailAddress = "";
	private String firstName = "";
	private String lastName = "";

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
		} else {
			body.put("emailAddress", userAccount.getEmailAddress());
			body.put("firstName", userAccount.getFirstName());
			body.put("lastName", userAccount.getLastName());
		}
		body.put("formUrls", buildUrlsMap());

		if (userAccount.isExternalAuthOnly()) {
			body.put("externalAuthOnly", Boolean.TRUE);
		}
		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}
		if (!confirmationCode.isEmpty()) {
			body.put(confirmationCode, Boolean.TRUE);
		}

		strategy.addMoreBodyValues(body);

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	public void updateAccount() {
		userAccount.setEmailAddress(emailAddress);
		userAccount.setFirstName(firstName);
		userAccount.setLastName(lastName);

		strategy.setAdditionalProperties(userAccount);

		userAccountsDao.updateUserAccount(userAccount);

		strategy.notifyUser();
		confirmationCode = strategy.getConfirmationCode();
	}

	boolean isExternalAuthOnly() {
		return (userAccount != null) && userAccount.isExternalAuthOnly();
	}
}
