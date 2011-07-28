/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;

public class Properties2PropertiesRetryController extends BaseEditController {

	private static final Log log = LogFactory.getLog(Properties2PropertiesRetryController.class.getName());

    public void doGet (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
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

        ObjectPropertyDao opDao = request.getFullWebappDaoFactory().getObjectPropertyDao();
        DataPropertyDao dpDao = request.getFullWebappDaoFactory().getDataPropertyDao();
        epo.setDataAccessObject(opDao);
        
        List propList = ("data".equals(request.getParameter("propertyType"))) 
    	? dpDao.getAllDataProperties()
    	: opDao.getAllObjectProperties();
        
    	Collections.sort(propList);
    	
    	 String superpropertyURIstr = request.getParameter("SuperpropertyURI");
         String subpropertyURIstr = request.getParameter("SubpropertyURI");
       
        HashMap<String,Option> hashMap = new HashMap<String,Option>();
        List<Option> optionList = FormUtils.makeOptionListFromBeans(propList,"URI","LocalNameWithPrefix",superpropertyURIstr,null);
        List<Option> superPropertyOptions = getSortedList(hashMap,optionList);
        optionList = FormUtils.makeOptionListFromBeans(propList,"URI","LocalNameWithPrefix",subpropertyURIstr,null);
        List<Option> subPropertyOptions = getSortedList(hashMap, optionList);
        
        HashMap hash = new HashMap();
    	hash.put("SuperpropertyURI", superPropertyOptions);
        hash.put("SubpropertyURI", subPropertyOptions);
        
        FormObject foo = new FormObject();
        foo.setOptionLists(hash);

        epo.setFormObject(foo);
        
        request.setAttribute("operation","add");
        
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
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
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName() + " could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }
    
    public List<Option> getSortedList(HashMap<String,Option> hashMap, List<Option> optionList){
    	
    	 class ListComparator implements Comparator<String>{
 			@Override
 			public int compare(String str1, String str2) {
 				// TODO Auto-generated method stub
 				Collator collator = Collator.getInstance();
 				return collator.compare(str1, str2);
 			}
         	
         }

    	List<String> bodyVal = new ArrayList<String>();
    	List<Option> options = new ArrayList<Option>();
    	Iterator<Option> itr = optionList.iterator();
    	 while(itr.hasNext()){
         	Option option = itr.next();
         	hashMap.put(option.getBody(),option);
            bodyVal.add(option.getBody());
         }
         
                 
        Collections.sort(bodyVal, new ListComparator());
        ListIterator<String> itrStr = bodyVal.listIterator();
        while(itrStr.hasNext()){
        	options.add(hashMap.get(itrStr.next()));
        }
        return options;
    }
    
}
