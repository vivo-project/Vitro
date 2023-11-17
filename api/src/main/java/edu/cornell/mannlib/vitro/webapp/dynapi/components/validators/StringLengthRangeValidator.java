package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class StringLengthRangeValidator extends IsNotBlank {

    private static final Log log = LogFactory.getLog(StringLengthRangeValidator.class);

    private Integer minLength;

    private Integer maxLength;

    public Integer getMinLength() {
        return minLength;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#minLength", maxOccurs = 1)
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#maxLength", maxOccurs = 1)
    public void setMaxLength(int maxValue) {
        this.maxLength = maxValue;
    }

    @Override
    public boolean isValid(String name, Data data) {
        if (!super.isValid(name, data)) {
            return false;
        }
    	if (JsonContainerView.isJsonArray(data.getParam())) {
    		List<String> array = ArrayView.getArray(data);
			for (Object value : array) {
				if (!isLengthInRange(value.toString())) {
	                log.debug("Length of " + name + " is not in range [" + ((minLength != null) ? minLength : " ") + "-"
	                        + ((maxLength != null) ? maxLength : " ") + "].");
	                return false;
	            }
			}
    	} else {
    		if (!isLengthInRange(SimpleDataView.getStringRepresentation(data))) {
    			log.debug("Length of " + name + " is not in range [" + ((minLength != null) ? minLength : " ") + "-"
                        + ((maxLength != null) ? maxLength : " ") + "].");
                return false;
    		}
    		
    	}
        return true;
    }

    private boolean isLengthInRange(String string) {
        int length = string.length();

        if ((minLength != null) && (length < minLength)) {
            return false;
        }

        if ((maxLength != null) && (length > maxLength)) {
            return false;
        }

        return true;
    }
}
