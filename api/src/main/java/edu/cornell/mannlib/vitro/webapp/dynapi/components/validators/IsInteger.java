package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOMessageConverterUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IsInteger extends IsNotBlank {

    private static final Log log = LogFactory.getLog(IsInteger.class);

    @Override
    public boolean isValid(String name, String value) {
        if (!super.isValid(name, value)) {
            return false;
        }

        if (!isInteger(value)) {
            return false;
        }

        return true;
    }

    private boolean isInteger(String value) {
        if (! IOMessageConverterUtils.isInteger(value)) {
            log.debug("Value is not a number. Validation failed.");
            return false;
        } else
            return true;
    }

}
