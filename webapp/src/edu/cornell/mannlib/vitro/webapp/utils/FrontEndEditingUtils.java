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

    public static enum EditMode {
        ADD, EDIT, REPAIR, ERROR;
    }
    
    /* Determine whether a property editing form is in add, edit, or repair mode. */
    public static EditMode getEditMode(HttpServletRequest request, String relatedPropertyUri) {
       
        Individual obj = (Individual)request.getAttribute("object");
        return getEditMode(request, obj, relatedPropertyUri);
    }
    
    public static EditMode getEditMode(HttpServletRequest request, Individual obj, String relatedPropertyUri) {
    	 EditMode mode = EditMode.ADD;
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
