/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Accepts requests to update a set of URIs in the search index. 
 */
public class UpdateUrisInIndexTest {

    @Test
    public void lineToUrisTest(){
        Assert.assertEquals(Arrays.asList("uri1"), UpdateUrisInIndex.lineToUris( "uri1"));
        Assert.assertEquals(Arrays.asList("uri1", "uri2"), UpdateUrisInIndex.lineToUris( "uri1,uri2"));
        
        Assert.assertEquals(Arrays.asList("uri1"), UpdateUrisInIndex.lineToUris( "uri1\n"));
        Assert.assertEquals(Arrays.asList("uri1","uri2"), UpdateUrisInIndex.lineToUris( "uri1\nuri2"));
        
        Assert.assertEquals(Collections.EMPTY_LIST, UpdateUrisInIndex.lineToUris( "" ));
        Assert.assertEquals(Collections.EMPTY_LIST, UpdateUrisInIndex.lineToUris( "," ));
        Assert.assertEquals(Collections.EMPTY_LIST, UpdateUrisInIndex.lineToUris( " , " ));        
    }
    
    
    @Test
    public void UrisFromInputIteratorTest(){
        doUrisFromInputIterator("",0);
        doUrisFromInputIterator(" ",0);
        doUrisFromInputIterator(" , ",0);
        doUrisFromInputIterator("\n",0);
        doUrisFromInputIterator("\n\n\n",0);
        doUrisFromInputIterator("http://bogus.com/n234",1);        
        doUrisFromInputIterator("http://bogus.com/n234\nhttp://bogus.com/n442",2);
        doUrisFromInputIterator("http://bogus.com/n234, http://bogus.com/n442",2);
        doUrisFromInputIterator("http://bogus.com/n234,\nhttp://bogus.com/n442\n",2);
        
        doUrisFromInputIterator("http://bogus.com/n234\n",1);        
        doUrisFromInputIterator("\nhttp://bogus.com/n234",1);
        doUrisFromInputIterator("\nhttp://bogus.com/n234\n",1);
        
    }
          
    public void doUrisFromInputIterator(String input, int expectedUris){
        Iterator<String> it = new UpdateUrisInIndex.UrisFromInputIterator( new StringReader(input) );
        int count = 0;
        while( it.hasNext()){
            String uri = it.next();
            if( uri == null)
                Assert.fail("UrisFromInputIterator should not return null strings \n " + 
                     "Null string for uri #" + count + " for input '" + input + "'");
            if( uri.isEmpty())
                Assert.fail("UrisFromInputIterator should not return empty strings \n " + 
                     "Empty string for uri #" + count + " for input '" + input + "'");            
            count++;
        }
        Assert.assertEquals("Incorrect number of URIs from input '" + input + "'", expectedUris, count);
    }
    
}
