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

    public StringWriter mergeToTemplate(String templateName, Map<String, Object> map, Configuration config) {
        
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

    public String mergeMapToTemplate(String templateName, Map<String, Object> map, Configuration config) {
        return mergeToTemplate(templateName, map, config).toString();
    }
    
}
