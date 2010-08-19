/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class IndividualEditEvent extends EditEvent {

	private static final String INDIVIDUAL_EDIT_EVENT = VitroVocabulary.INDIVIDUAL_EDIT_EVENT;
	private static final String EDITED_INDIVIDUAL = VitroVocabulary.EDITED_INDIVIDUAL;
	
	private String individualURI;
	
	public IndividualEditEvent(String userURI, boolean begin, String individualURI) {
		super(userURI, begin);
		this.individualURI = individualURI;
	}
	
	public String getIndividualURI() {
		return individualURI;
	}

	public Map<String,List<RDFNode>> getPropertyMap() {
		Map<String,List<RDFNode>> map = super.getPropertyMap();
		List<RDFNode> valueList = new LinkedList<RDFNode>();
		valueList.add(ResourceFactory.createResource(individualURI));
		map.put(EDITED_INDIVIDUAL, valueList);
		List<RDFNode> typeValueList = map.get(RDF.type.getURI());
		if (typeValueList == null) {
			typeValueList = new LinkedList<RDFNode>();
		}
		typeValueList.add(ResourceFactory.createResource(INDIVIDUAL_EDIT_EVENT));
		map.put(RDF.type.getURI(),typeValueList);
		return map;	
	}
	
}
