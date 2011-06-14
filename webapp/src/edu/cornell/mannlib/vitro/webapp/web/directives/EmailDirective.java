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
      
        Object o = params.get("subject");
        if (o == null) {
            throw new TemplateModelException(
                "The email directive requires a value for parameter 'subject'.");
        }        
        if (! ( o instanceof SimpleScalar)) {
            throw new TemplateModelException(
                "The email directive requires a string value for parameter 'subject'.");
        }        
        String subject = o.toString();         

        o = params.get("html");
        if (o == null) {
            throw new TemplateModelException(
                "The email directive requires a value for parameter 'html'.");
        }        
        if (! ( o instanceof SimpleScalar)) {
            throw new TemplateModelException(
                "The email directive requires a string value for parameter 'html'.");
        }        
        String html = o.toString();
        
        o = params.get("text");
        if (o == null) {
            throw new TemplateModelException(
                "The email directive requires a value for parameter 'text'.");
        }        
        if (! ( o instanceof SimpleScalar)) {
            throw new TemplateModelException(
                "The email directive requires a string value for parameter 'text'.");
        }        
        String text = o.toString(); 

        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        
        o = (FreemarkerEmailMessage) request.getAttribute("emailMessage");        
        if ( o == null) {
            throw new TemplateModelException(
                "No email message object found in the request.");
        }
        if ( ! (o instanceof FreemarkerEmailMessage)) {
            throw new TemplateModelException(
                "Invalid value for request email attribute");
        }
        FreemarkerEmailMessage email = (FreemarkerEmailMessage) o;
        email.send(subject, html, text);
        
    }
    
    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("effect", "Create an email message from the parameters set in the invoking template.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("subject", "email subject");
        params.put("html", "HTML version of email message");
        params.put("text", "Plain text version of email message");
        map.put("parameters", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("&lt;email subject=\"Password reset confirmation\" html=html text=text&gt;");
        map.put("examples", examples);
        
        return map;
    }
}
