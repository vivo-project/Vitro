package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IsInteger extends IsNotBlank {

    private static final Log log = LogFactory.getLog(IsInteger.class);

    @Override
    public boolean isValid(String name, Data data) {
        if (!super.isValid(name, data)) {
            return false;
        }

        if (JsonContainerView.isJsonArray(data.getParam())) {
            List array = ArrayView.getArray(data);
            for (Object value : array) {
                if (!isInteger(value.toString())) {
                    return false;
                }
            }
        } else {
            if (!isInteger(data.getSerializedValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception e) {
            log.debug(e, e);
        }
        log.debug("Value is not a number. Validation failed.");
        return false;
    }

}
