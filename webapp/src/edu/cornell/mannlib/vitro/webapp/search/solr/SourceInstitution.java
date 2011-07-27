/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

public class SourceInstitution implements DocumentModifier {
	
	private String siteURL;
	static VitroSearchTermNames term = new VitroSearchTermNames();
	private String fieldForSiteURL = term.SITE_URL;
	
	public SourceInstitution(String siteURL){
		this.siteURL = siteURL;
	}
	
	@Override
	public void modifyDocument(Individual individual, SolrInputDocument doc,
			StringBuffer addUri) throws SkipIndividualException {
		
		doc.addField(fieldForSiteURL, siteURL);
	}

	@Override
	public void shutdown() {

	}

}
