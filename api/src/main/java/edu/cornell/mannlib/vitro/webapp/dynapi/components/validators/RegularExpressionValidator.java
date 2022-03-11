package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public boolean isValid(String name, String value) {
        if (value == null) {
            log.debug("No values of " + name + " found. Validation failed.");
            return false;
        }

        if (!matchingRegularExpression(value)) {
            log.debug("Value of " + name + " is not in accordance with the pattern \"" + regularExpression + "\"");
            return false;
        }

        return true;
    }

    private boolean matchingRegularExpression(String string) {
        return Pattern.matches(regularExpression, string);
    }

}
