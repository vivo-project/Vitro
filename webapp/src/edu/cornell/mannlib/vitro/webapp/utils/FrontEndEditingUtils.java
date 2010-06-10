/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class FrontEndEditingUtils {
 
    // NB List includes only those properties currently editable from the front end.
    private static final List<String> VITRO_NS_DATA_PROPS = Arrays.asList(VitroVocabulary.BLURB,                                                                         
                                                                        VitroVocabulary.CITATION, 
                                                                        VitroVocabulary.DESCRIPTION, 
                                                                        VitroVocabulary.IND_MAIN_IMAGE, 
                                                                        VitroVocabulary.LABEL, 
                                                                        VitroVocabulary.MONIKER
                                                                        // VitroVocabulary.RDF_TYPE,
                                                                        // VitroVocabulary.TIMEKEY
                                                                        );
    
    // NB List includes only those properties currently editable from the front end.   
    private static final List<String> VITRO_NS_OBJECT_PROPS = Arrays.asList(VitroVocabulary.ADDITIONAL_LINK,
                                                                           VitroVocabulary.PRIMARY_LINK
                                                                           );
            

    public static String getVitroNsPropDatatypeUri(String propName) {
        //Resource datatype = propName == TIMEKEY ? XSD.dateTime : XSD.xstring;
        //return datatype.getURI();
        return XSD.xstring.getURI();
    }

//  public static final Map<String, String> VITRO_NS_PROPERTIES = new HashMap<String, String>() {
//  {
//      put(BLURB, XSD.xstring.getURI());
//      put(CITATION, XSD.xstring.getURI());
//      put(DESCRIPTION, XSD.xstring.getURI());
//      put(LABEL, XSD.xstring.getURI());
//      put(LINK_ANCHOR, XSD.xstring.getURI());
//      put(MONIKER, XSD.xstring.getURI());
//      put(PRIMARY_LINK, XSD.xstring.getURI()); 
//      put(RDF_TYPE, XSD.xstring.getURI());
//      put(TIMEKEY, XSD.dateTime.getURI());            
//  }
//};

    public static boolean isVitroNsDataProp(String propertyUri) {
        return VITRO_NS_DATA_PROPS.contains(propertyUri);
    }
    
    public static boolean isVitroNsObjProp(String propertyUri) {
        return VITRO_NS_OBJECT_PROPS.contains(propertyUri);
    }
    
    public static String getVitroNsObjDisplayName(String predicateUri, Individual object, Model model) {
        
        String displayName = null;
                
        // These are the only Vitro namespace object properties that are editable on the front end at this point.
        if (StringUtils.equalsOneOf(predicateUri, VitroVocabulary.PRIMARY_LINK, VitroVocabulary.ADDITIONAL_LINK)) {            
            String linkAnchor = getLiteralValue(model, object, VitroVocabulary.LINK_ANCHOR);
            String linkUrl = getLiteralValue(model, object, VitroVocabulary.LINK_URL);            
            displayName = "<a class='externalLink' href='" + linkUrl + "'>" + linkAnchor + "</a>";                       
        }
        
        return displayName;

    }
    
    private static String getLiteralValue(Model model, Individual ind, String predicateUri) {

        String value = null;
        StmtIterator stmts = model.listStatements(model.createResource(ind.getURI()),  
                                                  model.getProperty(predicateUri),
                                                  (RDFNode)null);
        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            RDFNode node = stmt.getObject();
            if (node.isLiteral()) {
                Literal lit = (Literal) node.as(Literal.class);
                value = lit.getLexicalForm();
            }
        }
        
        return value;
            
    }
   
}
