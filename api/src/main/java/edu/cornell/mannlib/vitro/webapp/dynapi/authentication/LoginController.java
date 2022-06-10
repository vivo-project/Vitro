package edu.cornell.mannlib.vitro.webapp.dynapi.authentication;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BasicAuthenticator;
import edu.cornell.mannlib.vitro.webapp.dynapi.RESTEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.API_SERVLET_LOGIN;

@WebServlet(name = "Login", urlPatterns = {API_SERVLET_LOGIN})
public class LoginController extends VitroHttpServlet {
    
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

        if (username==null || password==null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicAuthenticator authenticator = new BasicAuthenticator(request);

        UserAccount user = authenticator.getAccountForInternalAuth(
                username);

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!authenticator.isUserPermittedToLogin(user)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!authenticator.isCurrentPasswordArgon2(user, password)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.getSession().setAttribute("logged_in_user", user);
        response.setStatus(200);
    }

}
