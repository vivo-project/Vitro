/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user is associated with this Individual page, and has editing
 * rights relating to it.
 * 
 * Subclasses exist to indicate how that association is created, as either a
 * Self-Editor or a Proxy Editor. In some cases (e.g., the MyProfile link) the
 * distinction is important.
 */
public abstract class HasAssociatedIndividual extends AbstractCommonIdentifier
		implements Identifier {

	private static Collection<HasAssociatedIndividual> getIdentifiers(
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

	private final String associatedIndividualUri;

	public HasAssociatedIndividual(String associatedIndividualUri) {
		this.associatedIndividualUri = associatedIndividualUri;
	}

	public String getAssociatedIndividualUri() {
		return associatedIndividualUri;
	}
}
