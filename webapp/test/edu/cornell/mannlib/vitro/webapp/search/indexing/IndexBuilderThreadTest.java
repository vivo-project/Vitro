/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;


public class IndexBuilderThreadTest extends AbstractTestClass {
	
	@Test
	public void testStoppingTheThread(){	 
	    setLoggerLevel(IndexBuilder.class, Level.OFF);
	    
		IndexBuilder ib = new IndexBuilder();		
		Assert.assertNotSame(Thread.State.NEW, ib.getState() );
		Assert.assertNotSame(Thread.State.TERMINATED, ib.getState() );
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		ib.stopIndexingThread();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertFalse(ib.isAlive());
		Assert.assertSame(Thread.State.TERMINATED, ib.getState() );
	}
}
