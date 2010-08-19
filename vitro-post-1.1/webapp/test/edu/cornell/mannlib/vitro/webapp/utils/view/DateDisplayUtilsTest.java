/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.view;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.utils.view.DateDisplayUtils;

public class DateDisplayUtilsTest {
    
    @Test
    public void testDisplayDate() {
        String date = "2009-10";
        Assert.assertEquals("10/2009", DateDisplayUtils.getDisplayDate(date));
    }
    
    @Test
    public void testGetDisplayDateRange() {
        String startRaw = "2010-10-11";
        String endRaw = "2010-11-09";
        Assert.assertEquals("10/11/2010 - 11/09/2010", DateDisplayUtils.getDisplayDateRangeFromRawDates(startRaw, endRaw));
        
        String start1 = "1/2/2010";
        String end1 = "3/4/2011";
        Assert.assertEquals("1/2/2010 - 3/4/2011", DateDisplayUtils.getDisplayDateRange(start1, end1));
        
        String empty = "";
        Assert.assertEquals("1/2/2010 - ", DateDisplayUtils.getDisplayDateRange(start1, empty));
        Assert.assertEquals(" - 3/4/2011", DateDisplayUtils.getDisplayDateRange(empty, end1));
        
        Assert.assertEquals("1/2/2010 - ", DateDisplayUtils.getDisplayDateRange(start1, (String)null));
        Assert.assertEquals(" - 3/4/2011", DateDisplayUtils.getDisplayDateRange((String)null, end1));       
    }
    



}
