/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * A base class for the Identifiers created by the
 * CommonIdentifierBundleFactory.
 */
public abstract class AbstractCommonIdentifier {

	/**
	 * Get all of the instances of this class of Identifier from this bundle.
	 */
	protected static <T extends Identifier> Collection<T> getIdentifiersForClass(
			IdentifierBundle ids, Class<T> clazz) {
		if ((ids == null) || (clazz == null)) {
			return Collections.emptySet();
		}

		Set<T> set = new HashSet<T>();
		for (Identifier id : ids) {
			if (clazz.isAssignableFrom(id.getClass())) {
				set.add(clazz.cast(id));
			}
		}
		return set;
	}
}
