/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.user.UserAccountsUserController;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Handle the "Edit Account" form display and submission.
 */
public class UserAccountsEditPage extends UserAccountsPage {
	private static final Log log = LogFactory
			.getLog(UserAccountsEditPage.class);

	private static final String PARAMETER_SUBMIT = "submitEdit";
	private static final String PARAMETER_USER_URI = "editAccount";
	private static final String PARAMETER_EMAIL_ADDRESS = "emailAddress";
	private static final String PARAMETER_EXTERNAL_AUTH_ID = "externalAuthId";
	private static final String PARAMETER_FIRST_NAME = "firstName";
	private static final String PARAMETER_LAST_NAME = "lastName";
	private static final String PARAMETER_ROLE = "role";
	private static final String PARAMETER_ASSOCIATE_WITH_PROFILE = "associate";

	private static final String ERROR_NO_EMAIL = "errorEmailIsEmpty";
	private static final String ERROR_EMAIL_IN_USE = "errorEmailInUse";
	private static final String ERROR_EMAIL_INVALID_FORMAT = "errorEmailInvalidFormat";
	private static final String ERROR_EXTERNAL_AUTH_ID_IN_USE = "errorExternalAuthIdInUse";
	private static final String ERROR_NO_FIRST_NAME = "errorFirstNameIsEmpty";
	private static final String ERROR_NO_LAST_NAME = "errorLastNameIsEmpty";
	private static final String ERROR_NO_ROLE = "errorNoRoleSelected";

	private static final String TEMPLATE_NAME = "userAccounts-edit.ftl";

	private final UserAccountsEditPageStrategy strategy;

	/* The request parameters */
	private boolean submit;
	private String userUri = "";
	private String emailAddress = "";
	private String externalAuthId = "";
	private String firstName = "";
	private String lastName = "";
	private String selectedRoleUri = "";
	private boolean associateWithProfile;

	private UserAccount userAccount;

	/** The result of checking whether this request is even appropriate. */
	private String bogusMessage = "";

	/** The result of validating a "submit" request. */
	private String errorCode = "";

	public UserAccountsEditPage(VitroRequest vreq) {
		super(vreq);

		this.strategy = UserAccountsEditPageStrategy.getInstance(vreq, this,
				isEmailEnabled());

		parseRequestParameters();
		validateUserAccountInfo();

		if (isSubmit() && !isBogus()) {
			validateParameters();
		}
	}

	private void parseRequestParameters() {
		submit = isFlagOnRequest(PARAMETER_SUBMIT);
		userUri = getStringParameter(PARAMETER_USER_URI, "");
		emailAddress = getStringParameter(PARAMETER_EMAIL_ADDRESS, "");
		externalAuthId = getStringParameter(PARAMETER_EXTERNAL_AUTH_ID, "");
		firstName = getStringParameter(PARAMETER_FIRST_NAME, "");
		lastName = getStringParameter(PARAMETER_LAST_NAME, "");
		selectedRoleUri = getStringParameter(PARAMETER_ROLE, "");
		associateWithProfile = isParameterAsExpected(
				PARAMETER_ASSOCIATE_WITH_PROFILE, "yes");

		strategy.parseAdditionalParameters();
	}

	private void validateUserAccountInfo() {
		userAccount = userAccountsDao.getUserAccountByUri(userUri);
		if (userAccount == null) {
			log.warn("Edit account for '" + userUri
					+ "' is bogus: no such user");
			bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
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
		if (emailAddress.isEmpty()) {
			errorCode = ERROR_NO_EMAIL;
		} else if (emailIsChanged() && isEmailInUse()) {
			errorCode = ERROR_EMAIL_IN_USE;
		} else if (!isEmailValidFormat()) {
			errorCode = ERROR_EMAIL_INVALID_FORMAT;
		} else if (externalAuthIdIsChanged() && isExternalAuthIdInUse()) {
			errorCode = ERROR_EXTERNAL_AUTH_ID_IN_USE;
		} else if (firstName.isEmpty()) {
			errorCode = ERROR_NO_FIRST_NAME;
		} else if (lastName.isEmpty()) {
			errorCode = ERROR_NO_LAST_NAME;
		} else if (!isRootUser() && selectedRoleUri.isEmpty()) {
			errorCode = ERROR_NO_ROLE;
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

	private boolean externalAuthIdIsChanged() {
		return !externalAuthId.equals(userAccount.getExternalAuthId());
	}

	private boolean isExternalAuthIdInUse() {
		if (externalAuthId.isEmpty()) {
			return false;
		}
		return userAccountsDao.getUserAccountByExternalAuthId(externalAuthId) != null;
	}

	private boolean isRootUser() {
		return ((userAccount != null) && userAccount.isRootUser());
	}

	public boolean isValid() {
		return errorCode.isEmpty();
	}

	public final ResponseValues showPage() {
		Map<String, Object> body = new HashMap<String, Object>();

		if (isSubmit()) {
			body.put("emailAddress", emailAddress);
			body.put("externalAuthId", externalAuthId);
			body.put("firstName", firstName);
			body.put("lastName", lastName);
			body.put("selectedRole", selectedRoleUri);
		} else {
			body.put("emailAddress", userAccount.getEmailAddress());
			body.put("externalAuthId", userAccount.getExternalAuthId());
			body.put("firstName", userAccount.getFirstName());
			body.put("lastName", userAccount.getLastName());
			body.put("selectedRole", getExistingRoleUri());
		}

		if (!isRootUser()) {
			body.put("roles", buildRolesList());
		}

		if (associateWithProfile) {
			body.put("associate", Boolean.TRUE);
		}
		body.put("formUrls", buildUrlsMapWithEditUrl());

		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}

		strategy.addMoreBodyValues(body);

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	private String getExistingRoleUri() {
		Set<String> uris = userAccount.getPermissionSetUris();
		if (uris.isEmpty()) {
			return "";
		} else {
			return uris.iterator().next();
		}
	}

	private Map<String, String> buildUrlsMapWithEditUrl() {
		Map<String, String> map = buildUrlsMap();
		map.put("edit", editAccountUrl(userAccount.getUri()));
		return map;
	}

	public void updateAccount() {
		userAccount.setEmailAddress(emailAddress);
		userAccount.setFirstName(firstName);
		userAccount.setLastName(lastName);
		userAccount.setExternalAuthId(externalAuthId);

		if (isRootUser()) {
			userAccount.setPermissionSetUris(Collections.<String> emptySet());
		} else {
			userAccount.setPermissionSetUris(Collections
					.singleton(selectedRoleUri));
		}

		strategy.setAdditionalProperties(userAccount);

		userAccountsDao.updateUserAccount(userAccount);

		strategy.notifyUser();
	}

	public boolean wasPasswordEmailSent() {
		return strategy.wasPasswordEmailSent();
	}

	public UserAccount getUpdatedAccount() {
		return userAccount;
	}

}
