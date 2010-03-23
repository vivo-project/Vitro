/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

import junit.framework.Assert;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class StringUtilsTest extends AbstractTestClass {
    
    protected static List<String> stringList = new ArrayList<String>();
    protected static List<Integer> intList = new ArrayList<Integer>();
    static {
        stringList.add("apple");
        stringList.add("banana");
        stringList.add("orange");  
        
        intList.add(1);
        intList.add(2);
        intList.add(3);
    }

    @Test
    public void testCapitalize() {
        String s1 = "cat";
        Assert.assertEquals("Cat", StringUtils.capitalize(s1));
        
        String s2 = "Cat";
        Assert.assertEquals(s2, StringUtils.capitalize(s2));
        
        String s3 = "CAT";
        Assert.assertEquals(s3, StringUtils.capitalize(s3));
        
    }
    
    @Test
    public void testQuote() {
        String s1 = "cat";
        Assert.assertEquals("\"cat\"", StringUtils.quote(s1));
        
        String s2 = "";
        Assert.assertEquals("", StringUtils.quote(s2));
    }
    
    @Test 
    public void testJoinNoArgs() {
        
        Assert.assertEquals("apple,banana,orange", StringUtils.join(stringList));
        Assert.assertEquals("1,2,3", StringUtils.join(intList));
    }
    
    @Test 
    public void testJoinArgs() {
        
        Assert.assertEquals("apple:banana:orange", StringUtils.join(stringList, false, ":"));
        Assert.assertEquals("\"apple\"|\"banana\"|\"orange\"", StringUtils.join(stringList, true, "|"));
        Assert.assertEquals("\"apple\",\"banana\",\"orange\"", StringUtils.join(stringList, true, null));
        Assert.assertEquals("apple,banana,orange", StringUtils.join(stringList, false, null));
        Assert.assertEquals("apple...banana...orange", StringUtils.join(stringList, false, "..."));
        
    }    
    
    @Test 
    public void testQuotedList() {
        
        Assert.assertEquals("\"apple\"|\"banana\"|\"orange\"", StringUtils.quotedList(stringList, "|"));
        Assert.assertEquals("\"apple\",\"banana\",\"orange\"", StringUtils.quotedList(stringList, null));
    }
    
    @Test
    public void testEqualsOneOf() {
        
        String s1 = "cat";
        Assert.assertTrue(StringUtils.equalsOneOf(s1, "dog", "mouse", "cat", "horse"));
        Assert.assertTrue(StringUtils.equalsOneOf(s1, "cat"));
        Assert.assertFalse(StringUtils.equalsOneOf(s1, "dog", "mouse", "horse"));
        Assert.assertFalse(StringUtils.equalsOneOf(s1));       
    }
}
