/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * A base class for the Identifiers created by the
 * CommonIdentifierBundleFactory.
 */
public abstract class AbstractCommonIdentifier {
	protected static <T> Collection<T> getIdentifiersForClass(
			IdentifierBundle ids, Class<T> clazz) {
		Set<T> set = new HashSet<T>();
		for (Identifier id : ids) {
			if (clazz.isAssignableFrom(id.getClass())) {
				set.add(clazz.cast(id));
			}
		}
		return set;
	}
}
