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
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class RefactorRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RefactorRetryController.class.getName());

	private void doRenameResource(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		epo.setAttribute("modeStr","renameResource");
		epo.setAttribute("_action","insert");
		epo.setAttribute("oldURI",request.getParameter("oldURI"));
		request.setAttribute("editAction","refactorOp");
		request.setAttribute("title","Rename Resource");
		request.setAttribute("formJsp", "/templates/edit/specific/renameResource_retry.jsp");
		request.setAttribute("_cancelButtonDisabled","disabled");
		request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
	}
	
	private void doMovePropertyStatements(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		epo.setAttribute("modeStr","movePropertyStatements");
		String propertyURI = request.getParameter("propertyURI");
		epo.setAttribute("propertyURI",propertyURI);
		epo.setAttribute("propertyType",request.getParameter("propertyType"));
		FormObject foo = new FormObject();
		epo.setFormObject(foo);
		HashMap<String,List<Option>> optMap = new HashMap<String,List<Option>>();
		foo.setOptionLists(optMap);
		List<Option> subjectClassOpts = FormUtils.makeOptionListFromBeans(request.getFullWebappDaoFactory().getVClassDao().getAllVclasses(),"URI","Name", null, null);
		subjectClassOpts.add(0,new Option("","? wildcard",true));
		optMap.put("SubjectClassURI", subjectClassOpts);
		optMap.put("ObjectClassURI", subjectClassOpts);
		
		List newPropertyOpts;
		if (epo.getAttribute("propertyType").equals("ObjectProperty"))  {
			List<ObjectProperty> opList = request.getFullWebappDaoFactory().getObjectPropertyDao().getAllObjectProperties();
			Collections.sort(opList);
			newPropertyOpts = FormUtils.makeOptionListFromBeans(opList,"URI","LocalNameWithPrefix", null, null);
		} else {
			List<DataProperty> dpList = request.getFullWebappDaoFactory().getDataPropertyDao().getAllDataProperties();
			Collections.sort(dpList);
			newPropertyOpts = FormUtils.makeOptionListFromBeans(dpList,"URI","Name", null, null);
		}
		HashMap<String,Option> hashMap = new HashMap<String,Option>();
        newPropertyOpts = getSortedList(hashMap,newPropertyOpts);
		newPropertyOpts.add(new Option("","(move to trash)"));
		optMap.put("NewPropertyURI", newPropertyOpts);				
		
		request.setAttribute("editAction","refactorOp");
		request.setAttribute("title", "Move Property Statements");
		request.setAttribute("formJsp", "/templates/edit/specific/movePropertyStatements_retry.jsp");
		request.setAttribute("_cancelButtonDisabled","disabled");
		request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
	}
	
	public void doMoveInstances(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		epo.setAttribute("modeStr","moveInstances");
		String propertyURI = request.getParameter("VClassURI");
		epo.setAttribute("VClassURI",propertyURI);
		FormObject foo = new FormObject();
		epo.setFormObject(foo);
		HashMap<String,List<Option>> optMap = new HashMap<String,List<Option>>();
		foo.setOptionLists(optMap);
		List<Option> newClassURIopts = FormUtils.makeOptionListFromBeans(request.getFullWebappDaoFactory().getVClassDao().getAllVclasses(),"URI","LocalNameWithPrefix", null, null);
		newClassURIopts.add(new Option ("","move to trash"));
		optMap.put("NewVClassURI", newClassURIopts);
		request.setAttribute("editAction","refactorOp");
		request.setAttribute("title", "Move Class Instances");
		request.setAttribute("formJsp", "/templates/edit/specific/moveInstances_retry.jsp");
		request.setAttribute("_cancelButtonDisabled","disabled");
		request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }
	
	    //create an EditProcessObject for this and put it in the session
	    EditProcessObject epo = super.createEpo(request);
	    
	    VitroRequest vreq = new VitroRequest(request);
	    
	    String modeStr = request.getParameter("mode");
	    
	    if (modeStr != null) {
	    	if (modeStr.equals("renameResource")) {
	    		doRenameResource(vreq, response, epo);
	    	} else if (modeStr.equals("movePropertyStatements")) {
	    		doMovePropertyStatements(vreq, response, epo);
	    	} else if (modeStr.equals("moveInstances")) {
	    		doMoveInstances(vreq, response, epo);
	    	}
	    }

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
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
