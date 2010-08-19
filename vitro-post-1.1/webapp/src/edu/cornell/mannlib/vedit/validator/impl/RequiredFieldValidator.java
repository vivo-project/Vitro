/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator.impl;

import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.ValidationObject;

public class RequiredFieldValidator implements Validator {

    public ValidationObject validate (Object obj) throws IllegalArgumentException {

        ValidationObject vo = new ValidationObject();

        if (obj==null || (obj instanceof String && ((String)obj).length()==0)) {
            vo.setValid(false);
            vo.setMessage("Please enter a value");
        } else {
            vo.setValid(true);
        }
        vo.setValidatedObject(obj);

        return vo;

    }

}
