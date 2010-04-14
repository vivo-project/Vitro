/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webservices.xml.convert.test;

import org.dom4j.Element;

import edu.cornell.mannlib.vitro.beans.Ents2Ents;
import edu.cornell.mannlib.vitro.webservices.xml.convert.Ents2EntsXml;
import junit.framework.TestCase;

public class Ents2EntsTest extends TestCase {
    
	public void testNull(){
		Ents2EntsXml xmler = new Ents2EntsXml();
		assertEquals(xmler.toXmlElem(null), null);
	}
	
	public Ents2Ents makeObj(){
		Ents2Ents obj = new Ents2Ents();
		obj.setEnts2entsId(12);
		obj.setDomainId(223);
		obj.setRangeId(2255);
		obj.setPropertyId(605);
		obj.setQualifier("This is the super(){}[]';\":.,><");		
		return obj;
	}

	public void testAttributes(){
		Ents2Ents ent = makeObj();
		Ents2EntsXml xmler = new Ents2EntsXml();
		Element elem = xmler.toXmlElem(ent);
		assertEquals(elem.valueOf("@ents2entsId"),
				Integer.toString(ent.getEnts2entsId()));
		assertEquals(elem.valueOf("@domainId"),
				Integer.toString(ent.getDomainId()));
		assertEquals(elem.valueOf("@rangeId"),
				Integer.toString(ent.getRangeId()));
		assertEquals(elem.valueOf("@etypes2RelationsId"),
				Integer.toString(ent.getPropertyId()));
		assertEquals(elem.valueOf("@qualifier"),ent.getQualifier());		
	}
	
    public static void main(String[] args) {
        junit.textui.TestRunner.run( Ents2EntsTest.class );
    }

}
