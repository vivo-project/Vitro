/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ObjectPropertyDaoJena;

public class FrontEndEditingUtils {
 
    private static final Log log = LogFactory.getLog(FrontEndEditingUtils.class);
    
    // NB List includes only those properties currently editable from the front end.
    private static final List<String> VITRO_NS_DATA_PROPS = Arrays.asList(VitroVocabulary.BLURB,                                                                         
                                                                        VitroVocabulary.CITATION, 
                                                                        VitroVocabulary.DESCRIPTION, 
                                                                        VitroVocabulary.LABEL, 
                                                                        VitroVocabulary.MONIKER
                                                                        // VitroVocabulary.RDF_TYPE,
                                                                        // VitroVocabulary.TIMEKEY
                                                                        );
    
    // NB List includes only those properties currently editable from the front end.   
    private static final List<String> VITRO_NS_OBJECT_PROPS = Arrays.asList(VitroVocabulary.ADDITIONAL_LINK,
                                                                           VitroVocabulary.PRIMARY_LINK
                                                                           );
            
    public static enum EditMode {
        ADD, EDIT, REPAIR, ERROR;
    }
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
        if ( predicateUri.equals(VitroVocabulary.PRIMARY_LINK) || predicateUri.equals(VitroVocabulary.ADDITIONAL_LINK) ) {       
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
    
    /* Determine whether a property editing form is in add, edit, or repair mode. */
    public static EditMode getEditMode(HttpServletRequest request, String relatedPropertyUri) {
        EditMode mode = EditMode.ADD;
        Individual obj = (Individual)request.getAttribute("object");
        if( obj != null){
            List<ObjectPropertyStatement> stmts = obj.getObjectPropertyStatements(relatedPropertyUri);
            if( stmts != null){
                if( stmts.size() > 1 ){
                    mode = EditMode.ERROR; // Multiple roleIn statements, yuck.
                    log.debug("Multiple statements found for property " + relatedPropertyUri + ". Setting edit mode to ERROR.");
                }else if( stmts.size() == 0 ){
                    mode = EditMode.REPAIR; // need to repair the role node
                    log.debug("No statements found for property " + relatedPropertyUri + ". Setting edit mode to REPAIR.");
                }else if(stmts.size() == 1 ){
                    mode = EditMode.EDIT; // editing single statement
                    log.debug("Single statement found for property " + relatedPropertyUri + ". Setting edit mode to EDIT.");
                } 
            } else {
                log.debug("Statements null for property " + relatedPropertyUri + " . Setting edit mode to ADD.");
            }
        } else {
            log.debug("No object. Setting edit mode to ADD.");        
        }
        return mode;
    }
   
}
