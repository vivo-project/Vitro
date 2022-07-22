package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.entity.ContentType;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.LangTag;

public class DataStore {

	protected Map<String, RawData> dataMap = new HashMap<>();
	private Set<ContentType> acceptTypes = new HashSet<>();
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

	public void setAcceptedContentTypes(Set<ContentType> acceptTypes, ContentType contentType) {
		if (acceptTypes.isEmpty()) {
			this.acceptTypes.add(contentType);
		} else {
			this.acceptTypes.addAll(acceptTypes);	
		}
	}

	public void setAcceptLangs(Set<LangTag> acceptLangs) {
		this.acceptLangs.addAll(acceptLangs);
	}

}
