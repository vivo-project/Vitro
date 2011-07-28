/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageRootAccount;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.user.UserAccountsUserController;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

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
	private static final String PARAMETER_EXTERNAL_AUTH_ONLY = "externalAuthOnly";
	private static final String PARAMETER_FIRST_NAME = "firstName";
	private static final String PARAMETER_LAST_NAME = "lastName";
	private static final String PARAMETER_ROLE = "role";
	private static final String PARAMETER_ASSOCIATED_PROFILE_URI = "associatedProfileUri";
	private static final String PARAMETER_NEW_PROFILE_CLASS_URI = "newProfileClassUri";

	private static final String ERROR_NO_EMAIL = "errorEmailIsEmpty";
	private static final String ERROR_EMAIL_IN_USE = "errorEmailInUse";
	private static final String ERROR_EMAIL_INVALID_FORMAT = "errorEmailInvalidFormat";
	private static final String ERROR_EXTERNAL_AUTH_ID_IN_USE = "errorExternalAuthIdInUse";
	private static final String ERROR_NO_FIRST_NAME = "errorFirstNameIsEmpty";
	private static final String ERROR_NO_LAST_NAME = "errorLastNameIsEmpty";
	private static final String ERROR_NO_ROLE = "errorNoRoleSelected";

	private static final String TEMPLATE_NAME = "userAccounts-edit.ftl";

	private final UserAccountsEditPageStrategy strategy;
	private final boolean matchingIsEnabled;

	/* The request parameters */
	private boolean submit;
	private String userUri = "";
	private String emailAddress = "";
	private String externalAuthId = "";
	private boolean externalAuthOnly;
	private String firstName = "";
	private String lastName = "";
	private String selectedRoleUri = "";
	private String associatedProfileUri = "";
	private String newProfileClassUri = "";

	private UserAccount userAccount;

	/** The result of checking whether this request is even appropriate. */
	private String bogusMessage = "";

	/** The result of validating a "submit" request. */
	private String errorCode = "";

	public UserAccountsEditPage(VitroRequest vreq) {
		super(vreq);

		this.strategy = UserAccountsEditPageStrategy.getInstance(vreq, this,
				isEmailEnabled());

		this.matchingIsEnabled = SelfEditingConfiguration.getBean(vreq)
				.isConfigured();

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
		externalAuthOnly = isFlagOnRequest(PARAMETER_EXTERNAL_AUTH_ONLY);
		firstName = getStringParameter(PARAMETER_FIRST_NAME, "");
		lastName = getStringParameter(PARAMETER_LAST_NAME, "");
		selectedRoleUri = getStringParameter(PARAMETER_ROLE, "");
		associatedProfileUri = getStringParameter(
				PARAMETER_ASSOCIATED_PROFILE_URI, "");
		newProfileClassUri = getStringParameter(
				PARAMETER_NEW_PROFILE_CLASS_URI, "");

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
		if (userAccount.isRootUser()) {
			if (!PolicyHelper.isAuthorizedForActions(vreq,
					new ManageRootAccount())) {
				log.warn("User is attempting to edit the root account, "
						+ "but is not authorized to do so. Logged in as: "
						+ LoginStatusBean.getCurrentUser(vreq));
				bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
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

		body.put("userUri", userUri);

		if (isSubmit()) {
			body.put("emailAddress", emailAddress);
			body.put("externalAuthId", externalAuthId);
			body.put("firstName", firstName);
			body.put("lastName", lastName);
			body.put("selectedRole", selectedRoleUri);
			body.put(PARAMETER_NEW_PROFILE_CLASS_URI, newProfileClassUri);

			if (externalAuthOnly) {
				body.put(PARAMETER_EXTERNAL_AUTH_ONLY, Boolean.TRUE);
			}

			if (!associatedProfileUri.isEmpty()) {
				body.put("associatedProfileInfo",
						buildProfileInfo(associatedProfileUri));
			}
		} else {
			body.put("emailAddress", userAccount.getEmailAddress());
			body.put("externalAuthId", userAccount.getExternalAuthId());
			body.put("firstName", userAccount.getFirstName());
			body.put("lastName", userAccount.getLastName());
			body.put("selectedRole", getExistingRoleUri());
			body.put(PARAMETER_NEW_PROFILE_CLASS_URI, "");

			if (userAccount.isExternalAuthOnly()) {
				body.put(PARAMETER_EXTERNAL_AUTH_ONLY, Boolean.TRUE);
			}

			List<Individual> associatedInds = SelfEditingConfiguration.getBean(
					vreq).getAssociatedIndividuals(indDao, userAccount);
			if (!associatedInds.isEmpty()) {
				body.put("associatedProfileInfo",
						buildProfileInfo(associatedInds.get(0).getURI()));
			}
		}

		if (!isRootUser()) {
			body.put("roles", buildRolesList());
		}

		body.put("profileTypes", buildProfileTypesList());
		body.put("formUrls", buildUrlsMapWithEditUrl());

		if (!errorCode.isEmpty()) {
			body.put(errorCode, Boolean.TRUE);
		}

		if (matchingIsEnabled) {
			body.put("showAssociation", Boolean.TRUE);
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
		// Assemble the fields of the account.
		userAccount.setEmailAddress(emailAddress);
		userAccount.setFirstName(firstName);
		userAccount.setLastName(lastName);
		userAccount.setExternalAuthId(externalAuthId);

		if (externalAuthOnly) {
			userAccount.setMd5Password("");
			userAccount.setOldPassword("");
			userAccount.setPasswordChangeRequired(false);
			userAccount.setPasswordLinkExpires(0L);
		}

		if (isRootUser()) {
			userAccount.setPermissionSetUris(Collections.<String> emptySet());
			userAccount.setExternalAuthOnly(false);
		} else {
			userAccount.setPermissionSetUris(Collections
					.singleton(selectedRoleUri));
			userAccount.setExternalAuthOnly(externalAuthOnly);
		}
		strategy.setAdditionalProperties(userAccount);

		// Update the account.
		userAccountsDao.updateUserAccount(userAccount);

		// Associate the profile, as appropriate.
		if (matchingIsEnabled) {
			if (!newProfileClassUri.isEmpty()) {
				try {
					String newProfileUri = UserAccountsProfileCreator
							.createProfile(indDao, dpsDao, newProfileClassUri,
									userAccount);
					associatedProfileUri = newProfileUri;
				} catch (InsertException e) {
					log.error("Failed to create new profile of class '"
							+ newProfileClassUri + "' for user '"
							+ userAccount.getEmailAddress() + "'");
				}
			}

			SelfEditingConfiguration.getBean(vreq)
					.associateIndividualWithUserAccount(indDao, dpsDao,
							userAccount, associatedProfileUri);
		}

		// Tell the user.
		strategy.notifyUser();
	}

	public boolean wasPasswordEmailSent() {
		return strategy.wasPasswordEmailSent();
	}

	public UserAccount getUpdatedAccount() {
		return userAccount;
	}

	boolean isExternalAuthOnly() {
		return externalAuthOnly;
	}
}
