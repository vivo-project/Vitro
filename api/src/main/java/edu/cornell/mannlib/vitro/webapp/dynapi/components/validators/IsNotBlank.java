package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public class IsNotBlank extends AbstractValidator {

    private static final Log log = LogFactory.getLog(IsNotBlank.class);

    @Override
    public boolean isValid(String name, RawData data) {
    	
    	if (data.getParam().isArray()) {
    		List array = ArrayView.getArray(data);
    		if (array.isEmpty()) {
                log.debug("No values of " + name + " found. Validation failed.");
    			return false;
    		}
			for (Object value : array) {
				if (StringUtils.isBlank(value.toString())) {
					log.debug("Value is blank. Validation failed.");
					return false;
				}
			}
    	} else {
    		return isValidValue(name, data.getObject().toString());
    	}
        
        return true;
    }

	private boolean isValidValue(String value, String name) {
		 if (StringUtils.isBlank(value)) {
             log.debug("Param " + name + " Value is blank. Validation failed.");
             return false;
         }
		 return true;
	}

}
