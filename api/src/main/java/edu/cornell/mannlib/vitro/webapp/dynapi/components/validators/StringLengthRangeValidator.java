package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringLengthRangeValidator extends IsNotBlank {
	
	private static final Log log = LogFactory.getLog(StringLengthRangeValidator.class);

	private Integer minValue;

	private Integer maxValue;

	public Integer getMinValue() {
		return minValue;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#validatorMinNumericValue", minOccurs = 1, maxOccurs = 1)
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#validatorMaxNumericValue", minOccurs = 1, maxOccurs = 1)
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public boolean isValid(String name, String[] values) {
		if (!super.isValid(name, values)) {
			return false;
		}

		for (String value : values) {
			if (!isLengthInRange(value)) {
				return false;
			}
		}
		return true;
	}

	private boolean isLengthInRange(String string) {
		int length = string.length();

		if ((minValue != null) && (length < minValue))
			return false;

		if ((maxValue != null) && (length > maxValue))
			return false;

		return true;
	}
}
