/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.Arrays;
import java.util.List;

//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class FrontEndEditingUtils {
 
    public static final List<String> VITRO_NS_DATA_PROPS = Arrays.asList(VitroVocabulary.BLURB,                                                                         
                                                                        VitroVocabulary.CITATION, 
                                                                        VitroVocabulary.DESCRIPTION, 
                                                                        VitroVocabulary.IMAGETHUMB, 
                                                                        VitroVocabulary.LABEL, 
                                                                        VitroVocabulary.MONIKER
                                                                        // VitroVocabulary.RDF_TYPE,
                                                                        // VitroVocabulary.TIMEKEY
                                                                        );
    
    public static final List<String> VITRO_NS_OBJECT_PROPS = Arrays.asList(VitroVocabulary.ADDITIONAL_LINK,
                                                                           VitroVocabulary.PRIMARY_LINK
                                                                           );
            

    public static String getVitroNsPropDatatypeUri(String propName) {
        //Resource datatype = propName == TIMEKEY ? XSD.dateTime : XSD.xstring;
        //return datatype.getURI();
        return XSD.xstring.getURI();
    }
    
//    public static final Map<String, String> VITRO_NS_PROPERTIES = new HashMap<String, String>() {
//        {
//            put(BLURB, XSD.xstring.getURI());
//            put(CITATION, XSD.xstring.getURI());
//            put(DESCRIPTION, XSD.xstring.getURI());
//            put(LABEL, XSD.xstring.getURI());
//            put(LINK_ANCHOR, XSD.xstring.getURI());
//            put(MONIKER, XSD.xstring.getURI());
//            put(PRIMARY_LINK, XSD.xstring.getURI()); 
//            put(RDF_TYPE, XSD.xstring.getURI());
//            put(TIMEKEY, XSD.dateTime.getURI());            
//        }
//    };
}
