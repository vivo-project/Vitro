/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
     * Forward to either getDataPropertyStmtByHash or getVitroNsPropByHash, depending on the type of property.
     * @param ind
     * @param hash
     * @param model
     * @param isVitroNsProp
     * @return a DataPropertyStatement if found or null if not found
     */
    // RY Instead of a code fork here, we should have a method of Individual getAllDataPropertyStatements() which
    // doesn't filter out the vitro ns property statements. This would also simplify the front end editing of the vitro ns
    // properties, because they wouldn't have to be a special case.
    public static DataPropertyStatement getPropertyStmtByHash(Individual ind, int hash, Model model, boolean isVitroNsProp) {
        
        if (ind == null) return null;
        
        DataPropertyStatement dps = isVitroNsProp ? RdfLiteralHash.getVitroNsPropertyStmtByHash(ind, model, hash) :
            RdfLiteralHash.getDataPropertyStmtByHash(ind, hash);

        return dps;
    }
    

    public static DataPropertyStatement getDataPropertyStmtByHash( Individual ind, int hash){       

        List<DataPropertyStatement> statements = ind.getDataPropertyStatements();
        if( statements == null ) return null;
        for( DataPropertyStatement dps : statements){
            if( doesStmtMatchHash(dps, hash) )
                return dps;
        }
        return null;
    }

    /**
     * 
     * @param ind, may be null and getDataPropertyStatements() may return null.
     * @param hash
     * @return a DataPropertyStatement if found or null if not found
     */
    public static DataPropertyStatement getVitroNsPropertyStmtByHash(Individual ind, Model model, int hash) {

        DataPropertyStatement dps = null;
        StmtIterator stmts = model.listStatements(model.createResource(ind.getURI()), null, (RDFNode)null);
        try {
            while (stmts.hasNext()) {
                Statement stmt = stmts.nextStatement();
                RDFNode node = stmt.getObject();
                if ( node.isLiteral() ){
                    Literal lit = (Literal)node.as(Literal.class);
                    String value = lit.getLexicalForm();
                    String lang = lit.getLanguage();
                    String datatypeURI = lit.getDatatypeURI();
                    dps = new DataPropertyStatementImpl();
                    dps.setDatatypeURI(datatypeURI);
                    dps.setLanguage(lang);
                    dps.setData(value);
                    dps.setDatapropURI(stmt.getPredicate().toString());
                    dps.setIndividualURI(ind.getURI());
                                   
                    if (doesStmtMatchHash(dps, hash)) {
                        break;
                    }
                }
            }
            //} catch {

            } finally{
                stmts.close();
            }
            return dps;
        }
    
}
