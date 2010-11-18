/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * Capture the properties used by the External Authorization system, and use
 * them in common ways.
 * 
 * The first time this bean is requested, it is created from the configuration
 * properties and cached in the session. After that, the cached version is used.
 */
public class ExternalAuthHelper {
	private static final Log log = LogFactory.getLog(ExternalAuthHelper.class);

	private static final ExternalAuthHelper DUMMY_HELPER = new ExternalAuthHelper(
			null);

	private static final String BEAN_ATTRIBUTE = ExternalAuthHelper.class
			.getName();

	/**
	 * The configuration property that tells us what property associates an
	 * Individual with a NetID
	 */
	private static final String PROPERTY_NETID_MATCHING_PROPERTY = "externalAuth.netidMatchingProperty";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If there is no session, there is no bean. If there is a session and no
	 * bean, create one.
	 * 
	 * Never returns null.
	 */
	public static ExternalAuthHelper getBean(ServletRequest request) {
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
		// TODO the ConfigurationProperties should be attached to the
		// ServletContext.
		String netidMatchingPropertyUri = ConfigurationProperties
				.getProperty(PROPERTY_NETID_MATCHING_PROPERTY);
		return new ExternalAuthHelper(netidMatchingPropertyUri);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final String netidMatchingPropertyUri;

	public ExternalAuthHelper(String netidMatchingPropertyUri) {
		if (netidMatchingPropertyUri == null) {
			this.netidMatchingPropertyUri = null;
		} else {
			this.netidMatchingPropertyUri = netidMatchingPropertyUri.trim();
		}
	}

	public String getIndividualUriFromNetId(IndividualDao indDao, String netId) {
		if (indDao == null) {
			return null;
		}
		if (netId == null) {
			return null;
		}
		if (netidMatchingPropertyUri == null) {
			return null;
		}
		
		String uri = indDao.getIndividualURIFromNetId(netId,
				netidMatchingPropertyUri);
		log.debug("Netid =" + netId + ", individual URI=" + uri);
		return uri;
	}

	@Override
	public String toString() {
		return "ExternalAuthHelper[netidMatchingPropertyUri="
				+ netidMatchingPropertyUri + "]";
	}

}
