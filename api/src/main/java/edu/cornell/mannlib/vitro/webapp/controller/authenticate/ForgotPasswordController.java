/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import javax.mail.Message;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaImplementation;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaServiceBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailMessage;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebServlet(name = "forgotPassword", urlPatterns = {"/forgotPassword"})
public class ForgotPasswordController extends FreemarkerHttpServlet {

    private static final String RESET_PASSWORD_URL = "/accounts/resetPassword";

    private static final int DAYS_TO_USE_PASSWORD_LINK = 5;

    private static final String TEMPLATE_NAME = "userAccounts-resetPasswordRequest.ftl";
    private static final Log log = LogFactory.getLog(ForgotPasswordController.class.getName());
    private CaptchaImplementation captchaImpl;

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
        // Random time interval sleep so attacker can't calculate whether provided email is bound to an account or not
        sleepForRandomTime();

        captchaImpl = CaptchaServiceBean.getCaptchaImpl();

        Map<String, Object> dataContext = new HashMap<>();
        setCommonValues(dataContext, vreq);
        CaptchaServiceBean.addCaptchaRelatedFieldsToPageContext(dataContext);
        boolean isEnabled = isFunctionalityEnabled();
        dataContext.put("isEnabled", isEnabled);

        // Handle GET request (display the form) or print error message if functionality is disabled
        if (!isEnabled || vreq.getMethod().equalsIgnoreCase("GET")) {
            return showForm(dataContext);
        }

        return handlePostRequest(vreq, dataContext);
    }

    /**
     * Handles a POST request for password reset. Validates the captcha, checks for spam mitigation,
     * processes the password change request, and notifies the user.
     *
     * @param vreq        The Vitro request object.
     * @param dataContext A map containing additional data context for processing the request.
     * @return A response containing the appropriate values for rendering the result.
     */
    private ResponseValues handlePostRequest(VitroRequest vreq, Map<String, Object> dataContext) {
        UserAccountsDao userAccountsDao = constructUserAccountsDao();
        I18nBundle i18n = I18n.bundle(vreq);

        // Check for impossible length input
        String rawEmailInput = vreq.getParameter("email");
        if (rawEmailInput != null && rawEmailInput.length() > 320) {
            dataContext.put("errorMessage", i18n.text("error_invalid_email"));
            return showForm(dataContext);
        }

        String email = getNonNullTrimmedParameterValue(vreq, "email");
        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            dataContext.put("errorMessage", i18n.text("error_invalid_email", email));
            return showForm(dataContext);
        }

        if (!captchaIsValid(vreq)) {
            dataContext.put("wrongCaptcha", true);
            dataContext.put("emailValue", email);
            return showForm(dataContext);
        }

        dataContext.put("showPasswordChangeForm", false);

        Optional<UserAccount> userAccountOptional = getAccountForInternalAuth(email);
        if (userAccountOptional.isPresent()) {
            requestPasswordChange(userAccountOptional.get(), userAccountsDao);
            notifyUser(userAccountOptional.get(), i18n, vreq);
        } else {
            log.info("User tried to reset password with an unassociated email: " + email);
        }

        return emailSentNotification(dataContext, i18n, email);
    }

    /**
     * Validates the captcha input based on the configured captcha implementation.
     * The method extracts the necessary input parameters from the provided VitroRequest.
     * For reCAPTCHA v2, it uses the "g-recaptcha-response" parameter.
     * For other captcha implementations (e.g., NanoCaptcha), it uses "userSolution" for captcha input
     * and "challengeId" for challenge identification.
     *
     * @param vreq the VitroRequest containing the captcha parameters
     * @return true if the captcha is valid, false otherwise
     */
    private boolean captchaIsValid(VitroRequest vreq) {
        String captchaInput;
        String challengeId;
        switch (captchaImpl) {
            case RECAPTCHAV2:
                captchaInput = getNonNullTrimmedParameterValue(vreq, "g-recaptcha-response");
                challengeId = "";
                break;
            case NANOCAPTCHA:
            default:
                captchaInput = getNonNullTrimmedParameterValue(vreq, "userSolution");
                challengeId = getNonNullTrimmedParameterValue(vreq, "challengeId");
        }

        return CaptchaServiceBean.validateCaptcha(captchaInput, challengeId);
    }

    /**
     * Notifies the user about a password reset request by sending an email.
     *
     * @param userAccount The user account for which the password reset is requested.
     * @param i18n        The internationalization bundle for language translation.
     * @param vreq        The VitroRequest object containing request information.
     */
    private void notifyUser(UserAccount userAccount, I18nBundle i18n, VitroRequest vreq) {
        Map<String, Object> body = new HashMap<>();
        body.put("userAccount", userAccount);
        body.put("passwordLink",
            buildResetPasswordLink(userAccount.getEmailAddress(), userAccount.getEmailKey(), vreq));
        body.put("siteName", vreq.getAppBean().getApplicationName());
        body.put("subject", i18n.text("password_reset_pending_email_subject"));
        body.put("textMessage", i18n.text("password_reset_pending_email_plain_text"));
        body.put("htmlMessage", i18n.text("password_reset_pending_email_html_text"));

        sendEmailMessage(body, vreq, userAccount.getEmailAddress());

        if (adminShouldBeNotified()) {
            notifyAdmin(userAccount, i18n, vreq);
        }
    }

    /**
     * Notifies the administrator about a password reset for a user account.
     *
     * @param userAccount The user account for which the password is being reset.
     * @param i18n        The internationalization bundle for translating messages.
     * @param vreq        The Vitro request object.
     */
    private void notifyAdmin(UserAccount userAccount, I18nBundle i18n, VitroRequest vreq) {
        Map<String, Object> body = new HashMap<>();
        body.put("userAccount", userAccount);
        body.put("siteName", vreq.getAppBean().getApplicationName());
        body.put("subject", i18n.text("password_reset_admin_notification_email_subject"));
        body.put("textMessage", i18n.text("password_reset_admin_notification_email_plain_text"));
        body.put("htmlMessage", i18n.text("password_reset_admin_notification_email_html"));

        String adminEmailAddress =
            Objects.requireNonNull(ConfigurationProperties.getInstance().getProperty("email.replyTo"));

        sendEmailMessage(body, vreq, adminEmailAddress);
    }

    /**
     * Sends an email message using Freemarker templates.
     *
     * @param body           The map containing email body information.
     * @param vreq           The Vitro request object.
     * @param recipientEmail The email address of the recipient.
     */
    private void sendEmailMessage(Map<String, Object> body, VitroRequest vreq, String recipientEmail) {
        FreemarkerEmailMessage emailMessage = FreemarkerEmailFactory
            .createNewMessage(vreq);
        emailMessage.addRecipient(Message.RecipientType.TO, recipientEmail);
        emailMessage.setBodyMap(body);
        emailMessage.processTemplate();
        emailMessage.send();
    }

    /**
     * Checks whether the admin should be notified about password resets.
     *
     * @return True if the admin should be notified, false otherwise.
     */
    private boolean adminShouldBeNotified() {
        String adminShouldBeNotified =
            ConfigurationProperties.getInstance().getProperty("authentication.forgotPassword.notify-admin");

        return Boolean.parseBoolean(adminShouldBeNotified);
    }

    /**
     * Sets common values in the data context for rendering templates.
     *
     * @param dataContext The data context to store common values.
     * @param vreq        The VitroRequest object containing request information.
     */
    private void setCommonValues(Map<String, Object> dataContext, VitroRequest vreq) {
        ApplicationBean appBean = vreq.getAppBean();

        dataContext.put("forgotPasswordUrl", getForgotPasswordUrl(vreq));
        dataContext.put("contactUrl", getContactUrl(vreq));
        dataContext.put("contextPath", vreq.getContextPath());
        dataContext.put("emailConfigured", FreemarkerEmailFactory.isConfigured(vreq));
        dataContext.put("emailValue", "");
        dataContext.put("contactEmailConfigured", StringUtils.isNotBlank(appBean.getContactMail()));
        dataContext.put("wrongCaptcha", false);
    }

    /**
     * Creates a response for indicating that an email has been successfully sent.
     *
     * @param dataContext The data context for rendering the response.
     * @param i18n        The internationalization bundle for language translation.
     * @param email       The email address to which the notification was sent.
     * @return A ResponseValues object containing the email sent message.
     */
    private ResponseValues emailSentNotification(Map<String, Object> dataContext, I18nBundle i18n, String email) {
        dataContext.put("message", i18n.text("password_reset_email_sent", email));
        return new TemplateResponseValues(TEMPLATE_NAME, dataContext);
    }

    /**
     * Shows a password change form.
     *
     * @param dataContext The data context for rendering the form.
     * @return A ResponseValues object for displaying the form.
     */
    private ResponseValues showForm(Map<String, Object> dataContext) {
        dataContext.put("showPasswordChangeForm", true);
        return new TemplateResponseValues(TEMPLATE_NAME, dataContext);
    }

    /**
     * Constructs a UserAccountsDao using the provided VitroRequest.
     *
     * @return A UserAccountsDao instance for user account operations.
     */
    private UserAccountsDao constructUserAccountsDao() {
        WebappDaoFactory wdf = ModelAccess.getInstance().getWebappDaoFactory();
        return wdf.getUserAccountsDao();
    }

    /**
     * Requests a password change for a user account by updating necessary account properties.
     *
     * @param userAccount     The user account for which the password change is requested.
     * @param userAccountsDao The data access object for user accounts.
     */
    private void requestPasswordChange(UserAccount userAccount, UserAccountsDao userAccountsDao) {
        userAccount.setPasswordLinkExpires(calculateExpirationDate().getTime());
        userAccount.generateEmailKey();
        userAccountsDao.updateUserAccount(userAccount);
    }

    /**
     * Retrieves a user account for internal authentication based on the provided email address.
     *
     * @param emailAddress The email address associated with the user account.
     * @return An Optional containing the user account if found,
     * or empty if not found or if the UserAccountsDao is null.
     */
    private Optional<UserAccount> getAccountForInternalAuth(String emailAddress) {
        UserAccountsDao userAccountsDao = getUserAccountsDao();
        if (userAccountsDao == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(userAccountsDao.getUserAccountByEmail(emailAddress));
    }

    /**
     * Retrieves a UserAccountsDao instance from the provided HttpServletRequest.
     *
     * @return A UserAccountsDao instance for user account operations.
     */
    private UserAccountsDao getUserAccountsDao() {
        UserAccountsDao userAccountsDao = getWebappDaoFactory()
            .getUserAccountsDao();
        if (userAccountsDao == null) {
            log.error("getUserAccountsDao: no UserAccountsDao");
        }

        return userAccountsDao;
    }

    /**
     * Retrieves a WebappDaoFactory instance from the provided HttpServletRequest.
     *
     * @return A WebappDaoFactory instance for database access.
     */
    private WebappDaoFactory getWebappDaoFactory() {
        return ModelAccess.getInstance().getWebappDaoFactory();
    }

    /**
     * Builds a reset password link using the provided email and key, and the VitroRequest information.
     *
     * @param email The email address of the user.
     * @param key   The key for resetting the password.
     * @param vreq  The VitroRequest object containing request information.
     * @return The reset password link as a string.
     */
    private String buildResetPasswordLink(String email, String key, VitroRequest vreq) {
        try {
            String relativeUrl = UrlBuilder.getUrl(RESET_PASSWORD_URL, "user", email, "key", key);

            URL context = new URL(vreq.getRequestURL().toString());
            URL url = new URL(context, relativeUrl);
            return url.toExternalForm();
        } catch (MalformedURLException e) {
            return "error_creating_password_link";
        }
    }

    /**
     * Calculates the expiration date for a password reset link.
     *
     * @return The expiration date as a Date object.
     */
    private Date calculateExpirationDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, DAYS_TO_USE_PASSWORD_LINK);
        return c.getTime();
    }

    /**
     * Retrieves the URL for the "Forgot Password" page from the VitroRequest context.
     *
     * @param request The VitroRequest object containing request information.
     * @return The URL for the "Forgot Password" page as a string.
     */
    private String getForgotPasswordUrl(VitroRequest request) {
        String contextPath = request.getContextPath();
        return contextPath + "/forgotPassword";
    }

    /**
     * Retrieves the URL for the "Contact" page from the VitroRequest context.
     *
     * @param request The VitroRequest object containing request information.
     * @return The URL for the "Contact" page as a string.
     */
    private String getContactUrl(VitroRequest request) {
        String contextPath = request.getContextPath();
        return contextPath + "/contact";
    }

    /**
     * Checks whether the functionality for password recovery is enabled based on the configuration.
     *
     * @return 'true' if the functionality is enabled, 'false' otherwise.
     */
    private boolean isFunctionalityEnabled() {
        String enabled = ConfigurationProperties.getInstance().getProperty("authentication.forgotPassword");
        return enabled != null && enabled.equalsIgnoreCase("enabled");
    }

    /**
     * Causes the current thread to sleep for a random duration to mitigate time-based attacks.
     */
    private void sleepForRandomTime() {
        Random random = new Random();
        int minSleepTime = 100;
        int maxSleepTime = 500;
        int randomSleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;

        try {
            Thread.sleep(randomSleepTime);
        } catch (InterruptedException e) {
            log.error(randomSleepTime + "ms sleep time for mitigating time-based attacks was interrupted.");
        }
    }

    /**
     * Retrieves the non-null and trimmed value of the specified request parameter.
     * If the parameter value is null, an empty string is returned.
     * Leading and trailing whitespace is removed from the parameter value.
     *
     * @param request      the HttpServletRequest containing the parameters
     * @param parameterKey the key of the parameter to retrieve
     * @return the non-null and trimmed value of the specified request parameter,
     * or an empty string if the parameter is null
     */
    private String getNonNullTrimmedParameterValue(HttpServletRequest request, String parameterKey) {
        String parameterValue = request.getParameter(parameterKey);
        return (parameterValue == null) ? "" : parameterValue.trim();
    }
}
