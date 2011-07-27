/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.smoketest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Any class that wishes to output status to smoketest.ftl 
 * implements this interface.
 */
public interface SmokeTest {
	
	public TestResult test(VitroRequest vreq);
	
	public String getName();
}
