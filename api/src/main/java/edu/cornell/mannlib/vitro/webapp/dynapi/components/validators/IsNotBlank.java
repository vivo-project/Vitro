package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import org.apache.commons.lang3.StringUtils;

public class IsNotBlank extends AbstractValidator {

	@Override
	public boolean isValid(String name, String[] values) {
		if (values.length == 0) {
			return false;
		}
		if (StringUtils.isBlank(values[0])) {
			return false;
		}
		return true;
	}
}
