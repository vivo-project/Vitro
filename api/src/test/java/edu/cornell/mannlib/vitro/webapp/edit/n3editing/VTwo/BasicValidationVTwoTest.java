/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;


import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.i18n.I18nStub;
import stubs.javax.servlet.http.HttpServletRequestStub;

public class BasicValidationVTwoTest {

    @Before
    public void useI18nStubBundles() {
        I18nStub.setup();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHttpUrlValidate() {
        HttpServletRequest req = new HttpServletRequestStub();
        BasicValidationVTwo bv = new BasicValidationVTwo(Collections.EMPTY_MAP, I18nStub.bundle(req));
        String res;
        res = bv.validate("httpUrl", "http://example.com/index");
        Assert.assertEquals(BasicValidationVTwo.SUCCESS, res);

        res = bv.validate("httpUrl", "http://example.com/index?bogus=skjd%20skljd&something=sdkf");
        Assert.assertEquals(BasicValidationVTwo.SUCCESS, res);

        res = bv.validate("httpUrl", "http://example.com/index#2.23?bogus=skjd%20skljd&something=sdkf");
        Assert.assertEquals(BasicValidationVTwo.SUCCESS, res);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyValidate(){
       HttpServletRequest req = new HttpServletRequestStub();
       BasicValidationVTwo bv = new BasicValidationVTwo(Collections.EMPTY_MAP, I18nStub.bundle(req));

       Assert.assertEquals(BasicValidationVTwo.REQUIRED_FIELD_EMPTY_MSG, bv.validate("nonempty", null));
       Assert.assertEquals(BasicValidationVTwo.REQUIRED_FIELD_EMPTY_MSG, bv.validate("nonempty", ""));
       Assert.assertEquals(BasicValidationVTwo.SUCCESS, bv.validate("nonempty", "some value"));
    }
}
