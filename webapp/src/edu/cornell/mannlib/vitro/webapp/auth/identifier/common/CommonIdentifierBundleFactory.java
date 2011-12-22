/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionRegistry;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Create Identifiers that are recognized by the common policy family.
 */
public class CommonIdentifierBundleFactory implements IdentifierBundleFactory {
	private static final Log log = LogFactory
			.getLog(CommonIdentifierBundleFactory.class);

	private final ServletContext context;

	public CommonIdentifierBundleFactory(ServletContext context) {
		this.context = context;
	}

	@Override
	public IdentifierBundle getIdentifierBundle(ServletRequest request,
			HttpSession session, ServletContext unusedContext) {

		// If this is not an HttpServletRequest, we might as well fail now.
		HttpServletRequest req = (HttpServletRequest) request;

		ArrayIdentifierBundle bundle = new ArrayIdentifierBundle();

		bundle.addAll(createUserIdentifiers(req));
		bundle.addAll(createRootUserIdentifiers(req));
		bundle.addAll(createRoleLevelIdentifiers(req));
		bundle.addAll(createBlacklistOrAssociatedIndividualIdentifiers(req));
		bundle.addAll(createExplicitProxyEditingIdentifiers(req));
		bundle.addAll(createPermissionIdentifiers(req));

		return bundle;
	}

	/**
	 * If the user is logged in, create an identifier that shows his URI.
	 */
	private Collection<? extends Identifier> createUserIdentifiers(
			HttpServletRequest req) {
		LoginStatusBean bean = LoginStatusBean.getBean(req);
		if (bean.isLoggedIn()) {
			return Collections.singleton(new IsUser(bean.getUserURI()));
		} else {
			return Collections.emptySet();
		}
	}

	private Collection<? extends Identifier> createRootUserIdentifiers(
			HttpServletRequest req) {
		UserAccount user = LoginStatusBean.getCurrentUser(req);
		if ((user != null) && user.isRootUser()) {
			return Collections.singleton(new IsRootUser());
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Create an identifier that shows the role level of the current user, or
	 * PUBLIC if the user is not logged in.
	 */
	private Collection<? extends Identifier> createRoleLevelIdentifiers(
			HttpServletRequest req) {
		RoleLevel roleLevel = RoleLevel.getRoleFromLoginStatus(req);
		return Collections.singleton(new HasRoleLevel(roleLevel));
	}

	/**
	 * Find all of the individuals that are associated with the current user,
	 * and create either an IsBlacklisted or HasAssociatedIndividual for each
	 * one.
	 */
	private Collection<? extends Identifier> createBlacklistOrAssociatedIndividualIdentifiers(
			HttpServletRequest req) {
		Collection<Identifier> ids = new ArrayList<Identifier>();

		for (Individual ind : getAssociatedIndividuals(req)) {
			// If they are blacklisted, this factory will return an identifier
			Identifier id = IsBlacklisted.getInstance(ind, context);
			if (id != null) {
				ids.add(id);
			} else {
				ids.add(new HasProfile(ind.getURI()));
			}
		}

		return ids;
	}

	/**
	 * Get all Individuals associated with the current user as SELF.
	 */
	private Collection<Individual> getAssociatedIndividuals(
			HttpServletRequest req) {
		Collection<Individual> individuals = new ArrayList<Individual>();

		UserAccount user = LoginStatusBean.getCurrentUser(req);
		if (user == null) {
			log.debug("No Associated Individuals: not logged in.");
			return individuals;
		}

		WebappDaoFactory wdf = (WebappDaoFactory) context
				.getAttribute("webappDaoFactory");
		if (wdf == null) {
			log.error("Could not get a WebappDaoFactory from the ServletContext");
			return individuals;
		}

		IndividualDao indDao = wdf.getIndividualDao();

		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(req);
		individuals.addAll(sec.getAssociatedIndividuals(indDao, user));

		return individuals;
	}

	/**
	 * Get all Individuals associated with the current user by explicit proxy
	 * relationship.
	 */
	private Collection<? extends Identifier> createExplicitProxyEditingIdentifiers(
			HttpServletRequest req) {
		Collection<Identifier> ids = new ArrayList<Identifier>();

		UserAccount user = LoginStatusBean.getCurrentUser(req);
		if (user != null) {
			for (String proxiedUri : user.getProxiedIndividualUris()) {
				ids.add(new HasProxyEditingRights(proxiedUri));
			}
		}

		return ids;
	}

	/**
	 * Create an identifier for each Permission that the User has.
	 */
	private Collection<? extends Identifier> createPermissionIdentifiers(
			HttpServletRequest req) {
		UserAccount user = LoginStatusBean.getCurrentUser(req);
		if (user == null) {
			return createPublicPermissions();
		} else {
			return createUserPermissions(user);
		}
	}

	private Collection<? extends Identifier> createPublicPermissions() {
		Collection<Identifier> ids = new ArrayList<Identifier>();

		WebappDaoFactory wdf = (WebappDaoFactory) context
				.getAttribute("webappDaoFactory");
		if (wdf == null) {
			log.error("Could not get a WebappDaoFactory from the ServletContext");
			return ids;
		}

		UserAccountsDao uaDao = wdf.getUserAccountsDao();

		Set<String> permissionUris = new HashSet<String>();
		for (PermissionSet ps : uaDao.getAllPermissionSets()) {
			if (ps.isForPublic()) {
				permissionUris.addAll(ps.getPermissionUris());
			}
		}

		PermissionRegistry registry = PermissionRegistry.getRegistry(context);
		for (String permissionUri : permissionUris) {
			Permission permission = registry.getPermission(permissionUri);
			ids.add(new HasPermission(permission));
		}

		return ids;
	}

	private Collection<? extends Identifier> createUserPermissions(UserAccount user) {
		Collection<Identifier> ids = new ArrayList<Identifier>();

		WebappDaoFactory wdf = (WebappDaoFactory) context
				.getAttribute("webappDaoFactory");
		if (wdf == null) {
			log.error("Could not get a WebappDaoFactory from the ServletContext");
			return ids;
		}

		UserAccountsDao uaDao = wdf.getUserAccountsDao();

		Set<String> permissionUris = new HashSet<String>();
		for (String psUri : user.getPermissionSetUris()) {
			PermissionSet ps = uaDao.getPermissionSetByUri(psUri);
			if (ps != null) {
				permissionUris.addAll(ps.getPermissionUris());
			}
		}

		PermissionRegistry registry = PermissionRegistry.getRegistry(context);
		for (String permissionUri : permissionUris) {
			Permission permission = registry.getPermission(permissionUri);
			ids.add(new HasPermission(permission));
		}

		return ids;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " - " + hashCode();
	}

}
