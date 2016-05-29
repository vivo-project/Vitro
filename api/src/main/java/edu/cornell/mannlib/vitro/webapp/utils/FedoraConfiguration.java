/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import fedora.client.FedoraClient;

public class FedoraConfiguration {

        private static String FEDORA_PROPERTIES = "/WEB-INF/fedora.properties";
        
        public String fedoraUrl = null;
        private String adminUser = null;
        private String adminPassword = null;
        private String pidNamespace = null;    
        private String configurationStatus = "<p>Fedora configuration not yet loaded</p>";
        private boolean configured = false;
        private boolean connected = false;
                    
        public FedoraConfiguration(ServletContext context){
            setup( context );
        }
                
        public boolean isConfigured(){ return configured; }
        public boolean isConnected(){ return connected; }
        
        public String setup(ServletContext context ){
            internalSetup( context);
            if( ! configured )
                return configurationStatus;
            canConnectToFedoraServer();
            return configurationStatus;
        }
        
        private void internalSetup(ServletContext context) {
            this.configurationStatus = "";
            StringBuffer status = new StringBuffer("");
            
            if( connected && configured )
                return;
            
            Properties props = new Properties();
            String path = context.getRealPath(FEDORA_PROPERTIES);
            try{            
                InputStream in = new FileInputStream(new File( path ));
                props.load( in );
                fedoraUrl = props.getProperty("fedoraUrl");
                adminUser = props.getProperty("adminUser");
                adminPassword = props.getProperty("adminPassword");
                pidNamespace = props.getProperty("pidNamespace");
                if( fedoraUrl == null || adminUser == null || adminPassword == null ){
                    if( fedoraUrl == null ){
                        log.error("'fedoraUrl' not found in properties file");        
                        status.append("<p>'fedoraUrl' not found in properties file.</p>\n");
                    }
                    if( adminUser == null ) {
                        log.error("'adminUser' was not found in properties file, the " +
                              "user name of the fedora admin is needed to access the " +
                                "fedora API-M services.");                    
                        status.append("<p>'adminUser' was not found in properties file, the " +
                                "user name of the fedora admin is needed to access the " +
                                  "fedora API-M services.</p>\n");
                    }
                    if( adminPassword == null ){
                        log.error("'adminPassword' was not found in properties file, the " +
                        "admin password is needed to access the fedora API-M services.");
                        status.append("<p>'adminPassword' was not found in properties file, the " +
                        "admin password is needed to access the fedora API-M services.</p>\n");
                    }
                    if( pidNamespace == null ){
                        log.error("'pidNamespace' was not found in properties file, the " +
                        "PID namespace indicates which namespace to use when creating " +
                        "new fedor digital objects.");
                        status.append("<p>'pidNamespace' was not found in properties file, the " +
                                "PID namespace indicates which namespace to use when creating " +
                                "new fedor digital objects.</p>\n");
                    } 
                    fedoraUrl = null; adminUser = null; adminPassword = null;
                    configured = false;
                }  else {
                    configured = true;
                }
            }catch(FileNotFoundException e){
                log.error("No fedora.properties file found,"+ 
                        "it should be located at " + path);
                status.append("<h1>Fedora configuration failed.</h1>\n");
                status.append("<p>No fedora.properties file found,"+ 
                        "it should be located at " + path + "</p>\n");
                configured = false;
                return;
            }catch(Exception ex){            
                status.append("<p>Fedora configuration failed.</p>\n");
                status.append("<p>Exception while loading" + path + "</p>\n");
                status.append("<p>" + ex.getMessage() + "</p>\n"); 
                log.error("could not load fedora properties", ex);
                fedoraUrl = null; adminUser = null; adminPassword = null;
                configured = false;
                return;
            }
                         
            status.append(RELOAD_MSG); 
            this.configurationStatus += status.toString();
        }

        public boolean canConnectToFedoraServer( ){
            try{
                FedoraClient fc = new FedoraClient(fedoraUrl,adminUser, adminPassword);
                String fedoraVersion = fc.getServerVersion();
                if( fedoraVersion != null && fedoraVersion.length() > 0 ){
                    configurationStatus += "<p>Fedora server is live and is running " +
                            "fedora version " + fedoraVersion + "</p>\n";
                    connected = true;
                    return true;
                } else {
                    configurationStatus += "<p>Unable to reach fedora server</p>\n";
                    connected = false;
                    return false;
                }
            }catch (Exception e) {
                configurationStatus += "<p>There was an error while checking the " +
                        "fedora server version</p>\n<p>"+ e.getMessage() + "</p>\n";
                connected = false;
                return false;            
            }                            
        }
   
        
        
        public static final Property FILE_NAME = ResourceFactory.createProperty(VitroVocabulary.FILE_NAME);
        public static final Property CONTENT_TYPE = ResourceFactory.createProperty(VitroVocabulary.CONTENT_TYPE);
        public static final Property FILE_LOCATION = ResourceFactory.createProperty(VitroVocabulary.FILE_LOCATION);
        //public static final Property FEDORA_ID = ResourceFactory.createProperty(VitroVocabulary.FEDORA_PID);
            
        DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();
        
        private static Log log = LogFactory.getLog(FedoraConfiguration.class);
        
        private static final String RELOAD_MSG = 
            "<p>The fedora configuartion file will be reloaded if " +
            "you edit the properties file and check the status.</p>\n";

        public String getFedoraUrl() {
            return fedoraUrl;
        }

        public void setFedoraUrl(String fedoraUrl) {
            this.fedoraUrl = fedoraUrl;
        }

        public String getAdminUser() {
            return adminUser;
        }

        public void setAdminUser(String adminUser) {
            this.adminUser = adminUser;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getPidNamespace() {
            return pidNamespace;
        }

        public void setPidNamespace(String pidNamespace) {
            this.pidNamespace = pidNamespace;
        }

        public String getConfigurationStatus() {
            return configurationStatus;
        }

        public void setConfigurationStatus(String configurationStatus) {
            this.configurationStatus = configurationStatus;
        }

}
