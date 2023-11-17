package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class RegularExpressionValidator extends AbstractValidator {

    private static final Log log = LogFactory.getLog(RegularExpressionValidator.class);

    private String regularExpression;

    public String getRegularExpression() {
        return regularExpression;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#regularExpression", minOccurs = 1, maxOccurs = 1)
    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    @Override
    public boolean isValid(String name, Data data) {
    	
    	if (JsonContainerView.isJsonArray(data.getParam())) {
    		List<String> array = ArrayView.getArray(data);
    		if (array.isEmpty()) {
                log.debug("No values of " + name + " found. Validation failed.");
    			return false;
    		}
			for (Object value : array) {
				 if (!isMatches(value.toString())) {
		                log.debug("Value of " + name + " is not in accordance with the pattern \"" + regularExpression + "\"");
		                return false;
		            }
				if (StringUtils.isBlank(value.toString())) {
					log.debug("Value is blank. Validation failed.");
					return false;
				}
			}
    			
    	} else {
    		if (!isMatches(SimpleDataView.getStringRepresentation(data))) {
                log.debug("Value of " + name + " is not in accordance with the pattern \"" + regularExpression + "\"");
                return false;
            }
    	}
        return true;
    }

    private boolean isMatches(String string) {
        return Pattern.matches(regularExpression, string);
    }

}
