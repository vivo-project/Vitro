/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import static javax.mail.Message.RecipientType.TO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailMessage;

/**
 * Handle the variant details of the UserAccountsAddPage.
 */
public abstract class UserAccountsAddPageStrategy extends UserAccountsPage {
	protected final UserAccountsAddPage page;

	public static UserAccountsAddPageStrategy getInstance(VitroRequest vreq,
			UserAccountsAddPage page, boolean emailEnabled) {
		if (emailEnabled) {
			return new EmailStrategy(vreq, page);
		} else {
			return new NoEmailStrategy(vreq, page);
		}
	}

	public UserAccountsAddPageStrategy(VitroRequest vreq,
			UserAccountsAddPage page) {
		super(vreq);
		this.page = page;
	}

	protected abstract void parseAdditionalParameters();

	protected abstract String additionalValidations();

	protected abstract void addMoreBodyValues(Map<String, Object> body);

	protected abstract void setAdditionalProperties(UserAccount u);

	protected abstract void notifyUser();

	protected abstract boolean wasPasswordEmailSent();

	// ----------------------------------------------------------------------
	// Strategy to use if email is enabled.
	// ----------------------------------------------------------------------

	private static class EmailStrategy extends UserAccountsAddPageStrategy {
		public static final String CREATE_PASSWORD_URL = "/accounts/createPassword";
		private static final String EMAIL_TEMPLATE_WITH_PASSWORD = "userAccounts-acctCreatedEmail.ftl";
		private static final String EMAIL_TEMPLATE_NO_PASSWORD = "userAccounts-acctCreatedExternalOnlyEmail.ftl";

		private boolean sentEmail;

		public EmailStrategy(VitroRequest vreq, UserAccountsAddPage page) {
			super(vreq, page);
		}

		@Override
		protected void parseAdditionalParameters() {
			// No additional parameters
		}

		@Override
		protected String additionalValidations() {
			// No additional validations
			return "";
		}

		@Override
		protected void setAdditionalProperties(UserAccount u) {
			if (page.isExternalAuthOnly()) {
				u.setPasswordLinkExpires(0L);
				u.setStatus(Status.ACTIVE);
			} else {
				u.setPasswordLinkExpires(figureExpirationDate().getTime());
				u.setStatus(Status.INACTIVE);
			}
		}

		@Override
		protected void addMoreBodyValues(Map<String, Object> body) {
			body.put("emailIsEnabled", Boolean.TRUE);
		}

		@Override
		protected void notifyUser() {
			Map<String, Object> body = new HashMap<String, Object>();
			body.put("userAccount", page.getAddedAccount());
			body.put("passwordLink", buildCreatePasswordLink());
			body.put("siteName", getSiteName());

			FreemarkerEmailMessage email = FreemarkerEmailFactory
					.createNewMessage(vreq);
			email.addRecipient(TO, page.getAddedAccount().getEmailAddress());
			email.setSubject("Your VIVO account has been created.");
			if (page.isExternalAuthOnly()) {
				email.setTemplate(EMAIL_TEMPLATE_NO_PASSWORD);
			} else {
				email.setTemplate(EMAIL_TEMPLATE_WITH_PASSWORD);
			}
			email.setBodyMap(body);
			email.processTemplate();
			email.send();

			sentEmail = true;
		}

		private String buildCreatePasswordLink() {
			try {
				String email = page.getAddedAccount().getEmailAddress();
				String hash = page.getAddedAccount()
						.getPasswordLinkExpiresHash();
				String relativeUrl = UrlBuilder.getUrl(CREATE_PASSWORD_URL,
						"user", email, "key", hash);

				URL context = new URL(vreq.getRequestURL().toString());
				URL url = new URL(context, relativeUrl);
				return url.toExternalForm();
			} catch (MalformedURLException e) {
				return "error_creating_password_link";
			}
		}

		@Override
		protected boolean wasPasswordEmailSent() {
			return sentEmail;
		}

	}

	// ----------------------------------------------------------------------
	// Strategy to use if email is not enabled.
	// ----------------------------------------------------------------------

	private static class NoEmailStrategy extends UserAccountsAddPageStrategy {
		private static final String PARAMETER_INITIAL_PASSWORD = "initialPassword";
		private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

		private static final String ERROR_NO_PASSWORD = "errorPasswordIsEmpty";
		private static final String ERROR_WRONG_PASSWORD_LENGTH = "errorPasswordIsWrongLength";
		private static final String ERROR_PASSWORDS_DONT_MATCH = "errorPasswordsDontMatch";

		private String initialPassword;
		private String confirmPassword;

		public NoEmailStrategy(VitroRequest vreq, UserAccountsAddPage page) {
			super(vreq, page);
		}

		@Override
		protected void parseAdditionalParameters() {
			initialPassword = getStringParameter(PARAMETER_INITIAL_PASSWORD, "");
			confirmPassword = getStringParameter(PARAMETER_CONFIRM_PASSWORD, "");
		}

		@Override
		protected String additionalValidations() {
			if (page.isExternalAuthOnly()) {
				// No need to check the password info on external-only accounts
				return "";
			}

			if (initialPassword.isEmpty()) {
				return ERROR_NO_PASSWORD;
			} else if (!checkPasswordLength()) {
				return ERROR_WRONG_PASSWORD_LENGTH;
			} else if (!initialPassword.equals(confirmPassword)) {
				return ERROR_PASSWORDS_DONT_MATCH;
			} else {
				return "";
			}
		}

		private boolean checkPasswordLength() {
			return initialPassword.length() >= UserAccount.MIN_PASSWORD_LENGTH
					&& initialPassword.length() <= UserAccount.MAX_PASSWORD_LENGTH;
		}

		@Override
		protected void addMoreBodyValues(Map<String, Object> body) {
			body.put("initialPassword", initialPassword);
			body.put("confirmPassword", confirmPassword);
			body.put("minimumLength", UserAccount.MIN_PASSWORD_LENGTH);
			body.put("maximumLength", UserAccount.MAX_PASSWORD_LENGTH);
		}

		@Override
		protected void setAdditionalProperties(UserAccount u) {
			if (!page.isExternalAuthOnly()) {
				u.setMd5Password(Authenticator
						.applyMd5Encoding(initialPassword));
				u.setPasswordChangeRequired(true);
			}
			u.setStatus(Status.ACTIVE);
		}

		@Override
		protected void notifyUser() {
			// Do nothing.
		}

		@Override
		protected boolean wasPasswordEmailSent() {
			return false;
		}

	}

}
