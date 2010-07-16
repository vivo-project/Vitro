/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator.impl;

import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.ValidationObject;
import java.util.regex.*;

public class UrlValidator implements Validator {

    public ValidationObject validate (Object obj) throws IllegalArgumentException {

        ValidationObject vo = new ValidationObject();
        String theString = null;

        if (!(obj instanceof String)){
            throw new IllegalArgumentException("Expected instance of String");
        }

        Pattern pat = Pattern.compile("[a-z]{3,5}*://.*\\.[a-z]{2,4}");
        Matcher mat = pat.matcher(theString);
        if (mat.matches()){
            vo.setValid(true);
        } else {
            vo.setValid(false);
            vo.setMessage("Please enter a valid URL");
        }

        vo.setValidatedObject(obj);
        return vo;
    }
}
