package edu.cornell.mannlib.vitro.webapp.filters;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.INTERNAL;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.auth.checks.UserOnThread;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator.LoginNotPermitted;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebFilter(filterName = "Basic Authentication filter", urlPatterns = { "/*" })
public class BasicAuthFilter implements Filter {

    private static final String PROPERTY_NAME = "authentication.basic";
    private static final String ENABLED = "enabled";
    private static final String UNAUTHORIZED_ACCESS = "Unauthorized access.";
    private static final Log log = LogFactory.getLog(BasicAuthFilter.class);

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String authHeader = request.getHeader("Authorization");
        boolean isDisabled = !isEnabled();
        if (isDisabled || authHeader == null || !authHeader.startsWith("Basic ")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String base64 = authHeader.substring(6).trim();
            String credentials = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);
            if ((values.length != 2)) {
                throw new IllegalArgumentException(
                        format("Expected 2 arguments: username and password, received %d", values.length));
            }
            String username = values[0];
            String password = values[1];
            Authenticator authenticator = Authenticator.getInstance(request);
            UserAccount user = authenticator.getAccountForInternalAuth(username);
            if (user != null && authenticator.isUserPermittedToLogin(user)
                    && authenticator.isCurrentPasswordArgon2(user, password)) {
                try (UserOnThread uot = new UserOnThread(user.getUri())) {
                    authenticator.recordLoginAgainstUserAccount(user, INTERNAL);
                } catch (LoginNotPermitted e) {
                    throw e;
                }
                chain.doFilter(request, response);
                return;
            } else {
                response.sendError(SC_FORBIDDEN, UNAUTHORIZED_ACCESS);
                return;
            }
        } catch (IllegalArgumentException | LoginNotPermitted e) {
            log.error(e, e);
            response.sendError(SC_BAD_REQUEST, UNAUTHORIZED_ACCESS);
        }
    }

    private boolean isEnabled() {
        return ENABLED.equalsIgnoreCase(ConfigurationProperties.getInstance().getProperty(PROPERTY_NAME));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
