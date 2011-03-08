/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;

/**
 * Class used to setup a JenaNetidPolicy using the default.  
 * This setups the JenaNetidPolicy and a SelfEditingIdentifierFactory.
 * 
 * See JenaNetidPolicy.setupDefault() for the sparql queries that will
 * be used by the default JenaNetidPolicy.
 *
 * @author bdc34
 *
 */
public class JenaNetidPolicySetup implements ServletContextListener  {
	
	private static final Log log = LogFactory.getLog(JenaNetidPolicySetup.class.getName());

    @Override
	public void contextInitialized(ServletContextEvent sce) {
        try{
            log.debug("Setting up JenaNetidPolicy");

            JenaNetidPolicy jnip = new JenaNetidPolicy((OntModel) sce.getServletContext().getAttribute("jenaOntModel"));
            ServletPolicyList.addPolicy(sce.getServletContext(), jnip);

            SelfEditingIdentifierFactory niif =new SelfEditingIdentifierFactory();
            ActiveIdentifierBundleFactories.addFactory(sce, niif);

        }catch(Exception e){
            log.error("could not create AuthorizationFactory: " + e);
            e.printStackTrace();
        }
    }

    @Override
	public void contextDestroyed(ServletContextEvent sce) {
        /*nothing*/
    }

}

