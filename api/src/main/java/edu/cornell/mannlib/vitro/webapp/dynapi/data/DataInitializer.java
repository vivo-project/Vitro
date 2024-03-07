package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class DataInitializer {

    public static void initialize(Parameter param, Data data) throws ConversionException {

        if (param.isJsonContainer()) {
            initializeJsonObject(param, data);
        } else {
            initializeData(param, data);
        }
    }

    private static void initializeJsonObject(Parameter param, Data data) {
    }

    private static void initializeData(Parameter param, Data data) throws ConversionException {

    }

    private static void initializeArray(Parameter param, Data data) {

    }
}
