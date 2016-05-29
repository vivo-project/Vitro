/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class EditEvent {
	
    private static final String EDIT_EVENT = VitroVocabulary.EDIT_EVENT;
    private static final String EDIT_EVENT_AGENT = VitroVocabulary.EDIT_EVENT_AGENT;

    private Boolean begin;
	private String userURI;
    protected List<String> eventTypeURIList;
    protected Map<String,List<RDFNode>> propertyMap;
	
	public EditEvent (String userURI, boolean begin) {
		this.begin = begin;
		this.userURI = userURI;
	}
	
	public Map<String,List<RDFNode>> getPropertyMap() {
		this.propertyMap = new HashMap<String,List<RDFNode>>();
		List<RDFNode> agentValueList = new LinkedList<RDFNode>();
		agentValueList.add(ResourceFactory.createResource(userURI));
		this.propertyMap.put(EDIT_EVENT_AGENT, agentValueList);
		return propertyMap;
	}
	
	public Boolean getBegin() {
		return this.begin;
	}
	
	public List<String> getEventTypeURIList() {
		return eventTypeURIList;
	}
	
}
