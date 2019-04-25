/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class BaseEditElementVTwo  implements EditElementVTwo {
    private static final Log log = LogFactory.getLog(BaseEditElementVTwo.class);

    protected FieldVTwo field;

    public BaseEditElementVTwo(FieldVTwo field){
        this.field = field;
    }

    public void setField(FieldVTwo field){
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
        } catch (TemplateException | IOException e) {
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
