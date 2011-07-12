/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import static javax.mail.Message.RecipientType.TO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailMessage;

/**
 * Handle the variant details of the UserAccountsAddPage.
 */
public abstract class UserAccountsEditPageStrategy extends UserAccountsPage {
	protected final UserAccountsEditPage page;

	public static UserAccountsEditPageStrategy getInstance(VitroRequest vreq,
			UserAccountsEditPage page, boolean emailEnabled) {
		if (emailEnabled) {
			return new EmailStrategy(vreq, page);
		} else {
			return new NoEmailStrategy(vreq, page);
		}
	}

	public UserAccountsEditPageStrategy(VitroRequest vreq,
			UserAccountsEditPage page) {
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

	private static class EmailStrategy extends UserAccountsEditPageStrategy {
		private static final String PARAMETER_RESET_PASSWORD = "resetPassword";
		private static final String EMAIL_TEMPLATE = "userAccounts-passwordResetPendingEmail.ftl";

		public static final String RESET_PASSWORD_URL = "/accounts/resetPassword";

		private boolean resetPassword;
		private boolean sentEmail;

		public EmailStrategy(VitroRequest vreq, UserAccountsEditPage page) {
			super(vreq, page);
		}

		@Override
		protected void parseAdditionalParameters() {
			resetPassword = isFlagOnRequest(PARAMETER_RESET_PASSWORD);
		}

		@Override
		protected String additionalValidations() {
			// No additional validations
			return "";
		}

		@Override
		protected void setAdditionalProperties(UserAccount u) {
			if (resetPassword && !page.isExternalAuthOnly()) {
				u.setPasswordLinkExpires(figureExpirationDate().getTime());
			}
		}

		@Override
		protected void addMoreBodyValues(Map<String, Object> body) {
			body.put("emailIsEnabled", Boolean.TRUE);
			if (resetPassword) {
				body.put("resetPassword", Boolean.TRUE);
			}
		}

		@Override
		protected void notifyUser() {
			if (!resetPassword) {
				return;
			}
			if (page.isExternalAuthOnly()) {
				return;
			}

			Map<String, Object> body = new HashMap<String, Object>();
			body.put("userAccount", page.getUpdatedAccount());
			body.put("passwordLink", buildResetPasswordLink());
			body.put("siteName", getSiteName());

			FreemarkerEmailMessage email = FreemarkerEmailFactory
					.createNewMessage(vreq);
			email.addRecipient(TO, page.getUpdatedAccount().getEmailAddress());
			email.setTemplate(EMAIL_TEMPLATE);
			email.setBodyMap(body);
			email.processTemplate();
			email.send();

			sentEmail = true;
		}

		private String buildResetPasswordLink() {
			try {
				String email = page.getUpdatedAccount().getEmailAddress();
				String hash = page.getUpdatedAccount()
						.getPasswordLinkExpiresHash();
				String relativeUrl = UrlBuilder.getUrl(RESET_PASSWORD_URL,
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

	private static class NoEmailStrategy extends UserAccountsEditPageStrategy {
		private static final String PARAMETER_NEW_PASSWORD = "newPassword";
		private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

		private static final String ERROR_WRONG_PASSWORD_LENGTH = "errorPasswordIsWrongLength";
		private static final String ERROR_PASSWORDS_DONT_MATCH = "errorPasswordsDontMatch";

		private String newPassword;
		private String confirmPassword;

		public NoEmailStrategy(VitroRequest vreq, UserAccountsEditPage page) {
			super(vreq, page);
		}

		@Override
		protected void parseAdditionalParameters() {
			newPassword = getStringParameter(PARAMETER_NEW_PASSWORD, "");
			confirmPassword = getStringParameter(PARAMETER_CONFIRM_PASSWORD, "");
		}

		@Override
		protected String additionalValidations() {
			if (page.isExternalAuthOnly()) {
				// No need to check the password info on external-only accounts
				return "";
			}

			if (newPassword.isEmpty() && confirmPassword.isEmpty()) {
				return "";
			} else if (!checkPasswordLength(newPassword)) {
				return ERROR_WRONG_PASSWORD_LENGTH;
			} else if (!newPassword.equals(confirmPassword)) {
				return ERROR_PASSWORDS_DONT_MATCH;
			} else {
				return "";
			}
		}

		@Override
		protected void addMoreBodyValues(Map<String, Object> body) {
			body.put("newPassword", newPassword);
			body.put("confirmPassword", confirmPassword);
			body.put("minimumLength", UserAccount.MIN_PASSWORD_LENGTH);
			body.put("maximumLength", UserAccount.MAX_PASSWORD_LENGTH);
		}

		@Override
		protected void setAdditionalProperties(UserAccount u) {
			if (!page.isExternalAuthOnly() && !newPassword.isEmpty()) {
				u.setMd5Password(Authenticator.applyMd5Encoding(newPassword));
				u.setPasswordChangeRequired(true);
			}
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
