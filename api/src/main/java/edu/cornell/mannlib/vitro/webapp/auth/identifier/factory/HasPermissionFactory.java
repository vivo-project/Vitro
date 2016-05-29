/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionRegistry;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Figure out what Permissions the user is entitled to have.
 */
public class HasPermissionFactory extends BaseUserBasedIdentifierBundleFactory {
	private static final Log log = LogFactory
			.getLog(HasPermissionFactory.class);

	public HasPermissionFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
		if (user == null) {
			return createPublicPermissions();
		} else {
			return createUserPermissions(user);
		}
	}

	private IdentifierBundle createPublicPermissions() {
		Set<String> permissionUris = new HashSet<String>();
		for (PermissionSet ps : uaDao.getAllPermissionSets()) {
			if (ps.isForPublic()) {
				permissionUris.addAll(ps.getPermissionUris());
			}
		}
		log.debug("Permission URIs: " + permissionUris);

		return new ArrayIdentifierBundle(
				getIdentifiersFromPermissions(getPermissionsForUris(permissionUris)));
	}

	private IdentifierBundle createUserPermissions(UserAccount user) {
		Set<String> permissionUris = new HashSet<String>();
		for (String psUri : user.getPermissionSetUris()) {
			PermissionSet ps = uaDao.getPermissionSetByUri(psUri);
			if (ps != null) {
				permissionUris.addAll(ps.getPermissionUris());
			}
		}
		log.debug("user permission sets: " + user.getPermissionSetUris());
		log.debug("Permission URIs: " + permissionUris);

		return new ArrayIdentifierBundle(
				getIdentifiersFromPermissions(getPermissionsForUris(permissionUris)));
	}

	private Collection<Permission> getPermissionsForUris(
			Collection<String> permissionUris) {
		List<Permission> permissions = new ArrayList<Permission>();
		PermissionRegistry registry = PermissionRegistry.getRegistry(ctx);
		for (String uri : permissionUris) {
			permissions.add(registry.getPermission(uri));
		}
		return permissions;
	}

	private List<HasPermission> getIdentifiersFromPermissions(
			Collection<Permission> permissions) {
		List<HasPermission> ids = new ArrayList<HasPermission>();
		for (Permission permission : permissions) {
			ids.add(new HasPermission(permission));
		}
		return ids;
	}
}
