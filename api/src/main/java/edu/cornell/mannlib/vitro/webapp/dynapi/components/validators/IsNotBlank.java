package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;

public class IsNotBlank extends AbstractValidator {

    private static final Log log = LogFactory.getLog(IsNotBlank.class);

    @Override
    public boolean isValid(String name, Data data) {
    	
    	if (JsonContainerView.isJsonArray(data.getParam())) {
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
    		return isValidValue(name, SimpleDataView.getStringRepresentation(data));
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
