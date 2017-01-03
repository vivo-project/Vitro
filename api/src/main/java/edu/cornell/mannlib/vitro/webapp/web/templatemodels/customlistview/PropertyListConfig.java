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

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.CollatedObjectPropertyTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DefaultObjectPropertyDataPostProcessor;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ObjectPropertyDataPostProcessor;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ObjectPropertyTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ObjectPropertyTemplateModel.ConfigError;
import freemarker.cache.TemplateLoader;

public class PropertyListConfig {  
    private static final Log log = LogFactory.getLog(PropertyListConfig.class);
	
	
    private static final String CONFIG_FILE_PATH = "/config/";
    private static final String DEFAULT_CONFIG_FILE_NAME = "listViewConfig-default.xml";
    
    /* NB The default post-processor is not the same as the post-processor for the default view. The latter
     * actually defines its own post-processor, whereas the default post-processor is used for custom views
     * that don't define a post-processor, to ensure that the standard post-processing applies.
     * 
     * edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DefaultObjectPropertyDataPostProcessor
     */
    
    // TODO Lump these together into the PropertyListConfigContext
    private final ObjectPropertyTemplateModel optm;
    private final VitroRequest vreq;
    private final TemplateLoader templateLoader;
    
    private boolean isDefaultConfig;
    private Set<String> constructQueries;
    private String selectQuery;
    private String templateName;
    private ObjectPropertyDataPostProcessor postprocessor; // never null

    public PropertyListConfig(ObjectPropertyTemplateModel optm, TemplateLoader templateLoader, VitroRequest vreq, 
    		ObjectProperty op, boolean editing) 
        throws InvalidConfigurationException {
    	
    	this.optm = optm;
    	this.vreq = vreq;
    	WebappDaoFactory wadf = vreq.getWebappDaoFactory();
    	this.templateLoader = templateLoader;

        // Get the custom config filename
        String configFileName = wadf.getObjectPropertyDao().getCustomListViewConfigFileName(op);
        if (configFileName == null) { // no custom config; use default config
            configFileName = DEFAULT_CONFIG_FILE_NAME;
        } else {
        	CustomListViewLogger.log(op, configFileName);
        }
        log.debug("Using list view config file " + configFileName + " for object property " + op.getURI());
        
        String configFilePath = getConfigFilePath(configFileName);
        
        try {
            File config = new File(configFilePath);            
            if ( ! isDefaultConfig(configFileName) && ! config.exists() ) {
                log.warn("Can't find config file " + configFilePath + " for object property " + op.getURI() + "\n" +
                        ". Using default config file instead.");
                configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE_NAME);
                // Should we test for the existence of the default, and throw an error if it doesn't exist?
            }                   
            setValuesFromConfigFile(configFilePath, wadf, editing);           

        } catch (Exception e) {
            log.error("Error processing config file " + configFilePath + " for object property " + op.getURI(), e);
            // What should we do here?
        }
        
        if ( ! isDefaultConfig(configFileName) ) {
            ConfigError configError = checkConfiguration();
            if ( configError != null ) { // the configuration contains an error
                // If this is a collated property, throw an error: this results in creating an 
                // UncollatedPropertyTemplateModel instead.
                if (optm instanceof CollatedObjectPropertyTemplateModel) {
                    throw new InvalidConfigurationException(configError.getMessage());
                }
                // Otherwise, switch to the default config
                log.warn("Invalid list view config for object property " + op.getURI() + 
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

        ConfigError error = optm.checkQuery(selectQuery);
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
			
			boolean collated = optm instanceof CollatedObjectPropertyTemplateModel;

			selectQuery = configFileContents.getSelectQuery(collated, editing);
			templateName = configFileContents.getTemplateName();
			constructQueries = configFileContents.getConstructQueries();

			String postprocessorName = configFileContents.getPostprocessorName();
			postprocessor = getPostProcessor(postprocessorName, optm, wdf, configFilePath);
		} catch (Exception e) {
			log.error("Error processing config file " + configFilePath, e);
		}
    }
    
	private ObjectPropertyDataPostProcessor getPostProcessor(
			String className,
			ObjectPropertyTemplateModel optm,
			WebappDaoFactory wdf, String configFilePath) {
		try {
			if (StringUtils.isBlank(className)) {
				return new DefaultObjectPropertyDataPostProcessor(optm, wdf);
			}
			
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor(ObjectPropertyTemplateModel.class, WebappDaoFactory.class);
			return (ObjectPropertyDataPostProcessor) constructor.newInstance(optm, wdf);
		} catch (ClassNotFoundException e) {
			log.warn("Error processing config file '" + configFilePath
					+ "': can't load postprocessor class '" + className
					+ "'. " + "Using default postprocessor.", e);
			return new DefaultObjectPropertyDataPostProcessor(optm, wdf);
		} catch (NoSuchMethodException e) {
			log.warn("Error processing config file '" + configFilePath
					+ "': postprocessor class '" + className
					+ "' does not have a constructor that takes "
					+ "ObjectPropertyTemplateModel and WebappDaoFactory. "
					+ "Using default postprocessor.", e);
			return new DefaultObjectPropertyDataPostProcessor(optm, wdf);
		} catch (ClassCastException e) {
			log.warn("Error processing config file '" + configFilePath
					+ "': postprocessor class '" + className + "' does "
					+ "not implement ObjectPropertyDataPostProcessor. "
					+ "Using default postprocessor.", e);
			return new DefaultObjectPropertyDataPostProcessor(optm, wdf);
		} catch (Exception e) {
			log.warn("Error processing config file '" + configFilePath
					+ "': can't create postprocessor instance of class '"
					+ className + "'. " + "Using default postprocessor.", e);
			return new DefaultObjectPropertyDataPostProcessor(optm, wdf);
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

	public ObjectPropertyDataPostProcessor getPostprocessor() {
		return this.postprocessor;
	}
}