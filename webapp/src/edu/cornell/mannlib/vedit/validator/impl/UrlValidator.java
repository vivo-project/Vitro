/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator.impl;

import java.util.Iterator;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.Violation;

import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vedit.validator.Validator;

public class UrlValidator implements Validator {

    public ValidationObject validate (Object obj) throws IllegalArgumentException {

        ValidationObject vo = new ValidationObject();

        if (!(obj instanceof String)){
            throw new IllegalArgumentException("Expected instance of String");
        }

        IRIFactory factory = IRIFactory.jenaImplementation();
        IRI iri = factory.create((String) obj);
        if (iri.hasViolation(false) ) {
            String errorStr = "";
            Iterator<Violation> violIt = iri.violations(false);
            while(violIt.hasNext()) {
                errorStr += violIt.next().getShortMessage() + "  ";
            }
            vo.setValid(false);
            vo.setMessage("Please enter a valid URL.  " + errorStr);
        } else {
            vo.setValid(true);
        }

        vo.setValidatedObject(obj);
        return vo;
    }
}
