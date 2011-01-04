/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/** 
 * Template directive to be used to get image src values. 
 * Parameters are not needed, therefore not supported.
 * @author rjy7
 *
 */
public class UrlDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(UrlDirective.class);
    
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The url directive doesn't allow loop variables.");
        }
        
        if (body != null) {
            throw new TemplateModelException(
                "The url directive doesn't allow nested content.");
        } 
        
        String path = params.get("path").toString();
        if (path == null) {
            throw new TemplateModelException(
                "The url directive requires a value for parameter 'path'.");
        }
        
        if (!path.startsWith("/")) {
            throw new TemplateModelException(
                "The url directive requires that the value of parameter 'path' is an absolute path starting with '/'.");
        }
        
        String url = UrlBuilder.getUrl(path);
        Writer out = env.getOut();
        out.write(url);
    }

    public String help(Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        map.put("effect", "Generate a full url from a path. Use for generating src attribute of image tags.");
        
        map.put("comments", "The path should be an absolute path, starting with '/'.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("path", "path");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " path=\"/images/placeholders/person.thumbnail.jpg\" />");
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

    
}
