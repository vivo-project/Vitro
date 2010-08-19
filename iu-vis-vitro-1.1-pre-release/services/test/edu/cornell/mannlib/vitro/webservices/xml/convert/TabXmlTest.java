/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webservices.xml.convert.test;

import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;

import junit.framework.TestCase;
import edu.cornell.mannlib.vitro.beans.*;
import edu.cornell.mannlib.vitro.dao.db.*;
import edu.cornell.mannlib.vitro.webservices.xml.convert.TabXml;

public class TabXmlTest extends TestCase {
    
    private void doTabXmlTest(int tabId){
//        Tab tab = TabDao.getTab(tabId, 1,"now()",10,60,0,null,null,null);
//        Element elem = TabXml.toXmlElem(tab, 10);
//        assertTrue( elem != null );
//        String resultXml = null;
//        try{
//            resultXml = HTMLWriter.prettyPrintXHTML( elem.asXML() );
//        }catch(Exception ex){
//            fail("exception: " + ex.toString());
//        }
//        assertTrue(resultXml != null && resultXml.length() > 0); 
        
    }
    
    public void testTabs(){
        doTabXmlTest(63); 
        doTabXmlTest(18);
        doTabXmlTest(19);
        //bad xhtml doTabXmlTest(23);
        doTabXmlTest(25);
        doTabXmlTest(35);
        doTabXmlTest(64);
        doTabXmlTest(42);        
    }
    
    public static void main(String[] args){
        junit.textui.TestRunner.run( TabXmlTest.class );
    }
}
