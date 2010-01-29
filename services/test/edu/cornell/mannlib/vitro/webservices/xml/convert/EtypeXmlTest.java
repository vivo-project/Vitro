package edu.cornell.mannlib.vitro.webservices.xml.convert.test;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.beans.VClass;
import edu.cornell.mannlib.vitro.webservices.xml.convert.*;
import edu.cornell.mannlib.vitro.dao.db.test.EntityDaoTest;
import junit.framework.TestCase;

public class EtypeXmlTest extends TestCase {
    public void testXml1(){
        String result = "<VClass id=\"2132\" quickEditJsp=\"bleck/editjsp.bla\">"+
        "<typename>testtype</typename></VClass>";
        VClass etype = new VClass();
        etype.setId(2132);
//        etype.setTypename("testtype");
        etype.setQuickEditJsp("bleck/editjsp.bla");
        EtypeXml xmler = new EtypeXml();
        assertEquals(result, xmler.toXmlStr(etype));
        
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run( EtypeXmlTest.class );
    }
//this is a test change
}
