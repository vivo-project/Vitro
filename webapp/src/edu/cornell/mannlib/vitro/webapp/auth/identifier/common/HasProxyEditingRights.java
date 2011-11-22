/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user has a Proxy relationship to this Individual page, and has
 * proxy editing rights relating to it.
 */
public class HasProxyEditingRights extends HasAssociatedIndividual {
	private static Collection<HasProxyEditingRights> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, HasProxyEditingRights.class);
	}

	public static Collection<String> getProxiedPageUris(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (HasProxyEditingRights id : getIdentifiers(ids)) {
			set.add(id.getAssociatedIndividualUri());
		}
		return set;
	}

	public HasProxyEditingRights(String associatedIndividualUri) {
		super(associatedIndividualUri);
	}

	public String getProxiedPageUri() {
		return getAssociatedIndividualUri();
	}

	@Override
	public String toString() {
		return "HasProxyEditingRights[" + getAssociatedIndividualUri() + "]";
	}

}
