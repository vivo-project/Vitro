package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;

public class RegularExpressionValidator extends AbstractValidator {
	
	private static final Log log = LogFactory.getLog(RegularExpressionValidator.class);

	private String regularExpression;

	public String getRegularExpression() {
		return regularExpression;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#regularExpression", minOccurs = 1, maxOccurs = 1)
	public void setRegularExpression (String regularExpression) {
		this.regularExpression = regularExpression;
	}

	@Override
	public boolean isValid(String name, String[] values) {
		if (values.length == 0) {
			log.debug("No values of " + name + " found. Validation failed.");
			return false;
		}

		for (String value : values) {
			if (!isLengthInRange(value)) {
				log.debug("Value of " + name + " is not in accordance with the pattern \"" + regularExpression + "\"");
				return false;
			}
		}

		return true;
	}

	private boolean isLengthInRange(String string) {
		return Pattern.matches(regularExpression, string);
	}
}
