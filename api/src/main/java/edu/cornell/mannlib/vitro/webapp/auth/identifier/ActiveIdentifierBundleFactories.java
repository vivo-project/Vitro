/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

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
	 * Just for diagnostics. Don't expose the factories themselves, only their
	 * names.
	 */
	public static List<String> getFactoryNames(ServletContext ctx) {
		List<String> names = new ArrayList<String>();
		ActiveIdentifierBundleFactories actFact = getActiveFactories(ctx);
		for (IdentifierBundleFactory factory : actFact.factories) {
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
		return getActiveFactories(request).getBundleForRequest(request);
	}

	/**
	 * Get the Identifiers that would be created if this user were to log in.
	 */
	public static IdentifierBundle getUserIdentifierBundle(
			HttpServletRequest request, UserAccount userAccount) {
		return getActiveFactories(request).getBundleForUser(userAccount);
	}

	/**
	 * Get the singleton instance from the servlet context. If there isn't one,
	 * create one. This never returns null.
	 */
	private static ActiveIdentifierBundleFactories getActiveFactories(
			HttpServletRequest req) {
		if (req == null) {
			throw new NullPointerException("req may not be null.");
		}

		return getActiveFactories(req.getSession().getServletContext());
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
	private IdentifierBundle getBundleForRequest(HttpServletRequest request) {
		IdentifierBundle ib = new ArrayIdentifierBundle();
		for (IdentifierBundleFactory ibf : factories) {
			ib.addAll(ibf.getIdentifierBundle(request));
		}
		return ib;
	}

	/**
	 * Get all Identifiers that would be created if this User logged in.
	 */
	private IdentifierBundle getBundleForUser(UserAccount userAccount) {
		IdentifierBundle ib = new ArrayIdentifierBundle();
		for (IdentifierBundleFactory ibf : factories) {
			if (ibf instanceof UserBasedIdentifierBundleFactory) {
				UserBasedIdentifierBundleFactory ubibf = (UserBasedIdentifierBundleFactory) ibf;
				ib.addAll(ubibf.getIdentifierBundleForUser(userAccount));
			}
		}
		return ib;
	}

}
