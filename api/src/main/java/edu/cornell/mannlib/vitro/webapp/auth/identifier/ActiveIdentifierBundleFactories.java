/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Keep a list of the active IdentifierBundleFactories in the context.
 */
public class ActiveIdentifierBundleFactories {
    
    private static ActiveIdentifierBundleFactories INSTANCE = new ActiveIdentifierBundleFactories();
    
    private final List<IdentifierBundleFactory> factories = new ArrayList<IdentifierBundleFactory>();

    private ActiveIdentifierBundleFactories() {
        INSTANCE = this;
    }
    
    public static ActiveIdentifierBundleFactories getInstance() {
        return INSTANCE;
    }
    
	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Add a new IdentifierBundleFactory to the list.
	 */
	public static void addFactory(IdentifierBundleFactory factory) {
		if (factory == null) {
			throw new NullPointerException("factory may not be null.");
		}
		INSTANCE.factories.add(factory);
	}

	/**
	 * Just for diagnostics. Don't expose the factories themselves, only their
	 * names.
	 */
	public static List<String> getFactoryNames() {
		List<String> names = new ArrayList<String>();
		ActiveIdentifierBundleFactories actFact = ActiveIdentifierBundleFactories.getInstance();
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
		return ActiveIdentifierBundleFactories.getInstance().getBundleForRequest(request);
	}

	/**
	 * Get the Identifiers that would be created if this user were to log in.
	 */
	public static IdentifierBundle getUserIdentifierBundle(UserAccount userAccount) {
		return ActiveIdentifierBundleFactories.getInstance().getBundleForUser(userAccount);
	}

	// ----------------------------------------------------------------------
	// the instance
	// ----------------------------------------------------------------------

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
