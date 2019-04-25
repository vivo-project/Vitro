/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.impl.RequiredFieldValidator;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;


public class ClassgroupRetryController extends BaseEditController {

	private static final Log log = LogFactory.getLog(ClassgroupRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
		if (!isAuthorizedToDisplayPage(req, response,
				SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION)) {
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

        VClassGroupDao cgDao = ModelAccess.on(
                getServletContext()).getWebappDaoFactory().getVClassGroupDao();

        epo.setDataAccessObject(cgDao);

        VClassGroup vclassGroupForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("uri") != null) {
                try {
                    vclassGroupForEditing = (VClassGroup)cgDao.getGroupByURI(request.getParameter("uri"));
                    action = "update";
                    epo.setAction("update");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
                if (vclassGroupForEditing == null) {
                    //UTF-8 expected due to URIEncoding on Connector in server.xml
                    String uriToFind = request.getParameter("uri");
                    vclassGroupForEditing = (VClassGroup)cgDao.getGroupByURI(uriToFind);
                }
            } else {
                vclassGroupForEditing = new VClassGroup();
            }
            epo.setOriginalBean(vclassGroupForEditing);
        } else {
            vclassGroupForEditing = (VClassGroup) epo.getNewBean();
        }

        //validators
        List<Validator> validatorList = new ArrayList<Validator>();
        validatorList.add(new RequiredFieldValidator());
        epo.getValidatorMap().put("PublicName", validatorList);

        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new VclassGroupInsertPageForwarder());
        //make a postdelete pageforwarder that will send us to the list of classes
        epo.setPostDeletePageForwarder(new UrlForwarder("listGroups"));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(cgDao.getClass().getDeclaredMethod("getGroupByURI",args));
        } catch (NoSuchMethodException e) {
            log.error(this.getClass().getName()+" could not find the getGroupByURI method");
        }

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());
        epo.setFormObject(foo);

        FormUtils.populateFormFromBean(vclassGroupForEditing,action,foo,epo.getBadValueMap());

        request.setAttribute("formJsp","/templates/edit/specific/classgroup_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Classgroup Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","VClassGroup");
        setRequestAttributes(request,epo);

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/formBasic.jsp");
        } catch (Exception e) {
            log.error("VclassGroupRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class VclassGroupInsertPageForwarder implements PageForwarder {

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newVclassGroupUrl = "listGroups";
            try {
                response.sendRedirect(newVclassGroupUrl);
            } catch (IOException ioe) {
                log.error("VclassGroupInsertPageForwarder could not send redirect.");
            }
        }
    }

}
