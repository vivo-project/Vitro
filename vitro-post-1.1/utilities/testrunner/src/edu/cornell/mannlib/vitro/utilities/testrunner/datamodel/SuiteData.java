/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.datamodel;

import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults;

/**
 * TODO
 */
public class SuiteData {
	private final String name;
	private final boolean ignored;
	private final SuiteResults results;

	public SuiteData(String name, boolean ignored, SuiteResults results) {
		this.name = name;
		this.ignored = ignored;
		this.results = results;
	}

	public String getName() {
		return name;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public SuiteResults getResults() {
		return results;
	}

}
