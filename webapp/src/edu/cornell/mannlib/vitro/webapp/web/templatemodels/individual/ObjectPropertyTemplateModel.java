/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

public abstract class ObjectPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyTemplateModel.class);      
    private static final String TYPE = "object";

    private PropertyListConfig config;

    ObjectPropertyTemplateModel(ObjectProperty op, Individual subject, VitroRequest vreq) {
        super(op);
        setName(op.getDomainPublic());
        
        // Get the config for this object property
        try {
            config = new PropertyListConfig(op, vreq);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    protected String getQueryString() {
        return config.queryString;
    }
    
    protected String getCollationTarget() {
        return config.collationTarget;
    }
    
    protected boolean hasCustomListView() {
        return !config.isDefaultConfig;
    }
    
    protected static ObjectPropertyTemplateModel getObjectPropertyTemplateModel(ObjectProperty op, Individual subject, VitroRequest vreq) {
        if (op.getCollateBySubclass()) {
            try {
                return new CollatedObjectPropertyTemplateModel(op, subject, vreq);
            } catch (Exception e) {
                log.error(e, e);
                return new UncollatedObjectPropertyTemplateModel(op, subject, vreq);
            }
        } else {
            return new UncollatedObjectPropertyTemplateModel(op, subject, vreq);
        }
    }
    
    /** Apply postprocessing to query results to prepare for template */
    protected void postprocess(List<Map<String, String>> data, WebappDaoFactory wdf) {
        String postprocessorName = config.postprocessor;
        if (postprocessorName == null) {
            return;
        }

        try {
            Class<?> postprocessorClass = Class.forName(postprocessorName);
            Constructor<?> constructor = postprocessorClass.getConstructor(ObjectPropertyTemplateModel.class, WebappDaoFactory.class);
            ObjectPropertyDataPostprocessor postprocessor = (ObjectPropertyDataPostprocessor) constructor.newInstance(this, wdf);
            postprocessor.process(data);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    private class PropertyListConfig {

        private static final String DEFAULT_CONFIG_FILE = "listViewConfig-default.xml";
        private static final String CONFIG_FILE_PATH = "/config/";
        private static final String NODE_NAME_QUERY = "query";
        private static final String NODE_NAME_TEMPLATE = "template";
        private static final String NODE_NAME_COLLATION_TARGET = "collation-target";
        private static final String NODE_NAME_POSTPROCESSOR = "postprocessor";
        
        private boolean isDefaultConfig;
        private String queryString;
        private String templateName;
        private String collationTarget;
        private String postprocessor;

        PropertyListConfig(ObjectProperty op, VitroRequest vreq) throws Exception {

            // Get the custom config filename
            WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
            String configFileName = opDao.getCustomListConfigFileName(op);
            if (configFileName == null) { // no custom config; use default config
                configFileName = DEFAULT_CONFIG_FILE;
            }
            log.debug("Using list view config file " + configFileName + " for object property " + op.getURI());
            
            String configFilePath = getConfigFilePath(configFileName);
            try {
                File config = new File(configFilePath);            
                if ( ! isDefaultConfig(configFileName) && ! config.exists() ) {
                    log.warn("Can't find config file " + configFilePath + " for object property " + op.getURI() + "\n" +
                            ". Using default config file instead.");
                    configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE);
                    // Should we test for the existence of the default, and throw an error if it doesn't exist?
                }                   
                setValuesFromConfigFile(configFilePath);           

            } catch (Exception e) {
                log.error("Error processing config file " + configFilePath + " for object property " + op.getURI(), e);
                // What should we do here?
            }
            
            if ( ! isDefaultConfig(configFileName) ) {
                String invalidConfigMessage = checkForInvalidConfig(vreq);
                if ( StringUtils.isNotEmpty(invalidConfigMessage) ) {
                    log.warn("Invalid list view config for object property " + op.getURI() + 
                            " in " + configFilePath + ":\n" +                            
                            invalidConfigMessage + " Using default config instead.");
                    configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE);
                    setValuesFromConfigFile(configFilePath);                    
                }
            }
            
            isDefaultConfig = isDefaultConfig(configFileName);
        }
        
        private boolean isDefaultConfig(String configFileName) {
            return configFileName.equals(DEFAULT_CONFIG_FILE);
        }
        
        private String checkForInvalidConfig(VitroRequest vreq) {
            String invalidConfigMessage = null;

            if ( StringUtils.isBlank(queryString)) {
                invalidConfigMessage = "Missing query specification.";
            } else if ( StringUtils.isBlank(templateName)) {
                invalidConfigMessage = "Missing template specification.";
            } else {
                Configuration fmConfig = (Configuration) vreq.getAttribute("freemarkerConfig");
                TemplateLoader tl = fmConfig.getTemplateLoader();
                try {
                    if ( tl.findTemplateSource(templateName) == null ) {
                        invalidConfigMessage = "Specified template " + templateName + " does not exist.";
                    }
                } catch (IOException e) {
                    log.error("Error finding template " + templateName, e);
                }
            }
            return invalidConfigMessage;
        }
        
        private void setValuesFromConfigFile(String configFilePath) {
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
 
            try {
                db = dbf.newDocumentBuilder();
                Document doc = db.parse(configFilePath);
                // Required values
                queryString = getConfigValue(doc, NODE_NAME_QUERY);
                templateName = getConfigValue(doc, NODE_NAME_TEMPLATE); 
                
                // Optional values
                collationTarget = getConfigValue(doc, NODE_NAME_COLLATION_TARGET);
                postprocessor = getConfigValue(doc, NODE_NAME_POSTPROCESSOR);
            } catch (Exception e) {
                log.error("Error processing config file " + configFilePath, e);
                // What should we do here?
            }            
        }
 
        private String getConfigValue(Document doc, String nodeName) {
            NodeList nodes = doc.getElementsByTagName(nodeName);
            Element element = (Element) nodes.item(0); 
            String value = null;
            if (element != null) {
                value = element.getChildNodes().item(0).getNodeValue();   
                log.debug("Value of config parameter " + nodeName + " = " + value);
            } else {
                log.warn("No value for config parameter " + nodeName);
            }
            return value;           
        }
        
        private String getConfigFilePath(String filename) {
            return servletContext.getRealPath(CONFIG_FILE_PATH + filename);
        }
    }
    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }
    
    public String getTemplate() {
        return config.templateName;
    }
    
    public abstract boolean isCollatedBySubclass();

    @Override
    public String getAddLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getEditLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getDeleteLink() {
        // TODO Auto-generated method stub
        return null;
    }
}
