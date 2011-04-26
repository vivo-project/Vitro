/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageMenus;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class MenuN3EditController extends FreemarkerHttpServlet {

    protected final static String N3MENU_FORM = "menuN3Edit.ftl"; 
    protected final static String N3MENU_SUCCESS_RESULT = "menuN3Edit.ftl";     
    protected final static String N3MENU_ERROR_RESULT = "menuN3Edit.ftl";
    
    protected final static String N3_PARAM = "navigationN3";
    
    public final static Actions REQUIRED_ACTIONS = new Actions(new ManageMenus());
    
    @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return REQUIRED_ACTIONS;
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        String n3 = vreq.getParameter(N3_PARAM); 
        if( n3 != null &&  ! n3.isEmpty()){
            return setNewMenu(vreq);        
        }else{
            return showForm(vreq);
        }
    }

    private ResponseValues showForm(VitroRequest vreq) {
        Map<String,Object> data = new HashMap<String,Object>();
        
        String menuN3;                
        try {
            menuN3 = vreq.getWebappDaoFactory().getDisplayModelDao()
                    .getDisplayModel(getServletContext());
            data.put("menuN3", menuN3);
            data.put("cancelUrl", "/siteAdmin");
        } catch (Exception e) {
            data.put("errorMessage",e.getMessage());
        }        
        return new TemplateResponseValues(N3MENU_FORM, data);
    }

    private ResponseValues setNewMenu(VitroRequest vreq) {
        Map<String,Object> data = new HashMap<String,Object>();
        
        String menuN3 = vreq.getParameter(N3_PARAM);
        
        try {
            vreq.getWebappDaoFactory().getDisplayModelDao()
                .replaceDisplayModel(menuN3, getServletContext());
            data.put("message", "success");
        } catch (Exception e) {
            data.put("errorMessage",e.getMessage());
        }
        
        if( data.containsKey("errorMessage"))            
            return new TemplateResponseValues(N3MENU_ERROR_RESULT,data);
        else
            return new TemplateResponseValues(N3MENU_SUCCESS_RESULT, data);
            
    }

    
}
