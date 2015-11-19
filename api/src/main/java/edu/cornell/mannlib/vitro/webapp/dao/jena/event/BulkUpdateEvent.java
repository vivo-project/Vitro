/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.event;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class BulkUpdateEvent extends EditEvent {

	private static final String BULK_UPDATE_EVENT = VitroVocabulary.BULK_UPDATE_EVENT;
		
	public BulkUpdateEvent(String userURI, boolean begin) {
		super(userURI, begin);
	}
	
}
