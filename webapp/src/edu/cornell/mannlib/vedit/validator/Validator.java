package edu.cornell.mannlib.vedit.validator;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public interface Validator {

    public ValidationObject validate(Object obj) throws IllegalArgumentException;

}
