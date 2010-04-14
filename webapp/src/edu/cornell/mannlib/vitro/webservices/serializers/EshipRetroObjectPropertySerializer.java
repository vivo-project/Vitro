/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webservices.serializers;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.wsdl.fromJava.Types;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;


/**
 * This is used to by the axis webservices. see vitro/webservices/wsdd/VitroWs.wsdd
 */
public class EshipRetroObjectPropertySerializer implements Serializer {
    public static final QName myTypeQName = new QName("typeNS", "VitroEntity");

    public static final String ENTS2ENTSID_MBER = "ents2entsId";
    public static final String DOMAINID_MBER = "domainId";
    public static final String DOMAIN_MBER = "domain";
    public static final String RANGEID_MBER = "rangeId";
    public static final String RANGE_MBER = "range";
    public static final String PROPERTYID_MBER = "propertyId";
    public static final String PROPERTY_MBER = "property";
    public static final String QUALIFIER_MBER = "qualifier";
    public static final String DOMAINORIENTED_MBER = "domainOriented";


    /**
     * Serialize an element named name, with the indicated attributes
     * and value.
     * @param name is the element name
     * @param attributes are the attributes...serialize is free to add more.
     * @param value is the value
     * @param context is the SerializationContext
     */
    public void serialize(QName name, Attributes attributes,
                          Object value, SerializationContext context)
        throws IOException
    {
        if (!(value instanceof ObjectProperty ))
            throw new IOException("Can't serialize a " + value.getClass().getName() + " with a EshipRetroObjectPropertySerializer.");
        ObjectProperty e2e = (ObjectProperty)value;
        context.startElement(name, attributes);

        //context.serialize(new QName("", ENTS2ENTSID_MBER), null, e2e.getEnts2entsId());
        //context.serialize(new QName("", DOMAINID_MBER), null, e2e.getDomainId());
        //context.serialize(new QName("", DOMAIN_MBER), null, e2e.getDomain());
        context.serialize(new QName("", "URI"         ), null, e2e.getURI());
        context.serialize(new QName("", "domainPublic"), null, e2e.getDomainPublic());
        context.serialize(new QName("", "domainSide"  ), null, e2e.getDomainSidePhasedOut());
                
        List stmts = e2e.getObjectPropertyStatements();        
        context.serialize(new QName("", "ents2Ents"       ), null, stmts);

        context.endElement();
    }

    public String getMechanismType() { return Constants.AXIS_SAX; }

    public Element writeSchema(Class javaType, Types types) throws Exception {
        return null;
    }

}
