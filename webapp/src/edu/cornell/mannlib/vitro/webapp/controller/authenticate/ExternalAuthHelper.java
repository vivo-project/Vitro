/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * Capture the properties used by the External Authorization system, and use
 * them to assist in the process.
 * 
 * The first time this bean is requested, it is created from the configuration
 * properties and cached in the session. After that, the cached version is used.
 */
public class ExternalAuthHelper {
	private static final Log log = LogFactory.getLog(ExternalAuthHelper.class);

	private static final ExternalAuthHelper DUMMY_HELPER = new ExternalAuthHelper(
			null, null);

	private static final String BEAN_ATTRIBUTE = ExternalAuthHelper.class
			.getName();

	/** This configuration property points to the external authorization server. */
	private static final String PROPERTY_EXTERNAL_AUTH_SERVER_URL = "externalAuth.serverUrl";

	/** This configuration property says which HTTP header holds the username. */
	public static final String PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER = "externalAuth.netIdheaderName";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If there is no session, there is no bean. If there is a session and no
	 * bean, create one.
	 * 
	 * Never returns null.
	 */
	public static ExternalAuthHelper getHelper(ServletRequest request) {
		if (!(request instanceof HttpServletRequest)) {
			log.trace("Not an HttpServletRequest: " + request);
			return DUMMY_HELPER;
		}

		HttpSession session = ((HttpServletRequest) request).getSession(false);
		if (session == null) {
			log.trace("No session; no need to create one.");
			return DUMMY_HELPER;
		}

		Object attr = session.getAttribute(BEAN_ATTRIBUTE);
		if (attr instanceof ExternalAuthHelper) {
			log.trace("Found a bean: " + attr);
			return (ExternalAuthHelper) attr;
		}

		ExternalAuthHelper bean = buildBean();
		log.debug("Created a bean: " + bean);
		session.setAttribute(BEAN_ATTRIBUTE, bean);
		return bean;
	}

	private static ExternalAuthHelper buildBean() {
		String externalAuthServerUrl = ConfigurationProperties
				.getProperty(PROPERTY_EXTERNAL_AUTH_SERVER_URL);
		String externalAuthHeaderName = ConfigurationProperties
				.getProperty(PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER);

		return new ExternalAuthHelper(externalAuthServerUrl,
				externalAuthHeaderName);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final String externalAuthServerUrl;
	private final String externalAuthHeaderName;

	private ExternalAuthHelper(String externalAuthServerUrl,
			String externalAuthHeaderName) {
		this.externalAuthServerUrl = trimThis(externalAuthServerUrl);
		this.externalAuthHeaderName = trimThis(externalAuthHeaderName);
	}

	private String trimThis(String string) {
		if (string == null) {
			return null;
		} else {
			return string.trim();
		}
	}

	public String buildExternalAuthRedirectUrl(String returnUrl) {
		if (returnUrl == null) {
			log.error("returnUrl is null.");
			return null;
		}

		if (externalAuthServerUrl == null) {
			log.debug("deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_SERVER_URL
					+ "' -- sending directly to '" + returnUrl + "'");
			return returnUrl;
		}

		try {
			String encodedReturnUrl = URLEncoder.encode(returnUrl, "UTF-8");
			String externalAuthUrl = externalAuthServerUrl + "?target="
					+ encodedReturnUrl;
			log.debug("externalAuthUrl is '" + externalAuthUrl + "'");
			return externalAuthUrl;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // No UTF-8? Really?
		}
	}

	public String getExternalUsername(HttpServletRequest request) {
		if (request == null) {
			log.error("request is null.");
			return null;
		}

		if (externalAuthHeaderName == null) {
			log.error("User asked for external authentication, "
					+ "but deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER + "'");
			return null;
		}

		String username = request.getHeader(externalAuthHeaderName);
		log.debug("username=" + username);
		return username;
	}

	@Override
	public String toString() {
		return "ExternalAuthHelper[externalAuthServerUrl="
				+ externalAuthServerUrl + ", externalAuthHeaderName="
				+ externalAuthHeaderName + "]";
	}

}
