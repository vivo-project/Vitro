/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;


import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;

public class BasicValidationVTwoTest {


    @SuppressWarnings("unchecked")
    @Test
    public void testHttpUrlValidate() {
        BasicValidationVTwo bv = new BasicValidationVTwo(Collections.EMPTY_MAP);
        String res;
        res = bv.validate("httpUrl", "http://example.com/index");
        Assert.assertEquals(res, BasicValidationVTwo.SUCCESS);
        
        res = bv.validate("httpUrl", "http://example.com/index?bogus=skjd%20skljd&something=sdkf");
        Assert.assertEquals(res, BasicValidationVTwo.SUCCESS);
        
        res = bv.validate("httpUrl", "http://example.com/index#2.23?bogus=skjd%20skljd&something=sdkf");
        Assert.assertEquals(res, BasicValidationVTwo.SUCCESS);               
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyValidate(){
       BasicValidationVTwo bv = new BasicValidationVTwo(Collections.EMPTY_MAP);              
       
       Assert.assertEquals(
               bv.validate("nonempty", null)
               , BasicValidationVTwo.REQUIRED_FIELD_EMPTY_MSG);
        

       Assert.assertEquals(
               bv.validate("nonempty", "")
               , BasicValidationVTwo.REQUIRED_FIELD_EMPTY_MSG);
       
       Assert.assertEquals(
               bv.validate("nonempty", "some value")
               , BasicValidationVTwo.SUCCESS);
    }
}
