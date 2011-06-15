/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailMessage;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class EmailDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(EmailDirective.class);
    
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        FreemarkerEmailMessage email = null;

        Object paramValue = (FreemarkerEmailMessage) request.getAttribute("emailMessage");        
        if ( paramValue == null) {
            throw new TemplateModelException(
                "No email message object found in the request.");
        }
        if ( ! (paramValue instanceof FreemarkerEmailMessage)) {
            throw new TemplateModelException(
                "Invalid value for request email attribute");
        }
        email = (FreemarkerEmailMessage) paramValue;       

        
        // Read in parameter values. If a value is undefined by the template, the
        // default values defined by the email object will be used.
        String subject = null;
        paramValue = params.get("subject");    
        if (paramValue != null && paramValue instanceof SimpleScalar) {
            subject = paramValue.toString();  
        }  

        String html = null;
        paramValue = params.get("html");
        if (paramValue != null && paramValue instanceof SimpleScalar) {
            html = paramValue.toString();  
        }            
        
        String text = null;
        paramValue = params.get("text");
        if (paramValue != null && paramValue instanceof SimpleScalar) {
            text = paramValue.toString();  
        } 

        email.send(subject, html, text);       
    }
    
    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("effect", "Create an email message from the parameters set in the invoking template.");
        map.put("comment", "Parameter values undefined by the template will be provided by controller default values.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("subject", "email subject (optional)");
        params.put("html", "HTML version of email message (optional)");
        params.put("text", "Plain text version of email message (optional)");
        map.put("parameters", params);

        List<String> examples = new ArrayList<String>();
        examples.add("&lt;email subject=\"Password reset confirmation\" html=html text=text&gt;");
        examples.add("&lt;email html=html text=text&gt;");
        map.put("examples", examples);
        
        return map;
    }
}
