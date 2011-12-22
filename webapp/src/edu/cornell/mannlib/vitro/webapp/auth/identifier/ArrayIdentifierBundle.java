/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Most common implementation of a List of Identifiers (IdentifierBundle).
 */
public class ArrayIdentifierBundle extends ArrayList<Identifier> implements
		IdentifierBundle {
	public ArrayIdentifierBundle(Collection<? extends Identifier> ids) {
		super(ids);
	}
	
	public ArrayIdentifierBundle(Identifier... ids) {
		this(Arrays.asList(ids));
	}
}
