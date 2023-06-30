package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.IsInteger;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.IsNotBlank;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.NumericRangeValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.RegularExpressionValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.StringLengthRangeValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;

public class ValidatorsTest {

    @Test
    public void testIsIntegerValidator() {

        Validator validator = new IsInteger();

        String fieldName = "field";

        String[] values1 = { "1245", "-123" };
        assertTrue(validator.isValid(fieldName, values1));

        String[] values2 = { "1245.2" };
        assertFalse(validator.isValid(fieldName, values2));
    }

    @Test
    public void testIsNotBlankValidator() {

        Validator validator = new IsNotBlank();

        String fieldName = "field";

        String[] values1 = {};
        assertFalse(validator.isValid(fieldName, values1));

        String[] values2 = { "" };
        assertFalse(validator.isValid(fieldName, values2));

        String[] values3 = { "a string" };
        assertTrue(validator.isValid(fieldName, values3));
    }

    @Test
    public void testNumericRangeValidator() {

        NumericRangeValidator validator1 = new NumericRangeValidator();
        validator1.setMaxValue(40.3f);
        NumericRangeValidator validator2 = new NumericRangeValidator();
        validator2.setMinValue(36);
        NumericRangeValidator validator3 = new NumericRangeValidator();
        validator3.setMinValue(36);
        validator3.setMaxValue(40.3f);

        String fieldName = "field";

        String[] values1 = { "35" };
        assertTrue(validator1.isValid(fieldName, values1));
        assertFalse(validator2.isValid(fieldName, values1));
        assertFalse(validator3.isValid(fieldName, values1));

        String[] values2 = { "36.3" };
        assertTrue(validator1.isValid(fieldName, values2));
        assertTrue(validator2.isValid(fieldName, values2));
        assertTrue(validator3.isValid(fieldName, values2));

        String[] values3 = { "42" };
        assertFalse(validator1.isValid(fieldName, values3));
        assertTrue(validator2.isValid(fieldName, values3));
        assertFalse(validator3.isValid(fieldName, values3));
    }

    @Test
    public void testStringLengthRangeValidator() {

        StringLengthRangeValidator validator1 = new StringLengthRangeValidator();
        validator1.setMaxLength(7);
        StringLengthRangeValidator validator2 = new StringLengthRangeValidator();
        validator2.setMinLength(5);
        StringLengthRangeValidator validator3 = new StringLengthRangeValidator();
        validator3.setMinLength(5);
        validator3.setMaxLength(7);

        String fieldName = "field";

        String[] values1 = { "test" };
        assertTrue(validator1.isValid(fieldName, values1));
        assertFalse(validator2.isValid(fieldName, values1));
        assertFalse(validator3.isValid(fieldName, values1));

        String[] values2 = { "testte" };
        assertTrue(validator1.isValid(fieldName, values2));
        assertTrue(validator2.isValid(fieldName, values2));
        assertTrue(validator3.isValid(fieldName, values2));

        String[] values3 = { "testtest" };
        assertFalse(validator1.isValid(fieldName, values3));
        assertTrue(validator2.isValid(fieldName, values3));
        assertFalse(validator3.isValid(fieldName, values3));
    }

    @Test
    public void testRegularExpressionValidator() {

        RegularExpressionValidator validator1 = new RegularExpressionValidator();
        validator1.setRegularExpression("^(.+)@(\\S+)$");

        String fieldName = "email";

        String[] values1 = { "dragan@uns.ac.rs" };
        assertTrue(validator1.isValid(fieldName, values1));

        String[] values2 = { "dragan@" };
        assertFalse(validator1.isValid(fieldName, values2));

        String[] values3 = { "uns.ac.rs" };
        assertFalse(validator1.isValid(fieldName, values3));
    }

}
