/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

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

	/** This configuration property says which HTTP header holds the auth ID. */
	public static final String PROPERTY_EXTERNAL_AUTH_ID_HEADER = "externalAuth.netIdHeaderName";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Get the bean from the servlet context. If there is no bean, create one.
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

		ServletContext ctx = session.getServletContext();

		Object attr = ctx.getAttribute(BEAN_ATTRIBUTE);
		if (attr instanceof ExternalAuthHelper) {
			log.trace("Found a bean: " + attr);
			return (ExternalAuthHelper) attr;
		}

		ExternalAuthHelper bean = buildBean(ctx);
		log.debug("Created a bean: " + bean);
		setBean(ctx, bean);
		return bean;
	}

	/** It would be private, but we want to allow calls for faking. */
	protected static void setBean(ServletContext context,
			ExternalAuthHelper bean) {
		context.setAttribute(BEAN_ATTRIBUTE, bean);
	}

	private static ExternalAuthHelper buildBean(ServletContext ctx) {
		String externalAuthServerUrl = ConfigurationProperties.getBean(ctx)
				.getProperty(PROPERTY_EXTERNAL_AUTH_SERVER_URL);
		String externalAuthHeaderName = ConfigurationProperties.getBean(ctx)
				.getProperty(PROPERTY_EXTERNAL_AUTH_ID_HEADER);

		return new ExternalAuthHelper(externalAuthServerUrl,
				externalAuthHeaderName);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final String externalAuthServerUrl;
	private final String externalAuthHeaderName;

	/** It would be private, but we want to allow subclasses for faking. */
	protected ExternalAuthHelper(String externalAuthServerUrl,
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

	public String getExternalAuthId(HttpServletRequest request) {
		if (request == null) {
			log.error("request is null.");
			return null;
		}

		if (externalAuthHeaderName == null) {
			log.error("User asked for external authentication, "
					+ "but deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_ID_HEADER + "'");
			return null;
		}

		String externalAuthId = request.getHeader(externalAuthHeaderName);
		log.debug("externalAuthId=" + externalAuthId);
		return externalAuthId;
	}

	@Override
	public String toString() {
		return "ExternalAuthHelper[externalAuthServerUrl="
				+ externalAuthServerUrl + ", externalAuthHeaderName="
				+ externalAuthHeaderName + "]";
	}

}
