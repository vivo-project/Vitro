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

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#validatorRegularExpressionValue", minOccurs = 1, maxOccurs = 1)
	public void setRegularExpression (String regularExpression) {
		this.regularExpression = regularExpression;
	}

	@Override
	public boolean isValid(String name, String[] values) {
		for (String value : values) {
			if (!isLengthInRange(value)) {
				return false;
			}
		}
		return true;
	}

	private boolean isLengthInRange(String string) {
		return Pattern.matches(regularExpression, string);
	}
}
