/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator;

public interface Validator {

    public ValidationObject validate(Object obj) throws IllegalArgumentException;

}
