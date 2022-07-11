package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class JSONConverter {

	private static final Log log = LogFactory.getLog(JSONConverter.class.getName());
	public static void convert(HttpServletRequest request, Action action, DataStore dataStore) {
		ObjectNode schema = createDefaultSchema(action); 
				//TODO: action.getInputSerializedSchema();
		try {
			if (request.getReader() != null && request.getReader().lines() != null) {
			    String jsonString = request.getReader().lines().collect(Collectors.joining());
			    ObjectMapper mapper = new ObjectMapper();
			    JsonNode node = mapper.readTree(jsonString);
			}
		} catch (IOException e) {
			log.error(e,e);
		}
	}
	private static ObjectNode createDefaultSchema(Action action) {
		Parameters params = action.getRequiredParams();
	    ObjectMapper mapper = new ObjectMapper();
	    ObjectNode schema = mapper.createObjectNode();
	    for (String name : params.getNames()) {
		    ObjectNode param = mapper.createObjectNode();
		    schema.set(name, param);
	    }
		return schema;
	}

}
