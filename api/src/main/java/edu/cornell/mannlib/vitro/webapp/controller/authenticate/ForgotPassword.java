package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Message;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
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
public class ForgotPassword extends VitroHttpServlet {

    public static final String RESET_PASSWORD_URL = "/accounts/resetPassword";

    protected static final int DAYS_TO_USE_PASSWORD_LINK = 5;

    private static final Log log = LogFactory.getLog(ForgotPassword.class
        .getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        VitroRequest vreq = new VitroRequest(request);
        I18nBundle i18n = I18n.bundle(vreq);
        String email = request.getParameter("email");

        UserAccount userAccount = getAccountForInternalAuth(email, request);
        if (userAccount == null) {
            out.println("<h1>" + i18n.text("password_reset_email_non_existing") + "</h1>");
        }

        userAccount.setPasswordLinkExpires(figureExpirationDate().getTime());
        userAccount.generateEmailKey();
        userAccount.setStatus(UserAccount.Status.INACTIVE);

        notifyUser(userAccount, i18n, vreq);

        out.println("<h1>" + i18n.text("password_reset_email_sent") + email + "</h1>");
    }

    protected void notifyUser(UserAccount userAccount, I18nBundle i18n,
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

    public UserAccount getAccountForInternalAuth(String emailAddress, HttpServletRequest request) {
        UserAccountsDao userAccountsDao = getUserAccountsDao(request);
        if (userAccountsDao == null) {
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

    private Date figureExpirationDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, DAYS_TO_USE_PASSWORD_LINK);
        return c.getTime();
    }
}