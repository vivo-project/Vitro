/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import static org.junit.Assert.*;

import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.BasicValidation;

public class BasicValidationTest {

	@Test
	public void testValidate() {
		BasicValidation bv = new BasicValidation(Collections.EMPTY_MAP);
		String res;
		res = bv.validate("httpUrl", "http://example.com/index");
		Assert.assertEquals(res, bv.SUCCESS);
		
		res = bv.validate("httpUrl", "http://example.com/index?bogus=skjd%20skljd&something=sdkf");
		Assert.assertEquals(res, bv.SUCCESS);
		
		res = bv.validate("httpUrl", "http://example.com/index#2.23?bogus=skjd%20skljd&something=sdkf");
		Assert.assertEquals(res, bv.SUCCESS);				
	}

}
