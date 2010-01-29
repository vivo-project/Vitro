package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.web.jsptags.InputElementFormattingTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RdfLiteralHash {
    
    private static final Log log = LogFactory.getLog(RdfLiteralHash.class.getName());
    
    /**
     * Make a hash based on individual, property, literal and (lang or datatype).
     * 
     * @param stmt
     * @return a value between MIN_INTEGER and MAX_INTEGER 
     */
    public  static int makeRdfLiteralHash( DataPropertyStatement stmt ){
        if( (stmt.getLanguage() != null && stmt.getLanguage().trim().length() > 0) 
            && 
            (stmt.getDatatypeURI() != null && stmt.getDatatypeURI().trim().length() > 0  ) )
            throw new Error("DataPropertyStatement should not have both a language " +
                    "and a datatype; lang: '" + stmt.getLanguage() + "' datatype: '"+ stmt.getDatatypeURI() + "'");
            
        if( stmt.getIndividualURI() == null || stmt.getIndividualURI().trim().length() == 0 )
            throw new Error("Cannot make a hash for a statement with no subject URI");
        
        if( stmt.getDatapropURI() == null || stmt.getDatapropURI().trim().length() == 0)
            throw new Error("Cannot make a hash for a statement with no predicate URI");
        
        String langOrDatatype = "9876NONE";
        if( stmt.getLanguage() != null && stmt.getLanguage().trim().length() > 0){
           langOrDatatype = stmt.getLanguage();
       }else{
           if( stmt.getDatatypeURI() != null && stmt.getDatatypeURI().trim().length() > 0){
               langOrDatatype = stmt.getDatatypeURI();
           }
       }

        String hashMe = langOrDatatype + "_" + stmt.getIndividualURI() + "_" + stmt.getDatapropURI() + "_" + stmt.getData();
        if( log.isDebugEnabled() )
            log.debug("got hash " + hashMe.hashCode() + " for String '" + hashMe + "'");
        return hashMe.hashCode();
    }

    /**
     * 
     * @param stmt
     * @param hash
     * @return
     */
    public static boolean doesStmtMatchHash( DataPropertyStatement stmt, int hash){ 
        if( stmt == null )
            return false;
        
        int stmtHash;
        try{
            stmtHash = makeRdfLiteralHash(stmt);
            log.debug("incoming hash "+hash+" compared to calculated hash "+stmtHash);
        }catch( Throwable th){
            return false;
        }
        return stmtHash == hash;
    }
    
    
    /**
     * 
     * @param ind, may be null and getDataPropertyStatements() may return null.
     * @param hash
     * @return a DataPropertyStatement if found or null if not found
     */
    public static DataPropertyStatement getDataPropertyStmtByHash( Individual ind, int hash){       
        if( ind == null )  return null;
        List<DataPropertyStatement> statements = ind.getDataPropertyStatements();
        if( statements == null ) return null;
        for( DataPropertyStatement dps : statements){
            if( doesStmtMatchHash(dps, hash) )
                return dps;
        }
        return null;
    }
    
}
