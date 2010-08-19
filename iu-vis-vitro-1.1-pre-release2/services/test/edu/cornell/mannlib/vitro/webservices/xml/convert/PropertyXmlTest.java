/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webservices.xml.convert.test;

import junit.framework.TestCase;
import edu.cornell.mannlib.vitro.webservices.xml.convert.*;
import edu.cornell.mannlib.vitro.beans.*;

public class PropertyXmlTest extends TestCase {
    public void testXml1(){
        String expected =
            "<Property id=\"12\" parentId=\"1\" minCardinality=\"0\" "+
            "maxCardinality=\"23\" domainEtypeId=\"2323\" "+
            "rangeEtypeId=\"2443\"/>";
        
        Property prop = new Property();
        prop.setId(12);
        prop.setParentId(1);
        prop.setMinCardinality(0);
        prop.setMaxCardinality(23);
//        prop.setDomainEtypeId(2323);
//        prop.setRangeEtypeId(2443);
        PropertyXml xmler = new PropertyXml();        
        assertEquals(expected,xmler.toXmlStr(prop));
    }
    
    public void testXml2(){
        String expected =
            "<Property id=\"12\" parentId=\"1\" minCardinality=\"0\" "+
            "maxCardinality=\"23\" domainEtypeId=\"2323\" "+
            "rangeEtypeId=\"2443\"/>";
        
        Property prop = new Property();
        prop.setId(12);
        prop.setParentId(1);
        prop.setMinCardinality(0);
        prop.setMaxCardinality(23);
//        prop.setDomainEtypeId(2323);
//        prop.setRangeEtypeId(2443);
        PropertyXml xmler = new PropertyXml();        
        assertEquals(expected,xmler.toXmlStr(prop));
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run( PropertyXmlTest.class );
    }

}
