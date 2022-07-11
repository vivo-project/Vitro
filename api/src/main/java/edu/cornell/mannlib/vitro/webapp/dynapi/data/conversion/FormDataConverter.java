package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public class FormDataConverter {

	public static void convert(HttpServletRequest request, Action action, DataStore dataStore) throws ConversionException {
		Parameters required = action.getRequiredParams();
		Map<String, String[]> received = request.getParameterMap();
		for (String name : required.getNames()) {
			String[] values = received.get(name);
			if (values == null) {
				String message = String.format("Parameter %s not found", name);
				throw new ConversionException(message);
			}
			Parameter param = required.get(name);
			RawData data = DataFactory.create(param, values);
			dataStore.addData(name, data);
		}
	}

}
