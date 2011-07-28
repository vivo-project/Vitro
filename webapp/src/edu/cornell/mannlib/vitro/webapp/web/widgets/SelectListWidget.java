/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.SelectListGenerator;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;

public class SelectListWidget extends Widget {
    private static final Log log = LogFactory.getLog(SelectListWidget.class);
    
    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {        
        
        Object obj = params.get("fieldName");
        if( obj == null  || !(obj instanceof SimpleScalar)){
            log.error("SelectListWidget must have a parameter 'fieldName'");
            throw new Error("SelectListWidget must have a parameter'fieldName'");
        }
        String fieldName = ((SimpleScalar)obj).getAsString();
        if( fieldName.isEmpty() ){
            log.error("SelectListWidget must have a parameter 'fieldName'");        
            throw new Error("SelectListWidget must have a parameter 'fieldName' of type String");
        }
        
        VitroRequest vreq = new VitroRequest(request);        
        HttpSession session = request.getSession(false);
        EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);                
        
        WebappDaoFactory wdf;
        if (editConfig != null) { 
            wdf = editConfig.getWdfSelectorForOptons().getWdf(vreq,context);
        } else {                
            wdf = vreq.getWebappDaoFactory();
        }
        
        Map<String,String> selectOptions =  SelectListGenerator.getOptions(editConfig, fieldName, wdf);                                  
        Map<String,Object> rmap = new HashMap<String,Object>();
        rmap.put("selectList", selectOptions);
        
        return new WidgetTemplateValues("markup", rmap);
    }

}
