/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Create a new profile with the given VClass URI, and info from the user account.
 */
public class UserAccountsProfileCreator {
	private static final String URI_FOAF_FIRST_NAME = "http://xmlns.com/foaf/0.1/firstName";
	private static final String URI_FOAF_LAST_NAME = "http://xmlns.com/foaf/0.1/lastName";

	public static String createProfile(IndividualDao indDao,
			DataPropertyStatementDao dpsDao, String profileClassUri,
			UserAccount account) throws InsertException {
		IndividualImpl i = new IndividualImpl();
		i.setVClassURI(profileClassUri);
		String indUri = indDao.insertNewIndividual(i);

		addProp(dpsDao, indUri, URI_FOAF_FIRST_NAME, account.getFirstName());
		addProp(dpsDao, indUri, URI_FOAF_LAST_NAME, account.getLastName());

		String label = account.getLastName() + ", " + account.getFirstName();
		addProp(dpsDao, indUri, VitroVocabulary.LABEL, label);
		
		return indUri;
	}

	private static void addProp(DataPropertyStatementDao dpsDao, String indUri,
			String propertyUri, String value) {
		DataPropertyStatementImpl dps = new DataPropertyStatementImpl(indUri,
				propertyUri, value);
		dpsDao.insertNewDataPropertyStatement(dps);
	}
}
