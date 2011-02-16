/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class VitroURLTest { 

    /**
     * Test of beginsWithSlash method, of class VitroURL.
     */
    @Test
    public void testBeginsWithSlash()
    {     
        String pathStr = "entity?home=1&uri=http://aims.fao.org/aos/geopolitical.owl#Afghanistan";
        VitroURL instance = new VitroURL("", "UTF-8");
        boolean expResult = false;
        boolean result = instance.beginsWithSlash(pathStr);
        assertEquals(expResult, result);
    }

    /**
     * Test of endsInSlash method, of class VitroURL.
     */
    @Test
    public void testEndsInSlash()
    {        
        String pathStr = "/entity?home=1&uri=http://aims.fao.org/aos/geopolitical.owl#Afghanistan";
        VitroURL instance = new VitroURL("", "UTF-8");
        boolean expResult = false;
        boolean result = instance.endsInSlash(pathStr);
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class VitroURL.
     * This test includes a Individual URI with a '=' and a '?'
     * This test is from David Cliff via sourceforge.
     */
    @Test
    public void testToString()
    {
        String MelbUniStr = "/entity?home=1&uri=HTTPS://bida.themis.unimelb.edu.au/pls/apex/f?p=mrw2rdf:org:::::org_id:145";
        VitroURL instance = new VitroURL(MelbUniStr, "UTF-8");
        String expResult = "/entity?home=1&uri=HTTPS%3A%2F%2Fbida.themis.unimelb.edu.au%2Fpls%2Fapex%2Ff%3Fp%3Dmrw2rdf%3Aorg%3A%3A%3A%3A%3Aorg_id%3A145";
        String result = instance.toString();
        assertEquals(expResult, result);

        String defaultTestStr = "/entity?home=1&uri=http://aims.fao.org/aos/geopolitical.owl#Afghanistan";
        instance = new VitroURL(defaultTestStr, "UTF-8");
        expResult = "/entity?home=1&uri=http%3A%2F%2Faims.fao.org%2Faos%2Fgeopolitical.owl%23Afghanistan";
        result = instance.toString();
        assertEquals(expResult, result);
    }
    
    
    /**
     * This is a test similar to testToString()
     * in that it has a = and a ?  but it doesn't
     *  expect a double encoded URI.
     */
    @Test
    public void testWithEqualsSign(){
        String MelbUniStr = "/entity?home=1&uri=HTTPS://bida.themis.unimelb.edu.au/pls/apex/f?p=mrw2rdf:org:::::org_id:145";
        VitroURL instance = new VitroURL(MelbUniStr, "UTF-8");
        String expResult ="/entity?home=1&uri=HTTPS%3A%2F%2Fbida.themis.unimelb.edu.au%2Fpls%2Fapex%2Ff%3Fp%3Dmrw2rdf%3Aorg%3A%3A%3A%3A%3Aorg_id%3A145";        
        assertEquals(expResult, instance.toString());
    }
   
    @Test
    public void testParseQueryParams(){
        //parseQueryParams
        VitroURL instance = new VitroURL("stringNotImportant", "UTF-8");       
        List<String[]> result = instance.parseQueryParams("uri=HTTPS://bida.themis.unimelb.edu.au/pls/apex/f?p=mrw2rdf:org:::::org_id:145");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.get(0));
        Assert.assertEquals(2, result.get(0).length);
        Assert.assertEquals("uri",result.get(0)[0]);
        Assert.assertEquals("HTTPS://bida.themis.unimelb.edu.au/pls/apex/f?p=mrw2rdf:org:::::org_id:145",result.get(0)[1]);    
    }
    
    @Test
    public void testParseQueryParams2(){
        //parseQueryParams
        VitroURL instance = new VitroURL("stringNotImportant", "UTF-8");       
        List<String[]> result = instance.parseQueryParams("home=1&uri=HTTPS://bida.themis.unimelb.edu.au/pls/apex/f?p=mrw2rdf:org:::::org_id:145");
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        
        Assert.assertNotNull(result.get(0));
        Assert.assertEquals(2, result.get(0).length);
        Assert.assertEquals("home",result.get(0)[0]);
        Assert.assertEquals("1",result.get(0)[1]);
        
        Assert.assertNotNull(result.get(1));
        Assert.assertEquals(2, result.get(1).length);
        Assert.assertEquals("uri",result.get(1)[0]);
        Assert.assertEquals("HTTPS://bida.themis.unimelb.edu.au/pls/apex/f?p=mrw2rdf:org:::::org_id:145",result.get(1)[1]);    
    }
    
    @Test
    public void testParseQueryParams3(){
        //parseQueryParams
        VitroURL instance = new VitroURL("stringNotImportant", "UTF-8");       
        List<String[]> result = instance.parseQueryParams("home=1&uri=HTTPS://bida.edu.au/pls/apex/f?p=mrw2&additiona=234");
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        
        Assert.assertNotNull(result.get(0));
        Assert.assertEquals(2, result.get(0).length);
        Assert.assertEquals("home",result.get(0)[0]);
        Assert.assertEquals("1",result.get(0)[1]);
        
        Assert.assertNotNull(result.get(1));
        Assert.assertEquals(2, result.get(1).length);
        Assert.assertEquals("uri",result.get(1)[0]);
        Assert.assertEquals("HTTPS://bida.edu.au/pls/apex/f?p=mrw2&additiona=234",result.get(1)[1]);    
    }
    
    @Test (expected=Error.class)
    public void testParseQueryParams4(){
        //parseQueryParams
        VitroURL instance = new VitroURL("stringNotImportant", "UTF-8");       
        List<String[]> result = instance.parseQueryParams("home=1&shouldBeURI=HTTPS://bida.edu.au/pls/apex/f?p=mrw2&additiona=234");
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        
        Assert.assertNotNull(result.get(0));
        Assert.assertEquals(2, result.get(0).length);
        Assert.assertEquals("home",result.get(0)[0]);
        Assert.assertEquals("1",result.get(0)[1]);
        
        Assert.assertNotNull(result.get(1));
        Assert.assertEquals(2, result.get(1).length);
        Assert.assertEquals("uri",result.get(1)[0]);
        Assert.assertEquals("HTTPS://bida.edu.au/pls/apex/f?p=mrw2&additiona=234",result.get(1)[1]);    
    }
}