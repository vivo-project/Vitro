/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Keep a list of the active IdentifierBundleFactories in the context.
 */
public class ActiveIdentifierBundleFactories {
	private static final String ATTRIBUTE_ACTIVE_FACTORIES = ActiveIdentifierBundleFactories.class
			.getName();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Add a new IdentifierBundleFactory to the list.
	 */
	public static void addFactory(ServletContextEvent sce,
			IdentifierBundleFactory factory) {
		if (sce == null) {
			throw new NullPointerException("sce may not be null.");
		}
		if (factory == null) {
			throw new NullPointerException("factory may not be null.");
		}

		addFactory(sce.getServletContext(), factory);
	}

	/**
	 * Add a new IdentifierBundleFactory to the list.
	 */
	public static void addFactory(ServletContext ctx,
			IdentifierBundleFactory factory) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}
		if (factory == null) {
			throw new NullPointerException("factory may not be null.");
		}

		getActiveFactories(ctx).addFactory(factory);
	}
	
	/**
	 * Just for diagnostics. Don't expose the factories themselves, only their names.
	 */
	public static List<String> getFactoryNames(ServletContext ctx) {
		List<String> names = new ArrayList<String>();
		ActiveIdentifierBundleFactories actFact = getActiveFactories(ctx);
		for (IdentifierBundleFactory factory: actFact.factories) {
			names.add(factory.toString());
		}
		return names;
	}

	/**
	 * Get the Identifiers from the list of factories. This might return an
	 * empty bundle, but it never returns null.
	 * 
	 * This is package access, and should only be called by RequestIdentifiers.
	 * Everyone else should ask RequestIdentifiers to fetch them from the
	 * request.
	 */
	static IdentifierBundle getIdentifierBundle(HttpServletRequest request) {
		HttpSession session = request.getSession();
		ServletContext ctx = session.getServletContext();
		return getActiveFactories(ctx).getIdentifierBundle(request, session,
				ctx);
	}

	/**
	 * Get the singleton instance from the servlet context. If there isn't one,
	 * create one. This never returns null.
	 */
	private static ActiveIdentifierBundleFactories getActiveFactories(
			ServletContext ctx) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}

		Object obj = ctx.getAttribute(ATTRIBUTE_ACTIVE_FACTORIES);
		if (obj == null) {
			obj = new ActiveIdentifierBundleFactories();
			ctx.setAttribute(ATTRIBUTE_ACTIVE_FACTORIES, obj);
		}

		if (!(obj instanceof ActiveIdentifierBundleFactories)) {
			throw new IllegalStateException("Expected to find an instance of "
					+ ActiveIdentifierBundleFactories.class.getName()
					+ " in the context, but found an instance of "
					+ obj.getClass().getName() + " instead.");
		}

		return (ActiveIdentifierBundleFactories) obj;
	}

	// ----------------------------------------------------------------------
	// the instance
	// ----------------------------------------------------------------------

	private final List<IdentifierBundleFactory> factories = new ArrayList<IdentifierBundleFactory>();

	private void addFactory(IdentifierBundleFactory factory) {
		factories.add(factory);
	}

	/**
	 * Run through the active factories and get all Identifiers for this
	 * request.
	 */
	private IdentifierBundle getIdentifierBundle(HttpServletRequest request,
			HttpSession session, ServletContext ctx) {
		IdentifierBundle ib = new ArrayIdentifierBundle();
		for (IdentifierBundleFactory ibf : factories) {
			IdentifierBundle obj = ibf.getIdentifierBundle(request, session,
					ctx);
			if (obj != null) {
				ib.addAll(obj);
			}
		}
		return ib;
	}
}
