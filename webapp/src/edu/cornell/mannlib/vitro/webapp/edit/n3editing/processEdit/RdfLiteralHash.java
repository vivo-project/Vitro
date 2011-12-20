/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class RdfLiteralHash {
    
    private static final Log log = LogFactory.getLog(RdfLiteralHash.class.getName());
    
    /**
     * Make a hash based on individual, property, literal and (lang or datatype).
     * 
     * @param stmt
     * @return a value between MIN_INTEGER and MAX_INTEGER 
     */
    public static int makeRdfLiteralHash( DataPropertyStatement stmt ){
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
     * Forward to either getDataPropertyStmtByHash or getRdfsLabelStatementByHash, depending on the property.
     * @param subjectUri, 
     * @param predicateUri, 
     * @param hash
     * @param model, may not be null
     * @return a DataPropertyStatement if found or null if not found
     */

    public static DataPropertyStatement getPropertyStmtByHash(String subjectUri, String predicateUri, int hash, Model model) {        
        if (subjectUri == null || predicateUri == null ) return null;
        
        model.enterCriticalSection(false);
        StmtIterator stmts = model.listStatements(model.createResource(subjectUri),  
                                                  model.getProperty(predicateUri),
                                                  (RDFNode)null);        
        try {
            while (stmts.hasNext()) {
                Statement stmt = stmts.nextStatement();
                RDFNode node = stmt.getObject();
                if ( node.isLiteral() ){
                    DataPropertyStatement dps =
                        makeDataPropertyStatementFromStatement(stmt, node);          
                    if (doesStmtMatchHash(dps, hash)) {
                        return dps;
                    }
                }
            }
            return null;
        } finally {
                stmts.close();
                model.leaveCriticalSection();
        }    
    }
    

//    /**
//     * Get data property for subject, predicate and hash.  This does not use 
//     * filtering DAOs to avoid the problems when attempting to edit predicates that
//     * are filtered out.
//     *         
//     * @param ind, may be null 
//     * @param hash
//     * @return a DataPropertyStatement if found or null if not found
//     */
//    protected static DataPropertyStatement getStatementByHash(String subjectUri, Model model, int hash) {
//
//    
//    
//    DataPropertyStatement dps = null;
//    
// // Not using getAllDataPropertyStatements() because it filters out rdfs:labels
////      
////      List<DataPropertyStatement> statements = ind.getDataPropertyStatements();
////      if( statements == null ) return null;
////      for( DataPropertyStatement dps : statements){  
////          if( doesStmtMatchHash(dps, hash) )
////              return dps;
////      }
////      return null;
//    
//    StmtIterator stmts = model.listStatements(model.createResource(subjectUri),  
//                                              model.getProperty(predicateUri),
//                                              (RDFNode)null);
//    try {
//        while (stmts.hasNext()) {
//            Statement stmt = stmts.nextStatement();
//            RDFNode node = stmt.getObject();
//            if ( node.isLiteral() ){
//                dps = makeDataPropertyStatementFromStatement(stmt, node);          
//                if (doesStmtMatchHash(dps, hash)) {
//                    return dps;
//                }
//            }
//        }
//        //} catch {
//
//        } finally{
//            stmts.close();
//        }
//        return null;
//    }
    
    /**
     * Get data property for subject, predicate and hash.  This does not use 
     * filtering DAOs to avoid the problems when attempting to edit predicates that
     * are filtered out.
     *         
     * @param ind, may be null 
     * @param hash
     * @return a DataPropertyStatement if found or null if not found
     */
//    protected static DataPropertyStatement getStatementByHash(String subjectUri, String predicateUri, Model model, int hash) {        
//        // Not using getAllDataPropertyStatements() because it filters out rdfs:labels
////         
////         List<DataPropertyStatement> statements = ind.getDataPropertyStatements();
////         if( statements == null ) return null;
////         for( DataPropertyStatement dps : statements){  
////             if( doesStmtMatchHash(dps, hash) )
////                 return dps;
////         }
////         return null;
//
//        
//        model.enterCriticalSection(false);
//        StmtIterator stmts = model.listStatements(model.createResource(subjectUri),  
//                                                  model.getProperty(predicateUri),
//                                                  (RDFNode)null);        
//        try {
//            while (stmts.hasNext()) {
//                Statement stmt = stmts.nextStatement();
//                RDFNode node = stmt.getObject();
//                if ( node.isLiteral() ){
//                    DataPropertyStatement dps =
//                        makeDataPropertyStatementFromStatement(stmt, node);          
//                    if (doesStmtMatchHash(dps, hash)) {
//                        return dps;
//                    }
//                }
//            }
//            return null;
//        } finally {
//                stmts.close();
//                model.leaveCriticalSection();
//        }            
//    }
    
    
    public static int makeRdfsLabelLiteralHash( Individual subject, String value, Model  model) { 
        
        String subjectUri = subject.getURI();
        String predicateUri = VitroVocabulary.LABEL;
        
        StmtIterator stmts = model.listStatements(model.createResource(subjectUri), 
                                                  model.getProperty(predicateUri), 
                                                  (RDFNode) null);                     
        DataPropertyStatement dps = null;
        int hash = 0;
        int count = 0;
        try {           
            while (stmts.hasNext()) {
                Statement stmt = stmts.nextStatement();
                RDFNode node = stmt.getObject();
                if (node.isLiteral()) {
                    count++;
                    dps = makeDataPropertyStatementFromStatement(stmt, node);
                    hash = makeRdfLiteralHash(dps);
                }
            }
        } finally {
            stmts.close();
        }
        
        if( count == 1 ) {
            return hash;
        } else if( count == 0 ){
            log.debug("No data property statement for " +
                    "subject:" + subjectUri + "\npredicate:" + predicateUri + "\nvalue: " + value);
            throw new IllegalArgumentException("Could not create RdfLiteralHash because " +
                    "there was no data property statement with the given value.");      
        } else{
            log.debug("Multiple data property statements for " +
                    "subject:" + subjectUri + "\npredicate:" + predicateUri + "\nvalue: " + value);
            throw new IllegalArgumentException("Could not create RdfLiteralHash because " +
                    "there were multiple data property statements with the given value.");                  
        }       
    }

    private static DataPropertyStatement makeDataPropertyStatementFromStatement(Statement statement, RDFNode node) {

        Literal lit = (Literal) node.as(Literal.class);
        String value = lit.getLexicalForm();
        String lang = lit.getLanguage();
        String datatypeUri = lit.getDatatypeURI();

        DataPropertyStatement dps = new DataPropertyStatementImpl();
        dps.setDatatypeURI(datatypeUri);
        dps.setLanguage(lang);
        dps.setData(value);
        dps.setDatapropURI(statement.getPredicate().getURI());
        dps.setIndividualURI(statement.getSubject().getURI());
         
        return dps;
    }
    
}
