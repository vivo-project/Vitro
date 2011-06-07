/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * Holds the configuration properties used in Self-Editing, and some commonly
 * used methods on those properties.
 */
public class SelfEditingConfiguration {
	private static final Log log = LogFactory
			.getLog(SelfEditingConfiguration.class);

	private static final String BEAN_ATTRIBUTE = SelfEditingConfiguration.class
			.getName();

	/**
	 * This configuration property tells us which data property on the
	 * Individual is used to associate it with a net ID.
	 */
	private static final String PROPERTY_SELF_EDITING_ID_MATCHING_PROPERTY = "selfEditing.idMatchingProperty";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If there is no session, create a bean on the fly. If there is a session,
	 * get the existing bean, or create one and store it for re-use.
	 * 
	 * Never returns null.
	 */
	public static SelfEditingConfiguration getBean(ServletRequest request) {
		if (!(request instanceof HttpServletRequest)) {
			log.error("Not an HttpServletRequest: " + request);
			return new SelfEditingConfiguration(null);
		}

		HttpSession session = ((HttpServletRequest) request).getSession(false);
		if (session == null) {
			log.trace("No session; no need to create one.");
			return new SelfEditingConfiguration(null);
		}

		Object attr = session.getAttribute(BEAN_ATTRIBUTE);
		if (attr instanceof SelfEditingConfiguration) {
			log.trace("Found a bean: " + attr);
			return (SelfEditingConfiguration) attr;
		}

		SelfEditingConfiguration bean = buildBean(session);
		log.debug("Created a bean: " + bean);
		session.setAttribute(BEAN_ATTRIBUTE, bean);
		return bean;
	}

	private static SelfEditingConfiguration buildBean(HttpSession session) {
		String selfEditingIdMatchingProperty = ConfigurationProperties.getBean(session)
				.getProperty(PROPERTY_SELF_EDITING_ID_MATCHING_PROPERTY);
		return new SelfEditingConfiguration(selfEditingIdMatchingProperty);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final String selfEditingIdMatchingProperty;

	public SelfEditingConfiguration(String selfEditingIdMatchingProperty) {
		this.selfEditingIdMatchingProperty = trimThis(selfEditingIdMatchingProperty);
	}

	private String trimThis(String string) {
		if (string == null) {
			return null;
		} else {
			return string.trim();
		}
	}

	// TODO JB This should move to UserAccountsDao.
	public String getIndividualUriFromUsername(IndividualDao indDao,
			String username) {
		if (indDao == null) {
			log.warn("No IndividualDao");
			return null;
		}
		if (username == null) {
			log.debug("username is null");
			return null;
		}
		if (selfEditingIdMatchingProperty == null) {
			log.debug("selfEditingMatchingProperty is null");
			return null;
		}

		String uri = indDao.getIndividualURIFromNetId(username,
				selfEditingIdMatchingProperty);
		log.debug("Username=" + username + ", individual URI=" + uri);
		return uri;
	}

	@Override
	public String toString() {
		return "SelfEditingConfiguration[selfEditingIdMatchingProperty="
				+ selfEditingIdMatchingProperty + "]";
	}

}
