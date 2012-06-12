/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
/**
 * Attempt to find or setup the local application namespace.  This 
 * namespace is used for individuals in configuration of
 * the application.  This NS is different than the default namespace which
 * is used for individuals in the data.
 */
public class LocalAppNamespaceSetup implements ServletContextListener{
    protected static String LOCAL_APP_NAMESPACE = "localAppNamespace";
    

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        //check if we have a base Ont model
        if( ModelContext.getOntModelSelector(ctx) == null ){
            ss.warning(this, "Need access to a OntModelSelector. " +
            		"It is likey that the ModelSetup has not yet been run. " +
                    "Display may not work correctly." +
                    "Local appliction namespace has not been set.");

            return;
        }

        if( ModelContext.getOntModelSelector(ctx).getDisplayModel() == null ){
            ss.warning(this, "Needs access to the display model. " +
                    "Display may not work correctly." +
            "Local appliction namespace has not been set.");

            return;            
        }                        
        
        if( ConfigurationProperties.getBean(ctx) == null ){
            ss.warning(this, "Need ConfigurationProperties.  " +
            		"Display may not work correctly." +
            		"Local appliction namespace has not been set.");
            return;
        }
           
        ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
        String defaultNs = props.getProperty( JenaDataSourceSetupBase.VITRO_DEFAULT_NAMESPACE);
        
        if( defaultNs == null ){
            ss.warning(this, "Need default namespace.  " +
                    "Display may not work correctly." +
                    "Local appliction namespace has not been set.");
            return;
        }
        
        activate( ctx, 
                  ModelContext.getOntModelSelector(ctx).getDisplayModel(), 
                  defaultNs, ss);
    }    
    
    public void activate( ServletContext ctx, OntModel displayModel, 
            String defaultNS, StartupStatus ss ){
        //first try to get LANS from ctx, maybe it was already set. 
        String lans = getLocalAppNamespace( ctx );
        if( lans != null )
            return; //LANS has already been setup so don't do anything here
        
        //try to get the LANS from the display model
        lans = getLANSFromDisplayModel( displayModel , ss);
        
        
        if( lans == null ){
            // no LANS found in displayModel
            // create a new LANS based on the default namespace
            lans = newLANSFromDefaultNS( defaultNS);                
        }                       
        
        ctx.setAttribute(LOCAL_APP_NAMESPACE, lans);
        ss.warning(this, "LANS is " + lans);
    }        
    
    protected static String newLANSFromDefaultNS(String defaultNS) {
        if( defaultNS != null ){
            if( defaultNS.endsWith("individual/") ){
                return defaultNS.substring(0, defaultNS.length() - "individual/".length()) + "application/";
            }else{
                return defaultNS.substring(0,defaultNS.length() -1 ) + "/application/";
            }
        }else{
            return null;
        }
    }

    /** This might return null if no application individuals can be found. 
     * @param ss */
    protected String getLANSFromDisplayModel(OntModel displayModel, StartupStatus ss) {
        //query for any individual of a type that should have
        //been created with the LANS and then use the first
        //individual's URI to make a LANS.
        String queryForConfInds =
            "PREFIX app: <"+ ApplicationConfiguration.NS + ">  \n" +
            "SELECT REDUCED ?ind WHERE  {\n" +
            "{ ?ind a  app:Application } \n" +
            "UNION { ?ind a  app:ConfigContext } \n" +
            "UNION { ?ind a  app:ObjectPropertyDisplayConfig } \n" +
            "UNION { ?ind a  app:DataPropertyDisplayConfig } \n" +
            "UNION { ?ind a  app:ClassDisplayConfig } \n" +
            "UNION { ?ind a  app:ObjectPropertyEditConfig } \n" +
            "UNION { ?ind a  app:DataPropertyEditConfig } \n" +
            "UNION { ?ind a  app:ClassEditConfig } \n" +
            "} ORDER BY ?ind" ;             
        
        Query query = QueryFactory.create( queryForConfInds );
        QueryExecution qexec = QueryExecutionFactory.create( query , displayModel);
        List<String>uris = new ArrayList<String>();
        displayModel.enterCriticalSection(Lock.READ);
        try{        	            
            ResultSet res = qexec.execSelect();
            for( ; res.hasNext() ; ){
                QuerySolution soln = res.nextSolution();
                uris.add( soln.getResource("ind").getURI() );
            }
        }finally{
            displayModel.leaveCriticalSection();
            qexec.close();
        }
        
        Set<String> uniqNSset = new TreeSet<String>();
        String lans = null;
        for( String uri: uris){            
            if( uri != null ){
                Resource res = ResourceFactory.createResource(uri);
                if( res.getNameSpace() != null ){
                    uniqNSset.add( res.getNameSpace());                    
                }
            }
        }
        
        if( uniqNSset.size() > 0 ){
            lans = (String) uniqNSset.toArray()[0];
        }
        
        if( uniqNSset.size() > 1){
            ss.warning(this, "Multiple application namespaces found in application " +
            		"display model. Using <" + lans +"> .");
        }
        
        return lans; //might be null
    }

    /**
     * Use this method to get the localAppNamespace from the
     * ServletContext.
     */
    public static String getLocalAppNamespace( ServletContext ctx){
        return (String)ctx.getAttribute(LOCAL_APP_NAMESPACE);        
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
         //nothing to do.   
    }
}
