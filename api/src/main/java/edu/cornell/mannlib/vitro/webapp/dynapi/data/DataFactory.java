package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.ArrayList;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class DataFactory {

	public static RawData create(Parameter param, String[] values) throws ConversionException {
		
		if (param.isArray()) {
			return createArray(param, values);
		} else {
			return createData(param, values);
		}
	}

	private static RawData createData(Parameter param, String[] values) throws ConversionException {
		if (values.length > 1) {
			String message = String.format("Found %s1 values for param %s2", values.length, param.getName());
			throw new ConversionException(message);
		}
		RawData data = new RawData(param);
		data.setRawString(values[0]);
		return null;
	}

	private static RawData createArray(Parameter param, String[] values) {
		RawData data = new RawData(param);
		ArrayList<Object> list = new ArrayList<>(values.length);
		for (String value : values) {
			list.add(value);
		}
		data.setObject(list);
		return data;
	}

}
