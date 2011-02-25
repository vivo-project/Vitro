/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginInProcessFlag;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.User;
import freemarker.core.Environment;
import freemarker.template.TemplateHashModel;
import freemarker.template.utility.DeepUnwrap;

public class LoginWidget extends Widget {
	private static final Log log = LogFactory.getLog(LoginWidget.class);

    /** The page that kicks off the External Authentication process. */
	private static final String EXTERNAL_AUTH_SETUP_URL = "/loginExternalAuth";

    private static enum Macro {
        LOGIN("loginForm"),
        FORCE_PASSWORD_CHANGE("forcePasswordChange"),
        ALREADY_LOGGED_IN("alreadyLoggedIn"),
        SERVER_ERROR("error");
        
        private final String macroName;
        
        Macro(String macroName) {
            this.macroName = macroName;
        }

        @Override
		public String toString() {
            return macroName;
        }
        
    }

    private static enum TemplateVariable {
        LOGIN_NAME("loginName"),
        FORM_ACTION("formAction"),
        INFO_MESSAGE("infoMessage"),
        ERROR_MESSAGE("errorMessage"),
        EXTERNAL_AUTH_NAME("externalAuthName"),
        EXTERNAL_AUTH_URL("externalAuthUrl"),
        CANCEL_URL("cancelUrl"),
        SITE_NAME("siteName");

        private final String variableName;
        
        TemplateVariable(String variableName) {
            this.variableName = variableName;
        }

        @Override
		public String toString() {
            return variableName;
        }
        
    }

    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {
        
        WidgetTemplateValues values = null;
  
        try {
           
            State state = getCurrentLoginState(request);
            log.debug("State on exit: " + state);
            
            TemplateHashModel dataModel = env.getDataModel();
            
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
                values = showLoginScreen(request, dataModel.get("siteName").toString());
            }
            
            values.put("urls", dataModel.get("urls"));
            values.put("currentServlet", dataModel.get("currentServlet"));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);     
            User user =  (User) dm.get("user");   
            values.put("user", user); 
            
        } catch (Exception e) {
            log.error(e, e);
            // This widget should display an error message rather than throwing the exception
            // up to the doMarkup() method, which would result in no display.
            values = showError(e);
        } 
        
        return values;

    }

    /**
     * User is starting the login process. Show them the login screen.
     */
    private WidgetTemplateValues showLoginScreen(HttpServletRequest request, String siteName)
            throws IOException {
        LoginProcessBean bean = LoginProcessBean.getBean(request);
        log.trace("Going to login screen: " + bean);

        WidgetTemplateValues values = new WidgetTemplateValues(Macro.LOGIN.toString());
        values.put(TemplateVariable.FORM_ACTION.toString(), getAuthenticateUrl(request));
        values.put(TemplateVariable.LOGIN_NAME.toString(), bean.getUsername());
        
		String externalAuthDisplayName = ConfigurationProperties.getBean(
				request).getProperty("externalAuth.buttonText");
		if (externalAuthDisplayName != null) {
			values.put(TemplateVariable.EXTERNAL_AUTH_URL.toString(),
					UrlBuilder.getUrl(EXTERNAL_AUTH_SETUP_URL));
			values.put(TemplateVariable.EXTERNAL_AUTH_NAME.toString(),
					externalAuthDisplayName);
		}

        String infoMessage = bean.getInfoMessageAndClear();
        if (!infoMessage.isEmpty()) {
            values.put(TemplateVariable.INFO_MESSAGE.toString(), infoMessage);
        }
        String errorMessage = bean.getErrorMessageAndClear();
        if (!errorMessage.isEmpty()) {
            values.put(TemplateVariable.ERROR_MESSAGE.toString(), errorMessage);
        }
        
        values.put(TemplateVariable.SITE_NAME.toString(), siteName);

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
        log.trace("Going to password change screen: " + bean);

        WidgetTemplateValues values = new WidgetTemplateValues(
                Macro.FORCE_PASSWORD_CHANGE.toString());
        values.put(TemplateVariable.FORM_ACTION.toString(), getAuthenticateUrl(request));
        values.put(TemplateVariable.CANCEL_URL.toString(), getCancelUrl(request));

        String errorMessage = bean.getErrorMessageAndClear();
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
     * Are we already logged in? If not, where are we in the process?
     */
    private State getCurrentLoginState(HttpServletRequest request) {
        if (LoginStatusBean.getBean(request).isLoggedIn()) {
            return State.LOGGED_IN;
        } 
        if (isOutdatedLoginProcessBean(request)) {
        	LoginProcessBean.removeBean(request);
        }
        return LoginProcessBean.getBean(request).getState();
    }

	/**
	 * A LoginProcessBean is outdated unless the the "in-process" flag is set in the
	 * session. 
	 * 
	 * Each time we hit Authenticate, the flag is set, and each time
	 * we draw the widget it is reset.
	 */
	private boolean isOutdatedLoginProcessBean(HttpServletRequest request) {
		boolean inProcess = LoginInProcessFlag.checkAndReset(request);
		if (!inProcess) {
			log.debug("The process bean is outdated. Discard it.");
		}
		
		return !inProcess;
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
