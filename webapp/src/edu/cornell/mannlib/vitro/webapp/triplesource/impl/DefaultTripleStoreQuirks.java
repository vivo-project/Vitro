/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.TripleStoreQuirks;

/**
 * The behavior for non-quirky TripleSource implementations.
 */
public class DefaultTripleStoreQuirks implements TripleStoreQuirks {

	@Override
	public boolean hasFileGraphChanged(Model fromFile, Model previous, String graphURI) {
		return !fromFile.isIsomorphicWith(previous);
	}

}
