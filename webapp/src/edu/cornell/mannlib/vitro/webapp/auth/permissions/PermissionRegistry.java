/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import javax.servlet.ServletContext;

/**
 * Holds a map of known Permission objects by URI. Resides in the
 * ServletContext.
 */
public class PermissionRegistry {
	/**
	 * Get the registry from the context. If the context doesn't contain a
	 * registry yet, write a warning and return an immutable registry with no
	 * permissions.
	 */
	public static PermissionRegistry getRegistry(ServletContext ctx) {
		throw new RuntimeException(
				"PermissionRegistry.getBean not implemented.");
	}

	/**
	 * Create an empty registry and set it into the context. This should only be
	 * called from PermissionSetsLoader.
	 */
	protected static void setRegistry(ServletContext ctx,
			PermissionRegistry registry) {
		throw new RuntimeException(
				"PermissionRegistry.setRegistry not implemented.");
	}

	/**
	 * Add a Permission to the registry. If a Permission with the same URI is
	 * already present, throw an IllegalStateException.
	 */
	public void addPermission(Permission p) {
		throw new RuntimeException(
				"PermissionRegistry.addPermission not implemented.");
	}

	/**
	 * Is there already a Permission registered with this URI?
	 */
	public boolean isPermission(String uri) {
		throw new RuntimeException(
				"PermissionRegistry.isPermission not implemented.");
	}

	/**
	 * Get the permission that is registered with this URI. If there is no such
	 * Permission, return a dummy Permission that always denies authorization.
	 */
	public Permission getPermission(String uri) {
		throw new RuntimeException(
				"PermissionRegistry.getPermission not implemented.");
	}
}
