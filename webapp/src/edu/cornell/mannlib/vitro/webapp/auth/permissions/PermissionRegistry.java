/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds a map of known Permission objects by URI. Resides in the
 * ServletContext.
 * 
 * This is not thread-safe, so all Permissions should be added during context
 * initialization.
 */
public class PermissionRegistry {
	private static final Log log = LogFactory.getLog(PermissionRegistry.class);

	private static final String ATTRIBUTE_NAME = PermissionRegistry.class
			.getName();

	/**
	 * Get the registry from the context. If the context doesn't contain a
	 * registry yet, create one.
	 */
	public static PermissionRegistry getRegistry(ServletContext ctx) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}

		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof PermissionRegistry) {
			return (PermissionRegistry) o;
		}
		if (o != null) {
			log.error("Error: PermissionRegistry was set to an "
					+ "invalid object: " + o);
		}

		PermissionRegistry registry = new PermissionRegistry();
		ctx.setAttribute(ATTRIBUTE_NAME, registry);
		return registry;
	}

	private final Map<String, Permission> permissionsMap = new HashMap<String, Permission>();

	private PermissionRegistry() {
		// nothing to initialize;
	}

	/**
	 * Add a Permission to the registry. If a Permission with the same URI is
	 * already present, throw an IllegalStateException.
	 */
	public void addPermission(Permission p) {
		if (p == null) {
			throw new NullPointerException("p may not be null.");
		}

		String uri = p.getUri();
		if (isPermission(uri)) {
			throw new IllegalStateException(
					"A Permission is already registered with this URI: '" + uri
							+ "'.");
		}

		permissionsMap.put(uri, p);
	}

	/**
	 * Is there already a Permission registered with this URI?
	 */
	public boolean isPermission(String uri) {
		return permissionsMap.containsKey(uri);
	}

	/**
	 * Get the permission that is registered with this URI. If there is no such
	 * Permission, return a dummy Permission that always denies authorization.
	 * 
	 * If you want to know whether an actual Permission has been registered at
	 * this URI, call isPermission() instead.
	 */
	public Permission getPermission(String uri) {
		Permission p = permissionsMap.get(uri);
		return (p == null) ? Permission.NOT_AUTHORIZED : p;
	}

}
