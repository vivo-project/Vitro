/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.smoketest;

/*
 * A Basic object that contains the status of a service
 * and a boolean indicating whether the service is on or off.
 * TODO: This is an initial implementation and might change significantly
 * over the course of time. (see NIHVIVO-336)
 */
public class TestResult {
	
	private String result = "";
	private boolean success = false;
	
	public TestResult(String result, boolean message) {
		this.result = result;
		this.success = message;
	}
	
	public String getResult(){
		return result;
	}
	
	public boolean getMessage(){
		return success;
	}
	

}
