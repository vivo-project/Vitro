package edu.cornell.mannlib.vitro.webapp.auth.identifier;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Pulls a netId out of the CUWebAuth REMOTE_USER header.
 *
 *
 * @author bdc34
 *
 */
public class SelfEditingIdentifierFactory implements IdentifierBundleFactory {
    public final static String httpHeaderForNetId = "REMOTE_USER";

    private static final Log log = LogFactory.getLog(SelfEditingIdentifierFactory.class.getName());


    public IdentifierBundle getIdentifierBundle(ServletRequest request, HttpSession session, ServletContext context) {
       IdentifierBundle idb = getFromCUWebAuthHeader(request,session,context);       
       if( idb != null )
           return idb;
       else
           return getFromSession(request,session);       
    }

    private IdentifierBundle getFromCUWebAuthHeader(ServletRequest request, HttpSession session,ServletContext context){
        String cuwebauthUser = ((HttpServletRequest)request).getHeader(CUWEBAUTH_REMOTE_USER_HEADER);
        log.debug("Looking for CUWebAuth header " + CUWEBAUTH_REMOTE_USER_HEADER + " found : '" + cuwebauthUser +"'");
        
        if( cuwebauthUser == null || cuwebauthUser.length() == 0){
                log.debug("No CUWebAuthUser string found");
                return null;
        }
        if( cuwebauthUser.length() > 100){
            log.info("CUWebAuthUser is longer than 100 chars, this may be a malicious request");
            return null;
        }
        if( context == null ){
            log.error("ServletContext was null");
            return null;
        }                                         

        NetId netid = new NetId(cuwebauthUser);                        
        SelfEditing selfE = null;

        IdentifierBundle idb = new ArrayIdentifierBundle();
        idb.add(netid);
        log.debug("added NetId object to IdentifierBundle from CUWEBAUTH header");            
        //VitroRequest vreq = new VitroRequest((HttpServletRequest)request);

        WebappDaoFactory wdf = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
        if( wdf == null ){
            log.error("Could not get a WebappDaoFactory from the ServletContext");
            return null;
        }

        String uri = wdf.getIndividualDao().getIndividualURIFromNetId(cuwebauthUser);

        if( uri != null){
            Individual ind = wdf.getIndividualDao().getIndividualByURI(uri);
            if( ind != null ){
                String blacklisted = checkForBlacklisted(ind, context);
                
                selfE = new SelfEditing( ind ,blacklisted );                
                idb.add(  selfE );
                log.debug("Found an Individual for netId " + cuwebauthUser + " URI: " + ind.getURI() );
            }else{
                log.warn("found a URI for the netId " + cuwebauthUser + " but could not build Individual");
            }
        }else{
            log.debug("could not find an Individual with a netId of " + cuwebauthUser );
        }        
        putNetIdInSession(session, selfE, netid);            
        return idb;

    }
    
    /**
     * Runs through .sparql files in the BLACKLIST_SPARQL_DIR, the first that returns one
     * or more rows will be cause the user to be blacklisted.  The first variable from
     * the first solution set will be returned.   
     */
    public static String checkForBlacklisted(Individual ind, ServletContext context) {
        if( ind == null || context == null ) {
            log.error("could not check for Blacklist, null individual or context");
            return NOT_BLACKLISTED;
        }        
        String realPath = context.getRealPath(BLACKLIST_SPARQL_DIR);        
        File blacklistDir = new File(realPath );        
        if( !blacklistDir.exists()){
            log.debug("could not find blacklist directory " + realPath);
            return NOT_BLACKLISTED;
        }
        if( ! blacklistDir.canRead() || ! blacklistDir.isDirectory() ){
            log.debug("cannot read blacklist directory " + realPath);
            return NOT_BLACKLISTED;            
        }

        log.debug("checking directlry " + realPath + " for blacklisting sparql query files");
        File[] files = blacklistDir.listFiles(new FileFilter(){
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".sparql");
            }}
        );

        String reasonForBlacklist = NOT_BLACKLISTED;
        for( File file : files ){
            try{
                reasonForBlacklist = runSparqlFileForBlacklist( file, ind, context);
                if( reasonForBlacklist != NOT_BLACKLISTED ) 
                    break;
            }catch(RuntimeException ex){
                log.error("Could not run blacklist check query for file " +
                        file.getAbsolutePath() + file.separatorChar + file.getName(),
                        ex);                
            }
        }
        return reasonForBlacklist;           
    }

    /**
     * Runs the SPARQL query in the file with the uri of the individual 
     * substituted in.  If there are any solution sets, then the URI of
     * the variable named "cause" will be returned, make sure that it is a 
     * resource with a URI. Otherwise null will be returned.
     * The URI of ind will be substituted into the query where ever the
     * token "?individualURI" is found.
     */
    private static String runSparqlFileForBlacklist
        (File file, Individual ind, ServletContext context) 
    {
        if( !file.canRead() ){
            log.debug("cannot read blacklisting SPARQL file " + file.getName());
            return NOT_BLACKLISTED;
        }
        String queryString = null;
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);            
            byte b[]= new byte[fis.available()];
            fis.read(b);
            queryString = new String(b);            
        }catch( FileNotFoundException fnfe){
            log.debug(fnfe);
            return NOT_BLACKLISTED;
        }catch( IOException ioe){
            log.debug(ioe);
            return NOT_BLACKLISTED;
        }finally{
            try {
                fis.close();
            } catch (IOException e) {
              log.warn("could not close file", e);
            }
        }
        
        if( queryString == null || queryString.length() == 0 ){
            log.debug(file.getName() + " is empty");
            return NOT_BLACKLISTED;            
        }
        Model model = (Model)context.getAttribute("jenaOntModel");
        
        queryString = queryString.replaceAll("\\?individualURI", "<" + ind.getURI() + ">");
        log.debug(queryString);
        Query query = QueryFactory.create(queryString);        
        QueryExecution qexec = QueryExecutionFactory.create(query,model);
        try{
            ResultSet results = qexec.execSelect();
            while(results.hasNext()){
                QuerySolution solution = results.nextSolution();
                if( solution.contains("cause") ){      
                    RDFNode node = solution.get("cause");
                    if( node.canAs( Resource.class ) ){
                        Resource x = solution.getResource("cause");                     
                        return x.getURI();
                    }else if( node.canAs(Literal.class)){
                        Literal x = (Literal)node.as(Literal.class);
                        return x.getString();
                    }
                }else{
                    log.error("Query solution must contain a variable \"cause\" of type Resource or Literal.");
                    return null;
                }
            }
        }finally{ qexec.close(); }    
        return null;
    }

    private IdentifierBundle getFromSession(ServletRequest req, HttpSession session ){
        NetId netid = (NetId)session.getAttribute(NETID_IN_SESSION);
        SelfEditing sed = (SelfEditing)session.getAttribute(URI_IN_SESSION);
        
        if( netid != null || sed != null ){
            IdentifierBundle idb = new ArrayIdentifierBundle();
            if( netid != null){
                idb.add(netid);
                log.debug("added NetId from session");
            }
            if( sed != null ){
                idb.add(sed);
                log.debug("added SelfEditing from Session");
            }
            return idb;
        }else 
            return null;
    }
    
    
    protected final static String NETID_IN_SESSION = "NetIdIdentifierFactory.netid";
    protected final static String URI_IN_SESSION = "NetIdIdentifierFactory.uri";
    
    public static void putNetIdInSession( HttpSession session, SelfEditing se, NetId ni){
        session.setAttribute(NETID_IN_SESSION, ni);
        session.setAttribute(URI_IN_SESSION, se);
    }
    
    public static void clearNetIdFromSession( HttpSession session ){
        session.removeAttribute(NETID_IN_SESSION);
        session.removeAttribute(URI_IN_SESSION);
    }
    
    /********************** NetId inner class *************************/
    public static class NetId implements Identifier{
        public final String value;
        public NetId(String value){
            this.value = value;
        }
        public String getValue(){return value;}
        public String toString(){ return value;}
    }
    
    
    /**
     * An identifier with the Individual that represents the human self-editor. 
     */
    public static class SelfEditing implements Identifier{        
        final Individual individual;
        final String blacklisted;        
        
        public SelfEditing ( Individual individual, String blacklisted){
            if( individual == null )
                throw new IllegalArgumentException("Individual must not be null");            
            this.individual = individual;
            this.blacklisted = blacklisted;            
        }
        public String getValue(){
            return individual.getURI();
        }
        public Individual getIndividual(){
            return individual;
        }
        public String getBlacklisted(){
            return blacklisted;
        }
        public String toString(){
            return "SelfEditing as " + getValue() +
            (getBlacklisted()!=null?  " blacklisted by via " + getBlacklisted():"");
        }
    }
    
    public static SelfEditing getSelfEditingIdentifier( IdentifierBundle whoToAuth ){
        if( whoToAuth == null ) return null;        
        for(Identifier id : whoToAuth){
            if (id instanceof SelfEditing) 
                return (SelfEditing)id;                           
        }
        return null;
    }
    
    public static String getSelfEditingUri( IdentifierBundle whoToAuth){
        SelfEditing sid = getSelfEditingIdentifier(whoToAuth);
        if( sid != null )
            return sid.getValue();
        else
            return null;
    }
    public static final String NOT_BLACKLISTED = null;   
    private final static String BLACKLIST_SPARQL_DIR = "/admin/selfEditBlacklist";
    private final static String CUWEBAUTH_REMOTE_USER_HEADER = "REMOTE_USER";
}
