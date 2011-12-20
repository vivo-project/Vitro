/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator.impl;

import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.ValidationObject;

public class IntValidator implements Validator {

    protected int minVal = 0; // the edit framework doesn't handle negative ints
    protected int maxVal = Integer.MAX_VALUE;

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

    public IntValidator(){}
    
    public IntValidator (int minVal, int maxVal){
        this.minVal = minVal;
        this.maxVal = maxVal;
    }
}
