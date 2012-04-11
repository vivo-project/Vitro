/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.methods;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.web.images.PlaceholderUtil;
import freemarker.core.Environment;
import freemarker.template.TemplateModelException;

/**
 * Get a URL for the placeholder image for this Individual, based on the VClass 
 * that the Individual belongs to.
 */
public class IndividualPlaceholderImageUrlMethod extends BaseTemplateMethodModel {

    @SuppressWarnings("rawtypes")
    @Override
    public String exec(List args) throws TemplateModelException {
        if (args.size() != 1) {
            throw new TemplateModelException("Wrong number of arguments");
        }

        String uri = (String) args.get(0);        
        Environment env = Environment.getCurrentEnvironment();
        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        VitroRequest vreq = new VitroRequest(request);
        String imageUrl = PlaceholderUtil.getPlaceholderImagePathForIndividual(vreq, uri);
        return UrlBuilder.getUrl(imageUrl);
    }

    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("return value", "The URL of the placeholder image for this individual, " +
        		"based on the VClasses that the individual belongs to.");

        List<String>params = new ArrayList<String>();
        params.add("Uri of individual");
        map.put("parameters", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add(name + "(individual.uri)");
        map.put("examples", examples);
        
        return map;
    }
    
}
