package edu.cornell.mannlib.vitro.webservices.serializers;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializerFactory;

import java.util.Iterator;
import java.util.Vector;

/**
 * * This is used to by the axis webservices. see vitro/webservices/wsdd/VitroWs.wsdd
 */
public class EntitySerFactory implements SerializerFactory {
    private Vector mechanisms;

    public EntitySerFactory() {
    }
    public javax.xml.rpc.encoding.Serializer getSerializerAs(String mechanismType) {
        return new EntitySerializer();
    }
    public Iterator getSupportedMechanismTypes() {
        if (mechanisms == null) {
            mechanisms = new Vector();
            mechanisms.add(Constants.AXIS_SAX);
        }
        return mechanisms.iterator();
    }
}
