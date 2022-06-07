package edu.cornell.mannlib.vitro.webapp.dynapi.authentication;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BasicAuthenticator;
import edu.cornell.mannlib.vitro.webapp.dynapi.RESTEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.base.Sys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "Login", urlPatterns = {"/rest_login"})
public class LoginController extends VitroHttpServlet {

    private static final Log log = LogFactory.getLog(RESTEndpoint.class);

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.service(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username==null || password==null){
            writeResponseError(response, "'username' and 'password' parameters must be present");
            return;
        }

        BasicAuthenticator authenticator = new BasicAuthenticator(request);

        UserAccount user = authenticator.getAccountForInternalAuth(
                username);

        if (user == null){
            writeResponseError(response, "User with given username does not exist");
            return;
        }

        if (!authenticator.isUserPermittedToLogin(user)) {
            writeResponseError(response, "User is not permitted to login");
            return;
        }

        if (!authenticator.isCurrentPasswordArgon2(user, password)) {
            writeResponseError(response, "Wrong password");
            return;
        }

        request.getSession().setAttribute("logged_in_user", user);
        response.setStatus(200);
    }

    private void writeResponseError(HttpServletResponse response, String message){
        try {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
