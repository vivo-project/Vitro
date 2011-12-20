/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildVClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;

public class BrowseController extends FreemarkerHttpServlet {
    static final long serialVersionUID=2006030721126L;
    
    private static final Log log = LogFactory.getLog(BrowseController.class);
    
    private static final String TEMPLATE_DEFAULT = "classGroups.ftl";
     
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return "Index of Contents";
    }
    
    
    @Override
    protected Actions requiredActions(VitroRequest vreq) {
        if ( vreq.getParameter("clearcache") != null )
            return new Actions(new RebuildVClassGroupCache() );
        else
            return Actions.AUTHORIZED;
    }


    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        
        Map<String, Object> body = new HashMap<String, Object>();
        String message = null;
        String templateName = TEMPLATE_DEFAULT;
                       
        if ( vreq.getParameter("clearcache") != null ) {
            //mainly for debugging
            if( PolicyHelper.isAuthorizedForActions(vreq, new RebuildVClassGroupCache()) ){
                clearGroupCache();
            }
        }
        
        List<VClassGroup> groups = null;
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(getServletContext());
        if ( vcgc == null ) {
            log.error("Could not get VClassGroupCache");
            message = "The system is not configured correctly. Please check your logs for error messages.";
        } else {
            groups =vcgc.getGroups();
            List<VClassGroupTemplateModel> vcgroups = new ArrayList<VClassGroupTemplateModel>(groups.size());
            for (VClassGroup group : groups) {
                vcgroups.add(new VClassGroupTemplateModel(group));
            }
            body.put("classGroups", vcgroups);
        }
        
        if (message != null) {
            body.put("message", message);
            templateName = Template.TITLED_MESSAGE.toString();
        }
        

        
        return new TemplateResponseValues(templateName, body);
    }
    
    protected void clearGroupCache(){
        VClassGroupCache.getVClassGroupCache(getServletContext()).doSynchronousRebuild();
    }
}