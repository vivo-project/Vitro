/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class Classes2ClassesRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(Classes2ClassesRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
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

        Classes2ClassesDao c2cDao = request.getFullWebappDaoFactory().getClasses2ClassesDao();
        VClassDao vcDao = request.getFullWebappDaoFactory().getVClassDao();
        epo.setDataAccessObject(c2cDao);
        Classes2Classes objectForEditing = new Classes2Classes();

        String superclassURIstr = request.getParameter("SuperclassURI");
        String subclassURIstr = request.getParameter("SubclassURI");
        if (superclassURIstr != null && superclassURIstr.length()>0)
            objectForEditing.setSuperclassURI(superclassURIstr);
        if (subclassURIstr != null && subclassURIstr.length()>0)
            objectForEditing.setSubclassURI(subclassURIstr);

        epo.setOriginalBean(objectForEditing);

        populateBeanFromParams(objectForEditing, request);

        HashMap hash = new HashMap();
        hash.put("SuperclassURI", FormUtils.makeOptionListFromBeans(vcDao.getAllVclasses(),"URI","LocalNameWithPrefix",objectForEditing.getSuperclassURI(),null));
        hash.put("SubclassURI", FormUtils.makeOptionListFromBeans(vcDao.getAllVclasses(),"URI","LocalNameWithPrefix",objectForEditing.getSubclassURI(),null));

        FormObject foo = new FormObject();
        foo.setOptionLists(hash);

        epo.setFormObject(foo);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        String modeStr = request.getParameter("opMode");
        if (modeStr != null && modeStr.equals("disjointWith")) {
        	request.setAttribute("editAction","classes2ClassesOp");
        	request.setAttribute("formJsp","/templates/edit/specific/disjointClasses_retry.jsp");
        	request.setAttribute("title","Disjointness Axiom Editing Form");
        } else if (modeStr != null && modeStr.equals("equivalentClass")){
        	request.setAttribute("editAction","classes2ClassesOp");
        	request.setAttribute("formJsp","/templates/edit/specific/equivalentClasses_retry.jsp");
        	request.setAttribute("title","Equivalent Class Editing Form");
        } else {
        	request.setAttribute("formJsp","/templates/edit/specific/class2classes_retry.jsp");
        	request.setAttribute("title","Super/Subclass Editing Form");
        }
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Classes2Classes");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("Classes2ClassesRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
