/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * An immutable key for storing and retrieving OntModels.
 * 
 * In addition to the usual options, it has a name, which adds to the
 * uniqueness.
 */
public final class OntModelKey extends ModelAccessKey {
	private final String name;
	private final int hashCode;

	public OntModelKey(String name, LanguageOption... options) {
		super(findLanguageOption(options));
		this.name = name;
		this.hashCode = super.hashCode() ^ name.hashCode();
	}

	public String getName() {
		return name;
	}

	@Override
	public LanguageOption getLanguageOption() {
		return super.getLanguageOption();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && ((OntModelKey) obj).name.equals(this.name);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return super.toString() + " " + ToString.modelName(name);
	}
	
}
