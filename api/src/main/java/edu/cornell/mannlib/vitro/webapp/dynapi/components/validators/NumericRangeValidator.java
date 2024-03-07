package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public boolean isValid(String name, Data data) {
        if (!super.isValid(name, data)) {
            return false;
        }

        if (JsonContainerView.isJsonArray(data.getParam())) {
            List array = ArrayView.getArray(data);
            for (Object value : array) {
                if (!isInRange(value.toString())) {
                    log.debug("Length of " +
                            name +
                            " is not in range [" +
                            ((minValue != null) ? minValue : " ") +
                            "-" +
                            ((maxValue != null) ? maxValue : " ") +
                            "].");
                    return false;
                }
            }
        } else {
            if (!isInRange(SimpleDataView.getStringRepresentation(data))) {
                log.debug("Length of " +
                        name +
                        " is not in range [" +
                        ((minValue != null) ? minValue : " ") +
                        "-" +
                        ((maxValue != null) ? maxValue : " ") +
                        "].");
                return false;
            }

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
