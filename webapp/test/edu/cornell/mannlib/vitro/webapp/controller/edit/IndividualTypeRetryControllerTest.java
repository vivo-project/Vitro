/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;


public class IndividualTypeRetryControllerTest extends AbstractTestClass {

    @Test
    public void optionCollator(){         
        IndividualTypeRetryController.OptionCollator oc = new IndividualTypeRetryController.OptionCollator();         
        int comp = oc.compare(
                    new Option("foo", "foo"),
                    new Option("Person", "foaf:Person") );
        //we just want compare() to not throw an exception
    }
}
