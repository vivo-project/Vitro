/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Collections;
import java.util.List;

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
		ConfigurationProperties config = ConfigurationProperties
				.getBean(session);
		String selfEditingIdMatchingProperty = config
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

	/**
	 * Get all Individuals associated with this user through the matching
	 * property. Never returns null.
	 */
	public List<Individual> getAssociatedIndividuals(IndividualDao indDao,
			UserAccount user) {
		if (user == null) {
			log.debug("user is null");
			return Collections.emptyList();
		}
		return getAssociatedIndividuals(indDao, user.getExternalAuthId());
	}

	/**
	 * Get all Individuals associated with this externalAuthId through the
	 * matching property. Never returns null.
	 */
	public List<Individual> getAssociatedIndividuals(IndividualDao indDao,
			String externalAuthId) {
		if (indDao == null) {
			log.warn("No IndividualDao");
			return Collections.emptyList();
		}
		if (externalAuthId == null) {
			log.debug("externalAuthId is null");
			return Collections.emptyList();
		}
		if (selfEditingIdMatchingProperty == null) {
			log.debug("selfEditingMatchingProperty is null");
			return Collections.emptyList();
		}
		return indDao.getIndividualsByDataProperty(
				selfEditingIdMatchingProperty, externalAuthId);
	}

	@Override
	public String toString() {
		return "SelfEditingConfiguration[selfEditingIdMatchingProperty="
				+ selfEditingIdMatchingProperty + "]";
	}

}
