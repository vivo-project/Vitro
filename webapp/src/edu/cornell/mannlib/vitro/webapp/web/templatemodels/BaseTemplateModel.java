/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Params;

public abstract class BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseTemplateModel.class.getName());
    
    public static ServletContext context = null;

    // Wrap UrlBuilder method so templates can call ${item.url}
    public String getUrl(String path) {
        return UrlBuilder.getUrl(path);
    }

    // Wrap UrlBuilder method so templates can call ${item.url}
    public String getUrl(String path, Params params) {
        return UrlBuilder.getUrl(path, params);
    }

    /*
     * public static List<?> wrapList(List<?> list, Class cl) 
     * throw error if cl not a child of ViewObject
     * This block of code is going to be repeated a lot:
            List<VClassGroup> groups = // code to get the data
            List<VClassGroupView> vcgroups = new ArrayList<VClassGroupView>(groups.size());
            Iterator<VClassGroup> i = groups.iterator();
            while (i.hasNext()) {
                vcgroups.add(new VClassGroupView(i.next()));
            }
            body.put("classGroups", vcgroups);
    Can we generalize it to a generic method of ViewObject - wrapList() ? 
    static method of ViewObject
    Params: groups, VClassGroupView (the name of the class) - but must be a child of ViewObject
    Return: List<viewObjectType>
     */

}
