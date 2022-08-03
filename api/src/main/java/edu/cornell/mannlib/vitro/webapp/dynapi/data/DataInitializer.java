package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class DataInitializer {
	
	public static void initialize(Parameter param, RawData data) throws ConversionException {
		
		if (param.isArray()) {
			initializeArray(param, data);
		} else if (param.isJsonObject()){
			initializeJsonObject(param, data);
		} else {
			initializeData(param, data);
		}
	}

	private static void initializeJsonObject(Parameter param, RawData data) {
	}

	private static void initializeData(Parameter param, RawData data) throws ConversionException {
		
	}

	private static void initializeArray(Parameter param, RawData data) {
		
	}
}
