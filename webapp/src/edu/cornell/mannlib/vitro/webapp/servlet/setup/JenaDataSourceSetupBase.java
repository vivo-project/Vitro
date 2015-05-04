/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoCon;

public class JenaDataSourceSetupBase extends JenaBaseDaoCon {
	private static final Log log = LogFactory.getLog(
			JenaDataSourceSetupBase.class);
	
    private static final String VITRO_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

    
    private static boolean firstStartup = false;

   public static boolean isFirstStartup() {
       return firstStartup;
   }
   
   public static void thisIsFirstStartup(){
       firstStartup = true;
   }
   
    protected String getDefaultNamespace(ServletContext ctx) {
        String dns = ConfigurationProperties.getBean(ctx).getProperty(
                VITRO_DEFAULT_NAMESPACE);
        if ((dns != null) && (!dns.isEmpty())) {
            return dns;
        } else {
            throw new IllegalStateException("runtime.properties does not "
                    + "contain a value for '" + VITRO_DEFAULT_NAMESPACE + "'");
        }
    }
    
}
