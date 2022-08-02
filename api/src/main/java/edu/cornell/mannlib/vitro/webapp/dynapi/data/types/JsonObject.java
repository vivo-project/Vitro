package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class JsonObject {

	private Configuration jsonPathConfig;
	private DocumentContext ctx;
	
	public JsonObject(String rawString) {
		ctx = JsonPath.using(jsonPathConfig).parse(rawString);
	}
	
}
