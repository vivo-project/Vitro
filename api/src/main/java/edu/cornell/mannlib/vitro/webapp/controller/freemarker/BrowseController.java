/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "browsecontroller", urlPatterns = {"/browse"}, loadOnStartup = 5)
public class BrowseController extends FreemarkerHttpServlet {
    static final long serialVersionUID=2006030721126L;

    private static final Log log = LogFactory.getLog(BrowseController.class);

    private static final String TEMPLATE_DEFAULT = "classGroups.ftl";

    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return "Index of Contents";
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        String templateName = TEMPLATE_DEFAULT;

        List<VClassGroup> groups = null;
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        groups =vcgc.getGroups();
        List<VClassGroupTemplateModel> vcgroups = new ArrayList<VClassGroupTemplateModel>(groups.size());
        for (VClassGroup group : groups) {
            vcgroups.add(new VClassGroupTemplateModel(group));
        }
        body.put("classGroups", vcgroups);

        return new TemplateResponseValues(templateName, body);
    }
}
