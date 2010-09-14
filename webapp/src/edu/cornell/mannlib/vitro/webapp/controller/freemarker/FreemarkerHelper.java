/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerHelper {
    
    private static final Log log = LogFactory.getLog(FreemarkerHelper.class);
    
    private Configuration config = null;
    
    public FreemarkerHelper(Configuration config) {
        this.config = config;
    }

    public StringWriter mergeToTemplate(String templateName, Map<String, Object> map) {
        
        Template template = null;
        try {
            template = config.getTemplate(templateName);
        } catch (IOException e) {
            log.error("Cannot get template " + templateName);
        }
        StringWriter sw = new StringWriter();
        if (template != null) {         
            try {
                template.process(map, sw);
            } catch (TemplateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
        }
        return sw;
    }
   

    public String mergeMapToTemplate(String templateName, Map<String, Object> map) {
        return mergeToTemplate(templateName, map).toString();
    }
    
}
