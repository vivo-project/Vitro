/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;

public class Properties2PropertiesRetryController extends BaseEditController {

	private static final Log log = LogFactory.getLog(Properties2PropertiesRetryController.class.getName());

    public void doGet (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);
        
        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        ObjectPropertyDao opDao = request.getUnfilteredWebappDaoFactory().getObjectPropertyDao();
        DataPropertyDao dpDao = request.getUnfilteredWebappDaoFactory().getDataPropertyDao();
        epo.setDataAccessObject(opDao);
        
        List propList = ("data".equals(request.getParameter("propertyType"))) 
    	? dpDao.getAllDataProperties()
    	: opDao.getAllObjectProperties();
        
    	sortForPickList(propList, request);
    	
    	 String superpropertyURIstr = request.getParameter("SuperpropertyURI");
         String subpropertyURIstr = request.getParameter("SubpropertyURI");
       
        HashMap<String,Option> hashMap = new HashMap<String,Option>();
        List<Option> optionList = FormUtils.makeOptionListFromBeans(propList,"URI","PickListName",superpropertyURIstr,null);
        List<Option> superPropertyOptions = getSortedList(hashMap, optionList, request);
        optionList = FormUtils.makeOptionListFromBeans(propList,"URI","PickListName",subpropertyURIstr,null);
        List<Option> subPropertyOptions = getSortedList(hashMap, optionList, request);
        
        HashMap hash = new HashMap();
    	hash.put("SuperpropertyURI", superPropertyOptions);
        hash.put("SubpropertyURI", subPropertyOptions);
        
        FormObject foo = new FormObject();
        foo.setOptionLists(hash);

        epo.setFormObject(foo);
        
        request.setAttribute("operation","add");
        
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        String modeStr = request.getParameter("opMode");
        if (modeStr != null && ( modeStr.equals("superproperty") || modeStr.equals("subproperty") || modeStr.equals("equivalentProperty") ) ) {
        	request.setAttribute("editAction","props2PropsOp");
        	request.setAttribute("formJsp","/templates/edit/specific/properties2properties_retry.jsp");
        	request.setAttribute("title", (modeStr.equals("superproperty") ? "Add Superproperty" : modeStr.equals("equivalentProperty") ? "Add Equivalent Property" : "Add Subproperty") );
        } 
        request.setAttribute("opMode", modeStr);
        
        request.setAttribute("_action",action);
        setRequestAttributes(request,epo);

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/formBasic.jsp");
        } catch (Exception e) {
            log.error(this.getClass().getName() + " could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }
    
}
