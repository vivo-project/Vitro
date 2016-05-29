/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.event;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public class IndividualCreationEvent extends IndividualEditEvent {
	
	private static final String INDIVIDUAL_CREATION_EVENT = VitroVocabulary.INDIVIDUAL_CREATION_EVENT; 
	
	public IndividualCreationEvent(String userURI, boolean begin, String individualURI) {
		super(userURI,begin,individualURI);
	}
	
    public Map<String,List<RDFNode>> getPropertyMap() {
		Map<String,List<RDFNode>> map = super.getPropertyMap();
		List<RDFNode> typeValueList = map.get(RDF.type.getURI());
		if (typeValueList == null) {
			typeValueList = new LinkedList<RDFNode>();
		}
		typeValueList.add(ResourceFactory.createResource(INDIVIDUAL_CREATION_EVENT));
		map.put(RDF.type.getURI(),typeValueList);
		return map;	
	}
}
