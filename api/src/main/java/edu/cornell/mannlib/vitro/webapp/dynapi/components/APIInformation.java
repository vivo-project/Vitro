package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.jena.datatypes.RDFDatatype;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class APIInformation {

	private String title;
	private String description;
	private String version;

	public String getTitle() {
		return title;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#title", minOccurs = 1, maxOccurs = 1)
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#description", maxOccurs = 1)
	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#version", minOccurs = 1, maxOccurs = 1)
	public void setVersion(String version) {
		this.version = version;
	}
}
