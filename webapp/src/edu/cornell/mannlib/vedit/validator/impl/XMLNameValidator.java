/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.ValidationObject;

public class XMLNameValidator implements Validator {

    private final static String ERR_MSG = "Must start with a letter or '_' and use only letters, digits, '.', '-' or '_'. No spaces allowed.";

    Pattern pat = null;
    boolean permitEmpty = false;

    public XMLNameValidator() {
        pat = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\-\\.]*");
    }

    public XMLNameValidator(boolean permitEmpty) {
	this();
	this.permitEmpty = permitEmpty;
    }

    public ValidationObject validate (Object obj) throws IllegalArgumentException {
        ValidationObject vo = new ValidationObject();
        String theString = null;

        try {
            theString = (String) obj;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected instance of String");
        }

	if (permitEmpty && (theString == null || "".equals(theString))) {
	    vo.setValid(true);
	} else {
            Matcher mat = pat.matcher(theString);
            if (mat.matches()){
                vo.setValid(true);
            } else {
                vo.setValid(false);
                vo.setMessage(ERR_MSG);
            }
        }

        vo.setValidatedObject(obj);
        return vo;
    }

}
