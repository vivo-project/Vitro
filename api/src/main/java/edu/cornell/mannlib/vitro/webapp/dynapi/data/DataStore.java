package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.entity.ContentType;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.LangTag;

public class DataStore {

	protected Map<String, RawData> dataMap = new HashMap<>();
	private ContentType responseType = ContentType.APPLICATION_JSON;
	private Set<LangTag> acceptLangs = new HashSet<>();
	private String resourceId = "";


	public DataStore() {
	}

	public void addData(String name, RawData data) {
        dataMap.put(name, data);
    }

	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceID(String resourceId) {
		this.resourceId  = resourceId;
	}

	public ContentType getResponseType() {
		return responseType;	
	}
	
	public void setResponseType(ContentType contentType) {
		this.responseType = contentType;	
	}

	public void setAcceptLangs(Set<LangTag> acceptLangs) {
		this.acceptLangs.addAll(acceptLangs);
	}

}
