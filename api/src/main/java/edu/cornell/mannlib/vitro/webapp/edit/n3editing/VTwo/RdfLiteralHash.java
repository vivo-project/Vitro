/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Set of static methods to create hashes for RDF literal statements
 * so they can be referenced in HTTP parameters.
 * This allows the creation of a URL to request the deletion of a 
 * RDF literal statement without including the whole literal in the parameters.
 *
 * ex.
 * {@code http://fake.com/delete?sub="http://bob"&pred="http://hasNickName"&stmtHash="23443434"}
 *
 * This could request the deletion of a the statement for Bob's nickname where the 
 * literal matched the hash 23443434.
 * 
 * The hash is for the whole statement, not just the literal part.  The has will only match
 * if the subject, predicate, datetype/language and literal match. 
 * 
 */
public class RdfLiteralHash {
    
    private static final Log log = LogFactory.getLog(RdfLiteralHash.class.getName());
    
    /**
     * Make a hash based on individual, property, literal and (lang or datatype).
     * 
     * @param stmt Data statement
     * @return a value between MIN_INTEGER and MAX_INTEGER 
     */
    public static int makeRdfLiteralHash( DataPropertyStatement stmt ){
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
     * @param stmt Data statement
     * @param hash Hash
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
     * @param subjectUri  Subject URI
     * @param predicateUri  Predicate URI
     * @param hash Hash
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
