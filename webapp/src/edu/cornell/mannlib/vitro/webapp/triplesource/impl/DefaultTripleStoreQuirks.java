/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.TripleStoreQuirks;

/**
 * The behavior for non-quirky TripleSource implementations.
 */
public class DefaultTripleStoreQuirks implements TripleStoreQuirks {

	@Override
	public boolean hasFileGraphChanged(Model fromFile, Model previous,
			String graphURI) {
		/**
		 * The test for isomorphism involves a large number of ASK queries. It
		 * appears to be faster to read the previous graph data into memory than
		 * to run those queries against the RDFService.
		 */
		return !fromFile.isIsomorphicWith(ModelFactory.createDefaultModel()
				.add(previous));
	}

}
