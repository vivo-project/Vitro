/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

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

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.ExternalAuthHelper;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Attempts to pull a NetId and a SelfEditing identifier from the externally
 * authorized username.
 * 
 * @author bdc34, trashed by jeb228
 */
public class SelfEditingIdentifierFactory implements IdentifierBundleFactory {
	private static final Log log = LogFactory.getLog(SelfEditingIdentifierFactory.class);
	
	/**
	 * The configuration property that names the HTTP header that will hold the
	 * username from the external authorization system.
	 */
	private static final String PROPERTY_EXTERNAL_AUTH_HEADER_NAME = "externalAuth.headerName";

	private static final int MAXIMUM_USERNAME_LENGTH = 100;
	
	public IdentifierBundle getIdentifierBundle(ServletRequest request,
			HttpSession session, ServletContext context) {
		if (!(request instanceof HttpServletRequest)) {
			log.debug("request is null or not an HttpServletRequest");
			return null;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		log.debug("request is for " + req.getRequestURI());
		
		NetId netId = figureNetId(req);
		SelfEditing selfId = figureSelfEditingId(req, netId);
		
		return buildIdentifierBundle(netId, selfId);
	}

	/**
	 * Get the name of the externally authorized user and put it into a NetId.
	 */
	private NetId figureNetId(HttpServletRequest req) {
		String externalAuthHeaderName = ConfigurationProperties.getProperty(PROPERTY_EXTERNAL_AUTH_HEADER_NAME);
		if (isEmpty(externalAuthHeaderName)) {
			log.debug(PROPERTY_EXTERNAL_AUTH_HEADER_NAME + " property is not configured.");
			return null;
		}

		String externalUsername = req.getHeader(externalAuthHeaderName);
		if (isEmpty(externalUsername)) {
			log.debug("The external username is empty.");
			return null;
		}
		if (externalUsername.length() > MAXIMUM_USERNAME_LENGTH) {
			log.info("The external username is longer than " + MAXIMUM_USERNAME_LENGTH
					+ " chars; this may be a malicious request");
			return null;
		}
		
		return new NetId(externalUsername);
	}

	/**
	 * If the externally authorized username is associated with an Individual in
	 * the model, create a SelfEditing identifier.
	 */
	private SelfEditing figureSelfEditingId(HttpServletRequest request,
			NetId netId) {
		if (netId == null) {
			return null;
		}
		String username = netId.getValue();

		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}

		ServletContext context = session.getServletContext();
		WebappDaoFactory wdf = (WebappDaoFactory) context
				.getAttribute("webappDaoFactory");
		if (wdf == null) {
			log.error("Could not get a WebappDaoFactory from the ServletContext");
			return null;
		}

		IndividualDao indDao = wdf.getIndividualDao();
		
		ExternalAuthHelper helper = ExternalAuthHelper.getHelper(request);
		String uri = helper.getIndividualUriFromNetId(indDao, username);
		if (uri == null) {
			log.debug("could not find an Individual with a netId of "
					+ username);
		}

		Individual ind = indDao.getIndividualByURI(uri);
		if (ind == null) {
			log.warn("found a URI for the netId " + username
					+ " but could not build Individual");
			return null;
		}

		log.debug("Found an Individual for netId " + username + " URI: " + uri);
		String blacklisted = checkForBlacklisted(ind, context);
		return new SelfEditing(ind, blacklisted, false);
	}

	/**
	 * Create a bundle that holds the identifiers we created, or null if we
	 * didn't create any.
	 */
	private IdentifierBundle buildIdentifierBundle(NetId netId,
			SelfEditing selfId) {
		if (netId == null && selfId == null) {
			log.debug("no self-editing IDs in the session");
			return null;
		}

		IdentifierBundle idb = new ArrayIdentifierBundle();
		if (netId != null) {
			idb.add(netId);
			log.debug("added NetId from session: " + netId);
		}
		if (selfId != null) {
			idb.add(selfId);
			log.debug("added SelfEditing from Session: " + selfId);
		}
		return idb;
	}

	private boolean isEmpty(String string) {
		return (string == null || string.isEmpty());
	}

	// ----------------------------------------------------------------------
	// static utility methods
	// ----------------------------------------------------------------------
	
    public static final String NOT_BLACKLISTED = null;   
    private final static String BLACKLIST_SPARQL_DIR = "/admin/selfEditBlacklist";

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
                        file.getAbsolutePath() + File.separatorChar + file.getName(),
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

    // ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------
    
    public static class NetId implements Identifier{
        public final String value;
        public NetId(String value){
            this.value = value;
        }
        public String getValue(){return value;}
        public String toString(){ return "NetID: " + value;}
    }
    
    
    /**
     * An identifier with the Individual that represents the human self-editor. 
     */
    public static class SelfEditing implements Identifier{        
        final Individual individual;
        final String blacklisted;        
        final boolean faked; //if this is true it was setup by FakeSeflEditingIdentifierFactory 
                        
        public SelfEditing ( Individual individual, String blacklisted ){
          this(individual,blacklisted,false);   
        }
        
        public SelfEditing ( Individual individual, String blacklisted, boolean faked){
            if( individual == null )
                throw new IllegalArgumentException("Individual must not be null");            
            this.individual = individual;
            this.blacklisted = blacklisted;            
            this.faked = faked;
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
            (getBlacklisted()!=null?  " blacklisted via " + getBlacklisted():"");
        }
        public boolean isFake() {
            return faked;
        }
    }
    
}
