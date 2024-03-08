package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class HTTPMethod {
	
 	private static final Log log = LogFactory.getLog(HTTPMethod.class);
	String name;

	public String getName() {
		return name;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#methodName", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}
}
