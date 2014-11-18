/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.tripleSource;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * TODO
 */
public interface TripleStoreQuirks {

	/**
	 * Test to see whether the FileGraph must be updated to reflect the current
	 * state of the file.
	 */
	boolean hasFileGraphChanged(Model fromFile, Model previous, String graphURI);

}
