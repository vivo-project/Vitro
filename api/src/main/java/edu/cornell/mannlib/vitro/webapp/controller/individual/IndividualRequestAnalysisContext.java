/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * Pull the fiddly-bits out of the IndividualRequestAnalyzer to make it easier
 * to test.
 */
public interface IndividualRequestAnalysisContext {

	/**
	 * What is the default namespace for the application?
	 */
	String getDefaultNamespace();

	/**
	 * Use the IndividualDao to get this individual.
	 * 
	 * If the URI is null, or if no such Individual exists, return null.
	 */
	Individual getIndividualByURI(String individualUri);

	/**
	 * If there is a user with this netID, and if they have a profile, return
	 * that Individual. Otherwise, return null.
	 */
	Individual getIndividualByNetId(String netId);

	/**
	 * If this Individual represents a File Bytestream, get the Alias URL
	 * associated with it. Otherwise, return null.
	 */
	String getAliasUrlForBytestreamIndividual(Individual individual);

}
