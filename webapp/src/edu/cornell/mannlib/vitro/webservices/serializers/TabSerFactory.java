package edu.cornell.mannlib.vitro.webservices.serializers;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Iterator;
import java.util.Vector;

import javax.xml.rpc.encoding.Serializer;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializerFactory;

public class TabSerFactory implements SerializerFactory{
    private TabSerializer ts;
    private Vector mechanisms;

    public TabSerFactory() {
    }
    
    public javax.xml.rpc.encoding.Serializer getSerializerAs(String mechanismType) {
        if( ts == null )
            ts = new TabSerializer();        
        return ts;
    }
    
    public Iterator getSupportedMechanismTypes() {
        if (mechanisms == null) {
            mechanisms = new Vector();
            mechanisms.add(Constants.AXIS_SAX);
        }
        return mechanisms.iterator();
    }

}
