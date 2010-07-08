/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;


public class IndexBuilderThreadTest extends AbstractTestClass {
	
	@Test
	public void testStoppingTheThread(){
		setLoggerLevel(IndexBuilderThread.class, Level.OFF);
		IndexBuilderThread ibt = new IndexBuilderThread(null);
		ibt.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		ibt.kill();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertFalse(ibt.isAlive());
	}
}
