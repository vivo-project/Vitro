package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public class FormDataConverter {

	private static ObjectMapper mapper = new ObjectMapper();


	public static void convert(HttpServletRequest request, Action action, DataStore dataStore) throws ConversionException {
		Parameters required = action.getRequiredParams();
		Map<String, String[]> received = request.getParameterMap();
		for (String name : required.getNames()) {
			String[] values = received.get(name);
			if (values == null || values.length == 0) {
				String message = String.format("Parameter %s not found", name);
				throw new ConversionException(message);
			}
			Parameter param = required.get(name);
			
			if (param.isArray()) {
				readArray(dataStore, name, values, param);
			} else {
				readParam(dataStore, name, values, param);
			}
		}
	}

	private static void readParam(DataStore dataStore, String name, String[] values, Parameter param) {
		RawData data = new RawData(param);
		data.setRawString(values[0]);
		dataStore.addData(name, data);
	}

	private static void readArray(DataStore dataStore, String name, String[] values, Parameter param) {
		RawData data = new RawData(param);
		ArrayNode node = mapper.createArrayNode();
		for (int i = 0; i< values.length;i++) {
			node.add(values[i]);
		}
		data.setRawString(node.toString());
		dataStore.addData(name, data);
	}

	public static void convert(HttpServletResponse response, Action action, OperationData operationData,
			DataStore dataStore) {
		// TODO Auto-generated method stub
		
	}

}
