package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IsInteger extends IsNotBlank {
	
	private static final Log log = LogFactory.getLog(IsInteger.class);

	@Override
	public boolean isValid(String name, String[] values) {
		if (!super.isValid(name, values)) {
			return false;
		}
		
		if (!isInteger(values[0])) {
			return false;
		}
		return true;
	}

	private boolean isInteger(String string) {
		//TODO: Do the real check 
		if (NumberUtils.isParsable(string)) {
			 return true;	
		}
		return false;
	}
}
