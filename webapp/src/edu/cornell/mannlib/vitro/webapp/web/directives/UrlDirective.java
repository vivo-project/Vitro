/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
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
        
        Object o = params.get("path");
        if (o == null) {
            throw new TemplateModelException(
                "The url directive requires a value for parameter 'path'.");
        }
        
        if (! ( o instanceof SimpleScalar)) {
            throw new TemplateModelException(
                "The url directive requires a string value for parameter 'path'.");
        }
        
        String path = o.toString();
        
        if (!path.startsWith("/")) {
            throw new TemplateModelException(
                "The url directive requires that the value of parameter 'path' is an absolute path starting with '/'.");
        }
        
        String url = UrlBuilder.getUrl(path);
        Writer out = env.getOut();
        out.write(url);
    }

    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("effect", "Generate a full url from a path by prepending the servlet context path. Use for generating src attribute of image tags, href attribute of anchor tags, etc.");
        
        map.put("comments", "The path should be an absolute path, starting with \"/\".");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("path", "path");
        map.put("parameters", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("&lt;img src=\"<@url path=\"/images/placeholders/person.thumbnail.jpg\" />\" /&gt;" );
        map.put("examples", examples);
        
        return map;
    }
    
}
