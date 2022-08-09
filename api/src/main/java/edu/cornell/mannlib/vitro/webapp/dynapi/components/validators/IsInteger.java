package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class IsInteger extends IsNotBlank {

    private static final Log log = LogFactory.getLog(IsInteger.class);

    @Override
    public boolean isValid(String name, RawData data) {
        if (!super.isValid(name, data)) {
            return false;
        }
        
        if (data.getParam().isArray()) {
    		List array = ArrayView.getArray(data);
			for (Object value : array) {
				if (!isInteger(value.toString())) {
				    return false;
				}
			}
    	} else {
    		try {
				if (!isInteger(data.getJsonValue())) {
				    return false;
				}
			} catch (ConversionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
         
        return true;
    }

    private boolean isInteger(String string) {
    	System.out.println(string);
    	try {
    		Integer.parseInt(string);
    		return true;
    	} catch(Exception e) {
    		log.debug(e,e);
    	}
        log.debug("Value is not a number. Validation failed.");
        return false;
    }

}
