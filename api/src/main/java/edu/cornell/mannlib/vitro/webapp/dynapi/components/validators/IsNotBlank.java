package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IsNotBlank extends AbstractValidator {

    private static final Log log = LogFactory.getLog(IsNotBlank.class);

    @Override
    public boolean isValid(String name, String value) {
        if (value == null) {
            log.debug("No values of " + name + " found. Validation failed.");
            return false;
        }
        if (StringUtils.isBlank(value)) {
           log.debug("Value is blank. Validation failed.");
           return false;
        }
        return true;
    }

}
