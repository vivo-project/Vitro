package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.*;


import java.net.URI;
import java.net.URISyntaxException;

public class IOMessageConverterUtils {

    public static boolean isInteger(String value) {
        try {
            int i = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String value) {
        try {
            double d = Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isBoolean(String value) {
        return (value!=null) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
    }

    public static boolean isURI(String value) {
        try {
            new URI(value);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static PrimitiveData getPrimitiveDataFromString(String text, ParameterType type){
        PrimitiveData retVal = null;
        if (text != null && type != null){
            if (IOMessageConverterUtils.isInteger(text) && new IntegerData(Integer.parseInt(text)).checkType(type))
                retVal = new IntegerData(Integer.parseInt(text));
            else if (IOMessageConverterUtils.isDouble(text) && new DecimalData(Double.parseDouble(text)).checkType(type))
                retVal = new DecimalData(Double.parseDouble(text));
            else if (IOMessageConverterUtils.isURI(text) && new AnyURIData(text).checkType(type))
                retVal = new AnyURIData(text);
            else if (IOMessageConverterUtils.isBoolean(text) && new BooleanData(Boolean.valueOf(text)).checkType(type))
                retVal = new BooleanData(Boolean.valueOf(text));
            else if (new StringData(text).checkType(type))
                retVal = new StringData(text);
        }

        return retVal;
    }

}
