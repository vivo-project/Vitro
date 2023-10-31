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
        dataContext.put("forgotPasswordUrl", getForgotPasswordUrl(vreq));
        dataContext.put("contactUrl", getContactUrl(vreq));
        UserAccountsDao userAccountsDao = constructUserAccountsDao(vreq);
        I18nBundle i18n = I18n.bundle(vreq);

        if (vreq.getMethod().equalsIgnoreCase("GET")) {
            dataContext.put("showPasswordChangeForm", true);
            return new TemplateResponseValues(TEMPLATE_NAME, dataContext);
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

    private ResponseValues emailSentMessage(Map<String, Object> dataContext, I18nBundle i18n, String email) {
        dataContext.put("message",
            i18n.text("password_reset_email_sent") + email + i18n.text("password_reset_email_sent_if_exists"));
        return new TemplateResponseValues(TEMPLATE_NAME, dataContext);
    }

    private UserAccountsDao constructUserAccountsDao(VitroRequest vreq) {
        ServletContext ctx = vreq.getSession().getServletContext();
        WebappDaoFactory wdf = ModelAccess.on(ctx).getWebappDaoFactory();
        return wdf.getUserAccountsDao();
    }

    private void requestPasswordChange(UserAccount userAccount, UserAccountsDao userAccountsDao) {
        userAccount.setPasswordLinkExpires(calculateExpirationDate().getTime());
        userAccount.generateEmailKey();
        userAccountsDao.updateUserAccount(userAccount);
    }

    private UserAccount getAccountForInternalAuth(String emailAddress, HttpServletRequest request) {
        UserAccountsDao userAccountsDao = getUserAccountsDao(request);
        if (userAccountsDao == null) {
            log.info("User tried to reset password with an unbound email: " + emailAddress);
            return null;
        }
        return userAccountsDao.getUserAccountByEmail(emailAddress);
    }

    private UserAccountsDao getUserAccountsDao(HttpServletRequest request) {
        UserAccountsDao userAccountsDao = getWebappDaoFactory(request)
            .getUserAccountsDao();
        if (userAccountsDao == null) {
            log.error("getUserAccountsDao: no UserAccountsDao");
        }

        return userAccountsDao;
    }

    private WebappDaoFactory getWebappDaoFactory(HttpServletRequest request) {
        return ModelAccess.on(request).getWebappDaoFactory();
    }

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

    private Date calculateExpirationDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, DAYS_TO_USE_PASSWORD_LINK);
        return c.getTime();
    }

    private String getForgotPasswordUrl(VitroRequest request) {
        String contextPath = request.getContextPath();
        return contextPath + "/forgot-password";
    }

    private String getContactUrl(VitroRequest request) {
        String contextPath = request.getContextPath();
        return contextPath + "/contact";
    }

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