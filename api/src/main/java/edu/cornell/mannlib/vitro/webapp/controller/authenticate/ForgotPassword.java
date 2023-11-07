package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.mail.Message;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebServlet(name = "forgot-password", urlPatterns = {"/forgot-password"})
public class ForgotPassword extends FreemarkerHttpServlet {

    private static final String RESET_PASSWORD_URL = "/accounts/resetPassword";

    private static final int DAYS_TO_USE_PASSWORD_LINK = 5;

    private static final String TEMPLATE_NAME = "userAccounts-resetPasswordRequest.ftl";

    private static final Log log = LogFactory.getLog(ForgotPassword.class.getName());


    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
        // Random time interval sleep so attacker can't calculate whether provided email is bound or not
        sleepForRandomTime();

        Map<String, Object> dataContext = new HashMap<>();
        setCommonValues(dataContext, vreq);
        UserAccountsDao userAccountsDao = constructUserAccountsDao(vreq);
        I18nBundle i18n = I18n.bundle(vreq);

        boolean isEnabled = isFunctionalityEnabled(vreq);
        dataContext.put("isEnabled", isEnabled);
        if (!isEnabled || vreq.getMethod().equalsIgnoreCase("GET")) {
            return showForm(dataContext);
        }

        String captchaInput = vreq.getParameter("defaultReal");
        String captchaDisplay = vreq.getParameter("defaultRealHash");
        if (!captchaHash(captchaInput).equals(captchaDisplay)) {
            dataContext.put("wrongCaptcha", true);
            return showForm(dataContext);
        }

        dataContext.put("showPasswordChangeForm", false);
        String email = vreq.getParameter("email");

        PasswordChangeRequestSpamMitigationResponse mitigationResponse =
            PasswordChangeRequestSpamMitigation.isPasswordResetRequestable(email);
        if (!mitigationResponse.getCanBeRequested()) {
            dataContext.put("message",
                i18n.text("password_reset_too_many_requests") + mitigationResponse.getNextRequestAvailableAtDate() +
                    i18n.text("password_reset_too_many_requests_at_time") +
                    mitigationResponse.getNextRequestAvailableAtTime());
            return new TemplateResponseValues(TEMPLATE_NAME, dataContext);
        }

        UserAccount userAccount = getAccountForInternalAuth(email, vreq);
        if (userAccount != null) {
            requestPasswordChange(userAccount, userAccountsDao);
            notifyUser(userAccount, i18n, vreq);
        }

        PasswordChangeRequestSpamMitigation.requestSuccessfullyHandledAndUserIsNotified(email);
        return emailSentMessage(dataContext, i18n, email);
    }

    /**
     * Notifies the user about a password reset request by sending an email.
     *
     * @param userAccount The user account for which the password reset is requested.
     * @param i18n        The internationalization bundle for language translation.
     * @param vreq        The VitroRequest object containing request information.
     */
    private void notifyUser(UserAccount userAccount, I18nBundle i18n,
                            VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("userAccount", userAccount);
        body.put("passwordLink",
            buildResetPasswordLink(userAccount.getEmailAddress(), userAccount.getEmailKey(), vreq));
        body.put("siteName", vreq.getAppBean().getApplicationName());
        body.put("subject", i18n.text("password_reset_pending_email_subject"));
        body.put("textMessage", i18n.text("password_reset_pending_email_plain_text"));
        body.put("htmlMessage", i18n.text("password_reset_pending_email_html_text"));

        FreemarkerEmailMessage emailMessage = FreemarkerEmailFactory
            .createNewMessage(vreq);
        emailMessage.addRecipient(Message.RecipientType.TO, userAccount.getEmailAddress());
        emailMessage.setBodyMap(body);
        emailMessage.processTemplate();
        emailMessage.send();
    }

    /**
     * Generates a hash value for a given string.
     *
     * @param value The input string to be hashed.
     * @return The computed hash value as a string.
     */
    private String captchaHash(String value) {
        int hash = 5381;
        value = value.toUpperCase();
        for (int i = 0; i < value.length(); i++) {
            hash = ((hash << 5) + hash) + value.charAt(i);
        }
        return String.valueOf(hash);
    }

    /**
     * Sets common values in the data context for rendering templates.
     *
     * @param dataContext The data context to store common values.
     * @param vreq        The VitroRequest object containing request information.
     */
    private void setCommonValues(Map<String, Object> dataContext, VitroRequest vreq) {
        dataContext.put("forgotPasswordUrl", getForgotPasswordUrl(vreq));
        dataContext.put("contactUrl", getContactUrl(vreq));
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
    private ResponseValues emailSentMessage(Map<String, Object> dataContext, I18nBundle i18n, String email) {
        dataContext.put("message",
            i18n.text("password_reset_email_sent") + email + i18n.text("password_reset_email_sent_if_exists"));
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
     * @param vreq The VitroRequest object for database access.
     * @return A UserAccountsDao instance for user account operations.
     */
    private UserAccountsDao constructUserAccountsDao(VitroRequest vreq) {
        ServletContext ctx = vreq.getSession().getServletContext();
        WebappDaoFactory wdf = ModelAccess.on(ctx).getWebappDaoFactory();
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
     * Retrieves the user account associated with an email address for internal authentication.
     *
     * @param emailAddress The email address for which to retrieve the user account.
     * @param request      The HttpServletRequest for web request information.
     * @return The UserAccount associated with the provided email address.
     */
    private UserAccount getAccountForInternalAuth(String emailAddress, HttpServletRequest request) {
        UserAccountsDao userAccountsDao = getUserAccountsDao(request);
        if (userAccountsDao == null) {
            log.info("User tried to reset password with an unbound email: " + emailAddress);
            return null;
        }
        return userAccountsDao.getUserAccountByEmail(emailAddress);
    }

    /**
     * Retrieves a UserAccountsDao instance from the provided HttpServletRequest.
     *
     * @param request The HttpServletRequest for accessing the UserAccountsDao.
     * @return A UserAccountsDao instance for user account operations.
     */
    private UserAccountsDao getUserAccountsDao(HttpServletRequest request) {
        UserAccountsDao userAccountsDao = getWebappDaoFactory(request)
            .getUserAccountsDao();
        if (userAccountsDao == null) {
            log.error("getUserAccountsDao: no UserAccountsDao");
        }

        return userAccountsDao;
    }

    /**
     * Retrieves a WebappDaoFactory instance from the provided HttpServletRequest.
     *
     * @param request The HttpServletRequest for accessing the WebappDaoFactory.
     * @return A WebappDaoFactory instance for database access.
     */
    private WebappDaoFactory getWebappDaoFactory(HttpServletRequest request) {
        return ModelAccess.on(request).getWebappDaoFactory();
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
        return contextPath + "/forgot-password";
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
     * Checks whether the functionality for resetting passwords is enabled based on configuration properties.
     *
     * @param vreq The VitroRequest object for retrieving configuration properties.
     * @return `true` if the functionality is enabled, `false` otherwise.
     */
    private boolean isFunctionalityEnabled(VitroRequest vreq) {
        String enabled = ConfigurationProperties.getBean(vreq).getProperty("authentication.forgotPassword");
        return enabled.equalsIgnoreCase("enabled");
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
}