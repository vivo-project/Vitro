package edu.cornell.mannlib.vedit.validator.impl;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.ValidationObject;

public class IntValidator implements Validator {

    protected int minVal = -1;
    protected int maxVal = -1;

    public ValidationObject validate (Object obj) throws IllegalArgumentException {

        ValidationObject vo = new ValidationObject();
        int theInt = -1;

        if (obj instanceof String) {
            try {
                theInt = Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                vo.setValid(false);
                vo.setMessage("Please enter an integer");
                vo.setValidatedObject(obj);
                return vo;
            }
        } else {
            try {
                theInt = ((Integer) obj).intValue();
            } catch (Exception e) {
                vo.setValid(false);
                vo.setMessage("Please enter an integer");
                vo.setValidatedObject(obj);
                return vo;
            }
        }

        if ( theInt < minVal || theInt > maxVal ) {
            vo.setValid(false);
            vo.setMessage("Enter a number between "+minVal+" and "+maxVal);
        } else {
            vo.setValid(true);
        }

        vo.setValidatedObject(obj);

        return vo;
    }

    public IntValidator (int minVal, int maxVal){
        this.minVal = minVal;
        this.maxVal = maxVal;
    }
}
