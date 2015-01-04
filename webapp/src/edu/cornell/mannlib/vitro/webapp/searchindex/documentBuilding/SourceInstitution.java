/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

public class SourceInstitution implements DocumentModifier {

	private String siteURL;
	private String siteName;

	static VitroSearchTermNames term = new VitroSearchTermNames();
	private String fieldForSiteURL = term.SITE_URL;
	private String fieldForSiteName = term.SITE_NAME;

	public SourceInstitution(String siteURL, String siteName) {
		this.siteURL = siteURL;
		this.siteName = siteName;
	}

	@Override
	public void modifyDocument(Individual individual, SearchInputDocument doc) {

		doc.addField(VitroSearchTermNames.SITE_URL, siteURL);
		doc.addField(VitroSearchTermNames.SITE_NAME, siteURL);

		doc.addField(fieldForSiteName, siteName);
	}

	@Override
	public void shutdown() {

	}

}
