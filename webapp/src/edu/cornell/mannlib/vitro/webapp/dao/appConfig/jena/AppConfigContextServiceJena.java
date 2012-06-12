/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import static edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration.*;
import static org.apache.commons.lang.StringUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.ClassDisplayConfigDao;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ApplicationConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ClassDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.context.AppConfigContextService;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.context.ConfigContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoUtils;


/**
 * Service to get configurations for display and edit contexts.
 * Must be thread safe.
 * 
 */
public class AppConfigContextServiceJena extends JenaBaseDaoUtils implements AppConfigContextService {

    private String localAppConfigNS;
    
    private OntModel appConfigModel;
    private ClassDisplayConfigDao classDisplayConfigDao;
                            
    public AppConfigContextServiceJena(){        
    }
    
    public AppConfigContextServiceJena(OntModel appConfigModel, String localAppConifgNS) {         
        this.appConfigModel = appConfigModel;
        this.localAppConfigNS = localAppConifgNS;
        this.classDisplayConfigDao = new ClassDisplayConfigDaoJena(appConfigModel,localAppConifgNS );
        
        //set up queries       
        basicQuery = QueryFactory.create(basicQueryStr) ;        
        basicQualifiedQuery = QueryFactory.create(basicQualifiedQueryStr);
    }

    @Override
    public List<ApplicationConfig> getConfigsForContext(
            ConfigContext queryContext, String configurationForURI)
            throws Exception {        
        
        //this will throw an Exception if there is a problem
        checkForValidContext( queryContext );
        
        List<ApplicationConfig> configs = new ArrayList<ApplicationConfig>();
        
        //How are we going to get the config before we
        //have some sort of fancy OWL reasoning?
        
        //figure out what kind of query context
        //is is just a contextFor?
        //or does it have qualifications?
        
        //try to get the context without any inheritance
        //but with qualifications?
        configs = basicQueryForConfigs( queryContext, configurationForURI );
        if( configs != null && configs.size() > 0)
            return configs;
        
        
        return configs;
    }
    
    /**
     * Query the application configuration model for a context with the 
     * specific URIs associated with the query context.
     */
    protected List<ApplicationConfig> basicQueryForConfigs( ConfigContext queryContext, String configurationForURI) 
    throws Exception{
        
        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("class", ResourceFactory.createResource( configurationForURI ));
                
        QueryExecution qexec = QueryExecutionFactory.create(basicQuery, appConfigModel, initialBindings) ;
        
        List<ContextResult> contextResults = new ArrayList<ContextResult>();
        appConfigModel.getLock().enterCriticalSection(Lock.READ);
        try {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; ) {
                QuerySolution soln = results.nextSolution() ;
                contextResults.addAll( makeConfig( 
                                soln.getResource("context"), 
                                soln.getResource("config"),
                                soln.getResource("configType")));                     
            }
        } finally { 
            appConfigModel.getLock().leaveCriticalSection();
            qexec.close() ; 
        }
        
        if( contextResults != null && contextResults.size() > 0 ){
            return configsForContextURIs( contextResults );
        }        
        
        return Collections.emptyList();
    }
     
    private Collection<? extends ContextResult> makeConfig(Resource resource,
            Resource resource2, Resource resource3) {
        if( resource == null || resource2 == null || resource3 == null)
            return Collections.emptyList();
        else{
            Collection<ContextResult> rv = new ArrayList<ContextResult>();            
            rv.add( new ContextResult(resource.getURI(),resource2.getURI(), resource3.getURI() ));
            return rv;
        }
    }

    private Query basicQuery = null;    
    /* basic query for a context that have no qualifications */
    private String basicQueryStr = 
        "SELECT ?context ?config ?configType WHERE {\n" +
//        " ?context <"+ CONFIG_CONTEXT_FOR +"> ?contextFor .\n" +
//        " OPTIONAL { ?context <" +QUALIFIED_BY +"> ?contextQualifier  } \n" +
//        " FILTER ( !bound( ?contextQualifier ) )\n" +                
        " ?config <"+ inheritingConfigurationFor.getURI()+"> ?class .\n" +
        " ?config a ?configType .\n" +         
        
        " ?context <"+ hasConfiguration.getURI() + "> ?config .\n"+
        "}";
    
    /** Get the configurations for the context */
    protected List<ApplicationConfig> configsForContextURIs(List<ContextResult> contextResults){        
        List<ApplicationConfig> configs = new ArrayList<ApplicationConfig>();
                
        configs.addAll( getClassDisplayConfigs( contextResults ));
        
        configs.addAll( getObjPropEditConfigs( contextResults ));
        configs.addAll( getObjPropDisplayConfigs( contextResults ));
        
        configs.addAll( getDataPropEditConfigs( contextResults ));
        configs.addAll( getDataPropDisplayConfigs( contextResults ));
                
        return configs;
    }
    
    private Collection<? extends ApplicationConfig> getDataPropDisplayConfigs(
            List<ContextResult> contextResults) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    private Collection<? extends ApplicationConfig> getDataPropEditConfigs(
            List<ContextResult> contextResults) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    private Collection<? extends ApplicationConfig> getObjPropDisplayConfigs(
            List<ContextResult> contextResults) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    private Collection<? extends ApplicationConfig> getObjPropEditConfigs(
            List<ContextResult> contextResults) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    private Collection<ClassDisplayConfig> getClassDisplayConfigs(
            List<ContextResult> contextResults) {
        ArrayList<ClassDisplayConfig> configs = new ArrayList<ClassDisplayConfig>();
        for( ContextResult result: contextResults){
            if( result == null  || isEmpty( result.configURI ) )
                continue;
            if( ClassDisplayConfig.getURI().equals( result.typeURI )){
                ClassDisplayConfig cdc = 
                    classDisplayConfigDao.getClassDisplayConfigByURI( result.configURI );
                if( cdc == null ){
                    log.debug("could not find ClassDisplayConfig with URI " + result.configURI);
                }else{
                    configs.add( cdc );
                }                    
            }
        }
        return configs;
    }

    /** make sure that the configContenx is reasonable. */
    protected void checkForValidContext(ConfigContext configContext) 
    throws Exception {
        if( configContext == null )
            throw new Exception("Configuration Context must not be null.");
        
        if( configContext.getConfigContextFor() == null )
            throw new Exception("Configuration Context getConfigContextFor() must not be null.");
        
        // Maybe check that getConfigContextFor() returns the 
        // URI of something in the system?
    }

    /* Simple structure to hold results */
    protected class ContextResult{
        String contextURI;
        String configURI;
        String typeURI;
        public ContextResult(String contextURI, String configURI, String typeURI) {
            super();
            this.contextURI = contextURI;
            this.configURI = configURI;
            this.typeURI = typeURI;
        }
                
    }


    
    private Query basicQualifiedQuery = null;
    /* basic query for a context with qualified  */
    private String basicQualifiedQueryStr =
        "SELECT ?context ?config ?configType WHERE {\n" +
        " ?context <"+ configContextFor.getURI() +"> ?contextFor .\n" +
        " ?context <"+ qualifiedBy.getURI() +"> ?contextQualifier .\n" +
        
        " ?context <"+ hasConfiguration.getURI() + "> ?config .\n"+        
        " ?config a ?configType .\n" + 
        "}";



}
