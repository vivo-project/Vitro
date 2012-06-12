/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;


import static edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ApplicationConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.context.ConfigContext;

public class AppConfigContextServiceJenaTest {
    OntModel appModel;
    
    String ns = "http://example.com/";
    
    String contextURI = ns+"context1";
    String classURI = ns+"class1";
    String propertyURI = ns+"property";
    String configURI = ns+"config1";
    
    String NAME = "display Name";
    
    
    @Before
    public void setUp() throws Exception {                
               
    }
    
    @Test
    public void testBasicClassConfig() throws Exception{
        
        //setup a app config model with a display name for a class        
        appModel = ModelFactory.createOntologyModel();
        
        Resource targetClass = appModel.createResource( classURI );
        
        Individual config = appModel.createIndividual( configURI, ClassDisplayConfig);
        config.addProperty(displayName, NAME );
        config.addProperty( inheritingConfigurationFor, targetClass);
        
        Individual context = appModel.createIndividual( contextURI , ConfigContext);
        context.addProperty(hasConfiguration, config);                                
        
        AppConfigContextServiceJena accService = new AppConfigContextServiceJena( appModel , ns );
        
        ConfigContext cc = new ConfigContextJena();
        cc.configContextFor( classURI );        
        
        List<ApplicationConfig> configListForCC = accService.getConfigsForContext( cc, classURI );
        Assert.assertNotNull( configListForCC );
        Assert.assertEquals(1, configListForCC.size());
        
    }
}


