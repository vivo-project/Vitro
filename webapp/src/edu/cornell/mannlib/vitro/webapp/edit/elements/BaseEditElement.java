/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class BaseEditElement  implements EditElement {
    private static final Log log = LogFactory.getLog(BaseEditElement.class);

    protected Field field;
    
    public BaseEditElement(Field field){
        this.field = field;
    }
    
    /**
     * Utility method for use in EditElements to merge a freemarker template.
     */
    protected String merge(Configuration fmConfig, String templateName, Map map){
        Template template = null;
        try {
            template = fmConfig.getTemplate(templateName);
        } catch (IOException e) {
            log.error("Cannot get template " + templateName);
        }  
         
        StringWriter writer = new StringWriter();
        try {
            template.process(map, writer);
        } catch (TemplateException e) {
            log.error(e,e);
        } catch (IOException e) {
            log.error(e,e);
        }
        return writer.toString();        
    }

    /**
     * Utility method to check if a value from the query parameters is none or a single value.
     * This returns true if the key is there and the value is null.
     * This does not check if the value is the empty string.
     */
    protected boolean hasNoneOrSingle(String key, Map<String, String[]> queryParameters){
        if( queryParameters != null ){
            if( ! queryParameters.containsKey(key) )
                return true; //none            
            String[] vt = queryParameters.get(key);
            return vt == null || vt.length == 0 || vt.length==1;
        }else{
            log.error("passed null queryParameters");
            return false;
        }
    }
    
    protected boolean hasSingleNonNullNonEmptyValueForKey(String key, Map<String, String[]> queryParameters){
        if( queryParameters != null ){
            if( ! queryParameters.containsKey(key) )
                return true; //none            
            String[] vt = queryParameters.get(key);
            return vt != null && vt.length == 1 && vt[0] != null && ! vt[0].isEmpty() ;
        }else{
            log.error("passed null queryParameters");
            return false;
        }
    }
}
