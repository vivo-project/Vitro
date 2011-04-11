/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Create Identifiers that are recognized by the common policy family.
 */
public class CommonIdentifierBundleFactory implements IdentifierBundleFactory {
	private static final Log log = LogFactory
			.getLog(CommonIdentifierBundleFactory.class);

	@Override
	public IdentifierBundle getIdentifierBundle(ServletRequest request,
			HttpSession session, ServletContext context) {

		// If this is not an HttpServletRequest, we might as well fail now.
		HttpServletRequest req = (HttpServletRequest) request;

		ArrayIdentifierBundle bundle = new ArrayIdentifierBundle();

		bundle.addAll(determineRoleLevelIdentifiers(req));
		bundle.addAll(determineAssociatedIndividualIdentifiers(req));

		return bundle;
	}

	/**
	 * Create an identifier that shows the role level of the current user, or
	 * PUBLIC if the user is not logged in.
	 */
	private Collection<? extends Identifier> determineRoleLevelIdentifiers(
			HttpServletRequest req) {
		RoleLevel roleLevel = RoleLevel.getRoleFromLoginStatus(req);
		return Collections.singleton(new HasRoleLevel(roleLevel));
	}

	/**
	 * Find all of the individuals that are associated with the current user,
	 * and create an Identifier for each one.
	 */
	private Collection<? extends Identifier> determineAssociatedIndividualIdentifiers(
			HttpServletRequest req) {
		Collection<Identifier> ids = new ArrayList<Identifier>();

		LoginStatusBean bean = LoginStatusBean.getBean(req);
		String username = bean.getUsername();

		if (!bean.isLoggedIn()) {
			log.debug("No SelfEditing: not logged in.");
			return ids;
		}

		if (StringUtils.isEmpty(username)) {
			log.debug("No SelfEditing: username is empty.");
			return ids;
		}

		HttpSession session = req.getSession(false);
		if (session == null) {
			log.debug("No SelfEditing: session is null.");
			return ids;
		}

		ServletContext context = session.getServletContext();
		WebappDaoFactory wdf = (WebappDaoFactory) context
				.getAttribute("webappDaoFactory");
		if (wdf == null) {
			log.error("Could not get a WebappDaoFactory from the ServletContext");
			return ids;
		}

		IndividualDao indDao = wdf.getIndividualDao();

		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(req);
		String uri = sec.getIndividualUriFromUsername(indDao, username);
		if (uri == null) {
			log.debug("Could not find an Individual with a netId of "
					+ username);
			return ids;
		}

		Individual ind = indDao.getIndividualByURI(uri);
		if (ind == null) {
			log.warn("Found a URI for the netId " + username
					+ " but could not build Individual");
			return ids;
		}

		log.debug("Found an Individual for netId " + username + " URI: " + uri);

		// Use the factory method to fill in the Blacklisting reason, if there
		// is one.
		ids.add(HasAssociatedIndividual.getInstance(ind, context));

		return ids;
	}
}
