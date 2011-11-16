/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.http.HttpServletRequestStub;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class EditConfigurationUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetEditKey() {
        HttpServletRequestStub req = new HttpServletRequestStub();
        req.addParameter("datapropKey", "2343");
        
        Integer hash = EditConfigurationUtils.getDataHash(new VitroRequest(req));
        Assert.assertNotNull(hash);
        Assert.assertEquals(new Integer(2343), hash);
        
        
        req = new HttpServletRequestStub();        
        
        hash = EditConfigurationUtils.getDataHash(new VitroRequest(req));
        Assert.assertNull( hash);
        
        
    }

}
