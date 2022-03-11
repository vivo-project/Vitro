package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class NumericRangeValidator extends IsNotBlank {

    private static final Log log = LogFactory.getLog(NumericRangeValidator.class);

    private Float minValue;

    private Float maxValue;

    public Float getMinValue() {
        return minValue;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#minValue", maxOccurs = 1)
    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#maxValue", maxOccurs = 1)
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String name, String value) {
        if (!super.isValid(name, value)) {
            return false;
        }
        if (!isInRange(value)) {
            log.debug("Value of " + name + " is not in range [" + ((minValue != null) ? minValue : " ") + "-"
                    + ((maxValue != null) ? maxValue : " ") + "].");
            return false;
        }
        return true;
    }

    private boolean isInRange(String string) {
        if (NumberUtils.isParsable(string)) {
            double value = NumberUtils.toDouble(string);

            if ((minValue != null) && (value < minValue)) {
                return false;
            }

            if ((maxValue != null) && (value > maxValue)) {
                return false;
            }

            return true;
        }
        return false;
    }

}
