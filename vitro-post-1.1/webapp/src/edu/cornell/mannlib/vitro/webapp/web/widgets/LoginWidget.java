/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import freemarker.core.Environment;
import freemarker.template.TemplateModel;

public class LoginWidget extends Widget {

    private static final Log log = LogFactory.getLog(LoginWidget.class);

    private static enum Macro {
        LOGIN("loginForm"),
        FORCE_PASSWORD_CHANGE("forcePasswordChange"),
        ALREADY_LOGGED_IN("alreadyLoggedIn"),
        SERVER_ERROR("error");
        
        private final String macroName;
        
        Macro(String macroName) {
            this.macroName = macroName;
        }

        public String toString() {
            return macroName;
        }
        
    }

    private static enum TemplateVariable {
        LOGIN_NAME("loginName"),
        FORM_ACTION("formAction"),
        INFO_MESSAGE("infoMessage"),
        ERROR_MESSAGE("errorMessage"),
        CANCEL_URL("cancelUrl");

        private final String variableName;
        
        TemplateVariable(String variableName) {
            this.variableName = variableName;
        }

        public String toString() {
            return variableName;
        }
        
    }

    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {
        
        WidgetTemplateValues values = null;
        TemplateModel urls = null;
        
        try {
            urls = env.getDataModel().get("urls");
            State state = getCurrentLoginState(request);
            log.debug("State on exit: " + state);
                        
            switch (state) {
            case LOGGED_IN:
                // On the login page itself, show a message that the user is already logged in.
                // Otherwise, when redirecting to login page from a page that the logged-in user
                // doesn't have access to, we would just show a blank page.
                if (request.getServletPath().equals(Route.LOGIN.path())) {
                    values = showMessageToLoggedInUser(request);
                    break;
                } else {
                    return null;
                }
            case FORCED_PASSWORD_CHANGE:
                values = showPasswordChangeScreen(request);
                break;
            default:
                values = showLoginScreen(request);
            }
        } catch (Exception e) {
            log.error(e);
            // This widget should display an error message rather than throwing the exception
            // up to the doMarkup() method, which would result in no display.
            values = showError(e);
        } 
        values.put("urls", urls);
        return values;

    }

    /**
     * User is just starting the login process. Be sure that we have a
     * {@link LoginProcessBean} with the correct status. Show them the login
     * screen.
     */
    private WidgetTemplateValues showLoginScreen(HttpServletRequest request)
            throws IOException {
        LoginProcessBean bean = LoginProcessBean.getBean(request);
        bean.setState(State.LOGGING_IN);
        log.trace("Going to login screen: " + bean);

        WidgetTemplateValues values = new WidgetTemplateValues(Macro.LOGIN.toString());
        values.put(TemplateVariable.FORM_ACTION.toString(), getAuthenticateUrl(request));
        values.put(TemplateVariable.LOGIN_NAME.toString(), bean.getUsername());

        String infoMessage = bean.getInfoMessage();
        if (!infoMessage.isEmpty()) {
            values.put(TemplateVariable.INFO_MESSAGE.toString(), infoMessage);
        }
        String errorMessage = bean.getErrorMessage();
        if (!errorMessage.isEmpty()) {
            values.put(TemplateVariable.ERROR_MESSAGE.toString(), errorMessage);
        }

        return values;
    }
    
    private WidgetTemplateValues showMessageToLoggedInUser(HttpServletRequest request) {
        return new WidgetTemplateValues(Macro.ALREADY_LOGGED_IN.toString());
    }

    /**
     * The user has given the correct password, but now they are required to
     * change it (unless they cancel out).
     */
    private WidgetTemplateValues showPasswordChangeScreen(HttpServletRequest request) {
        LoginProcessBean bean = LoginProcessBean.getBean(request);
        bean.setState(State.FORCED_PASSWORD_CHANGE);
        log.trace("Going to password change screen: " + bean);

        WidgetTemplateValues values = new WidgetTemplateValues(
                Macro.FORCE_PASSWORD_CHANGE.toString());
        values.put(TemplateVariable.FORM_ACTION.toString(), getAuthenticateUrl(request));
        values.put(TemplateVariable.CANCEL_URL.toString(), getCancelUrl(request));

        String errorMessage = bean.getErrorMessage();
        if (!errorMessage.isEmpty()) {
            values.put(TemplateVariable.ERROR_MESSAGE.toString(), errorMessage);
        }
        return values;
    }

    private WidgetTemplateValues showError(Exception e) {
        WidgetTemplateValues values = new WidgetTemplateValues(
                Macro.SERVER_ERROR.toString());
        values.put(TemplateVariable.ERROR_MESSAGE.toString(), "Internal server error:<br /> " + e);
        return values;
    }

    /**
     * Where are we in the process? Logged in? Not? Somewhere in between?
     */
    private State getCurrentLoginState(HttpServletRequest request) {
        if (LoginStatusBean.getBean(request).isLoggedIn()) {
            return State.LOGGED_IN;
        } else {
            return LoginProcessBean.getBean(request).getState();
        }
    }

    /** What's the URL for this servlet? */
    private String getAuthenticateUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();   
        return contextPath + "/authenticate";
    }

    /** What's the URL for this servlet, with the cancel parameter added? */
    private String getCancelUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String urlParams = "?cancel=true";
        return contextPath + "/authenticate" + urlParams;
    }

}
