/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user has this Individual page as their profile, and has
 * self-editing rights relating to it.
 */
public class HasProfile extends HasAssociatedIndividual {
	private static Collection<HasProfile> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, HasProfile.class);
	}

	public static Collection<String> getProfileUris(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (HasProfile id : getIdentifiers(ids)) {
			set.add(id.getAssociatedIndividualUri());
		}
		return set;
	}

	public HasProfile(String associatedIndividualUri) {
		super(associatedIndividualUri);
	}

	public String getProfileUri() {
		return getAssociatedIndividualUri();
	}

	@Override
	public String toString() {
		return "HasProfile[" + getAssociatedIndividualUri() + "]";
	}

}
