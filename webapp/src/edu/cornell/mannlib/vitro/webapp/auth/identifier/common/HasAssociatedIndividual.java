/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user is associated with this Individual.
 * 
 * This includes a thick factory method that will look through a directory of
 * files to determine whether the associated individual is blacklisted.
 * 
 * The mechanism is only to record how this was created, for diagnostic purposes.
 */
public class HasAssociatedIndividual extends AbstractCommonIdentifier implements
		Identifier {

	public static Collection<HasAssociatedIndividual> getIdentifiers(
			IdentifierBundle ids) {
		return getIdentifiersForClass(ids, HasAssociatedIndividual.class);
	}

	public static Collection<String> getIndividualUris(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (HasAssociatedIndividual id : getIdentifiers(ids)) {
			set.add(id.getAssociatedIndividualUri());
		}
		return set;
	}
	
	public enum Mechanism {SELF, EXPLICIT_PROXY}

	private final String associatedIndividualUri;
	private final Mechanism mechanism;

	public HasAssociatedIndividual(String associatedIndividualUri, Mechanism mechanism) {
		this.associatedIndividualUri = associatedIndividualUri;
		this.mechanism = mechanism;
	}

	public String getAssociatedIndividualUri() {
		return associatedIndividualUri;
	}

	@Override
	public String toString() {
		return "HasAssociatedIndividual[" + associatedIndividualUri + " (" + mechanism +
				")]";
	}
}
