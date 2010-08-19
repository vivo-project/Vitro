/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * User: jc55
 * Date: August 22, 2008
 * Time: 4:37 PM
 */
public class MakeTidyTest extends AbstractTestClass {
	@Before
	public void suppressLogging() {
		setLoggerLevel(MakeTidy.class, Level.WARN);
	}

    @Test
    public void testTidy(){
        String inputStr = "<p garbage here/><ul><li>list element one</li><li>list element two</li></ul></p";
        String expected = "<ul><li>list element one</li><li>list element two</li></ul>";

        MakeTidy tidy = new MakeTidy();
        assertEquivalentXmlDocs(expected, tidy.process(inputStr));
    }
}
