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
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
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
	 * Find out whether there is a matching property at all.
	 */
	public boolean isConfigured() {
		return selfEditingIdMatchingProperty != null;
	}

	/**
	 * What is the matching property? (might be null).
	 * 
	 * TODO This seems to expose the property unnecessarily, but how else can I
	 * do a SPARQL query for the Individual profiles that don't have matching
	 * property values?
	 */
	public String getMatchingPropertyUri() {
		return selfEditingIdMatchingProperty;
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
	 * matching property. If the externalAuthId is empty or null, it won't match
	 * anything, even though many individuals might have empty matching
	 * properties. Never returns null.
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
		if (externalAuthId.isEmpty()) {
			log.debug("externalAuthId is empty");
			return Collections.emptyList();
		}
		if (selfEditingIdMatchingProperty == null) {
			log.debug("selfEditingMatchingProperty is null");
			return Collections.emptyList();
		}
		return indDao.getIndividualsByDataProperty(
				selfEditingIdMatchingProperty, externalAuthId);
	}

	/**
	 * This Individual, if it exists, should be associated with this
	 * UserAccount.
	 * 
	 * No other Individual should be associated with this UserAccount.
	 */
	public void associateIndividualWithUserAccount(IndividualDao indDao,
			DataPropertyStatementDao dpsDao, UserAccount user,
			String associatedIndividualUri) {
		if (indDao == null) {
			log.warn("No IndividualDao");
			return;
		}
		if (dpsDao == null) {
			log.warn("No DataPropertyStatementDao");
			return;
		}
		if (user == null) {
			log.debug("user is null");
			return;
		}
		if (selfEditingIdMatchingProperty == null) {
			log.error("Can't associate Individual with UserAccount: "
					+ "selfEditingMatchingProperty is null");
			return;
		}

		String externalAuthId = user.getExternalAuthId();
		if (externalAuthId.isEmpty()) {
			log.debug("user has no externalAuthId");
			return;
		}

		// If the Individual exists, clear any previous matching property value
		// and set this one.
		Individual associatedInd = indDao
				.getIndividualByURI(associatedIndividualUri);
		if (associatedInd != null) {
			log.debug("setting the matching property on '"
					+ associatedIndividualUri + "' to '" + externalAuthId + "'");
			dpsDao.deleteDataPropertyStatementsForIndividualByDataProperty(
					associatedIndividualUri, selfEditingIdMatchingProperty);
			dpsDao.insertNewDataPropertyStatement(new DataPropertyStatementImpl(
					associatedIndividualUri, selfEditingIdMatchingProperty,
					externalAuthId));
		}

		// If any other Individuals have this matching property value, remove
		// it.
		for (Individual ind : indDao.getIndividualsByDataProperty(
				selfEditingIdMatchingProperty, externalAuthId)) {
			String indUri = ind.getURI();
			if (!indUri.equals(associatedIndividualUri)) {
				log.debug("clearing the matching property on '" + indUri + "'");
				dpsDao.deleteDataPropertyStatementsForIndividualByDataProperty(
						indUri, selfEditingIdMatchingProperty);
			}
		}
	}

	@Override
	public String toString() {
		return "SelfEditingConfiguration[selfEditingIdMatchingProperty="
				+ selfEditingIdMatchingProperty + "]";
	}

}
