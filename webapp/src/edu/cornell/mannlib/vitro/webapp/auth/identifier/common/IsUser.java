/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user has this URI.
 */
public class IsUser extends AbstractCommonIdentifier implements Identifier {
	public static Collection<IsUser> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, IsUser.class);
	}

	public static Collection<String> getUserUris(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (IsUser id : getIdentifiers(ids)) {
			set.add(id.getUri());
		}
		return set;
	}

	private final String uri;

	public IsUser(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "IsUser[" + uri + "]";
	}
}
