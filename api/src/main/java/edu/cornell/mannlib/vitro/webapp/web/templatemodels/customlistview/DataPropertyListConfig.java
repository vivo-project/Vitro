/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DataPropertyTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DataPropertyTemplateModel.ConfigError;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

public class DataPropertyListConfig {  
    private static final Log log = LogFactory.getLog(DataPropertyListConfig.class);
	
	
    private static final String CONFIG_FILE_PATH = "/config/";
    private static final String DEFAULT_CONFIG_FILE_NAME = "listViewConfig-dataDefault.xml";
    
    /* NB The default post-processor is not the same as the post-processor for the default view. The latter
     * actually defines its own post-processor, whereas the default post-processor is used for custom views
     * that don't define a post-processor, to ensure that the standard post-processing applies.
     * 
     * edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DefaultObjectPropertyDataPostProcessor
     */
    
    // TODO Lump these together into the PropertyListConfigContext
    private final DataPropertyTemplateModel dptm;
    private final VitroRequest vreq;
    private final TemplateLoader templateLoader;
    
    private boolean isDefaultConfig;
    private Set<String> constructQueries;
    private String selectQuery;
    private String templateName;

    public DataPropertyListConfig(DataPropertyTemplateModel dptm, TemplateLoader templateLoader, VitroRequest vreq, 
    		DataProperty dp, boolean editing) 
        throws InvalidConfigurationException {
    	
    	this.dptm = dptm;
    	this.vreq = vreq;
    	WebappDaoFactory wadf = vreq.getWebappDaoFactory();
    	this.templateLoader = templateLoader;

        // Get the custom config filename
        String configFileName = wadf.getDataPropertyDao().getCustomListViewConfigFileName(dp);
        if (configFileName == null) { // no custom config; use default config
            configFileName = DEFAULT_CONFIG_FILE_NAME;
        }
        log.debug("Using list view config file " + configFileName + " for data property " + dp.getURI());
        
        String configFilePath = getConfigFilePath(configFileName);
        
        try {
            File config = new File(configFilePath);            
            if ( ! isDefaultConfig(configFileName) && ! config.exists() ) {
                log.warn("Can't find config file " + configFilePath + " for data property " + dp.getURI() + "\n" +
                        ". Using default config file instead.");
                configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE_NAME);
                // Should we test for the existence of the default, and throw an error if it doesn't exist?
            }                   
            setValuesFromConfigFile(configFilePath, wadf, editing);           

        } catch (Exception e) {
            log.error("Error processing config file " + configFilePath + " for data property " + dp.getURI(), e);
            // What should we do here?
        }
        
        if ( ! isDefaultConfig(configFileName) ) {
            ConfigError configError = checkConfiguration();
            if ( configError != null ) { // the configuration contains an error
                log.warn("Invalid list view config for data property " + dp.getURI() + 
                        " in " + configFilePath + ":\n" +                            
                        configError + " Using default config instead.");
                configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE_NAME);
                setValuesFromConfigFile(configFilePath, wadf, editing);                    
            }
        }
        
        isDefaultConfig = isDefaultConfig(configFileName);
    }
    
    private boolean isDefaultConfig(String configFileName) {
        return configFileName.equals(DEFAULT_CONFIG_FILE_NAME);
    }
    
    private ConfigError checkConfiguration() {

        ConfigError error = dptm.checkQuery(selectQuery);
        if (error != null) {
            return error;
        }

        if (StringUtils.isBlank(selectQuery)) {
            return ConfigError.NO_SELECT_QUERY;
        }

        if ( StringUtils.isBlank(templateName)) {
           return ConfigError.NO_TEMPLATE;
        }

        try {
            if ( templateLoader.findTemplateSource(templateName) == null ) {
                return ConfigError.TEMPLATE_NOT_FOUND;
            }
        } catch (IOException e) {
            log.error("Error finding template " + templateName, e);
        }

        return null;
    }
    
    private void setValuesFromConfigFile(String configFilePath, WebappDaoFactory wdf, 
            boolean editing) {
		try {
			FileReader reader = new FileReader(configFilePath);
			CustomListViewConfigFile configFileContents = new CustomListViewConfigFile(reader);
			
			selectQuery = configFileContents.getSelectQuery(false, editing);
			templateName = configFileContents.getTemplateName();
			constructQueries = configFileContents.getConstructQueries();

		} catch (Exception e) {
			log.error("Error processing config file " + configFilePath, e);
		}
    }
    
    private String getConfigFilePath(String filename) {
        return vreq.getSession().getServletContext().getRealPath(CONFIG_FILE_PATH + filename);
    }

	public String getSelectQuery() {
		return this.selectQuery;
	}

	public Set<String> getConstructQueries() {
		return this.constructQueries;
	}

	public String getTemplateName() {
		return this.templateName;
	}

	public boolean isDefaultListView() {
		return this.isDefaultConfig;
	}

}