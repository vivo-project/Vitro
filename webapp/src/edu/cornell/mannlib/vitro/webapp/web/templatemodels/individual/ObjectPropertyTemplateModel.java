/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public abstract class ObjectPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyTemplateModel.class);      
    private static final String TYPE = "object";
    /* NB The default preprocessor is not the same as the preprocessor for the default view. The latter
     * actually defines its own preprocessor, whereas the default preprocessor is used for custom views
     * that don't define a preprocessor.
     */
    private static final String DEFAULT_PREPROCESSOR = 
        "edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.BaseObjectPropertyDataPreprocessor";

    private PropertyListConfig config;

    ObjectPropertyTemplateModel(ObjectProperty op, Individual subject, WebappDaoFactory wdf) {
        super(op);
        setName(op.getDomainPublic());
        
        // Get the config for this object property
        try {
            config = new PropertyListConfig(op, wdf);
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
    
    protected static ObjectPropertyTemplateModel getObjectPropertyTemplateModel(ObjectProperty op, Individual subject, WebappDaoFactory wdf) {
        if (op.getCollateBySubclass()) {
            try {
                return new CollatedObjectPropertyTemplateModel(op, subject, wdf);
            } catch (Exception e) {
                log.error(e, e);
                return new UncollatedObjectPropertyTemplateModel(op, subject, wdf);
            }
        } else {
            return new UncollatedObjectPropertyTemplateModel(op, subject, wdf);
        }
    }
    
    /** Apply preprocessing to query results to prepare for template */
    protected void preprocess(List<Map<String, String>> data, WebappDaoFactory wdf) {
        String preprocessorName = config.preprocessor;
        if (preprocessorName == null) {
            preprocessorName = DEFAULT_PREPROCESSOR;
        }

        try {
            Class<?> preprocessorClass = Class.forName(preprocessorName);
            Constructor<?> constructor = preprocessorClass.getConstructor(ObjectPropertyTemplateModel.class, WebappDaoFactory.class);
            ObjectPropertyDataPreprocessor preprocessor = (ObjectPropertyDataPreprocessor) constructor.newInstance(this, wdf);
            preprocessor.process(data);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    private class PropertyListConfig {

        private static final String DEFAULT_CONFIG_FILE = "objectPropertyList-default.xml";
        private static final String CONFIG_FILE_PATH = "/config/";
        private static final String NODE_NAME_QUERY = "query";
        private static final String NODE_NAME_TEMPLATE = "template";
        private static final String NODE_NAME_COLLATION_TARGET = "collation-target";
        private static final String NODE_NAME_PREPROCESSOR = "preprocessor";
        
        private String queryString;
        private String templateName;
        private String collationTarget;
        private String preprocessor;

        PropertyListConfig(ObjectProperty op, WebappDaoFactory wdf) throws Exception {

            // Get the custom config filename
            ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
            String filename = opDao.getCustomListConfigFilename(op);
            if (filename == null) { // no custom config; use default config
                filename = DEFAULT_CONFIG_FILE;
            }
            log.debug("Using custom list view config file " + filename + " for object property " + op.getURI());
            
            String configFilePath = getConfigFilePath(filename);
            try {
                File config = new File(configFilePath);            
                if (configFilePath != DEFAULT_CONFIG_FILE && ! config.exists()) {
                    log.warn("Can't find config file " + configFilePath + " for object property " + op.getURI() + "\n" +
                            ". Using default config file instead.");
                    configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE);
                    // Should we test for the existence of the default, and throw an error if it doesn't exist?
                }   
            
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(configFilePath);
                // Required values
                queryString = getConfigValue(doc, NODE_NAME_QUERY);
                templateName = getConfigValue(doc, NODE_NAME_TEMPLATE);                
                // Optional values
                collationTarget = getConfigValue(doc, NODE_NAME_COLLATION_TARGET);
                preprocessor = getConfigValue(doc, NODE_NAME_PREPROCESSOR);
            } catch (Exception e) {
                log.error("Error processing config file " + configFilePath + " for object property " + op.getURI(), e);
                // What should we do here?
            }
            
            if (queryString == null) {
                throw new Exception("Invalid custom view configuration: query string not defined.");                
            }
            if (templateName == null) {
                throw new Exception("Invalid custom view configuration: template name not defined.");
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
