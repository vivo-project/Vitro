package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;


public class ClassgroupRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(ClassgroupRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("ClassgroupRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        VClassGroupDao cgDao = getWebappDaoFactory().getVClassGroupDao();

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
                    try {
                        String uriToFind = new String(request.getParameter("uri").getBytes("ISO-8859-1"),"UTF-8");
                        vclassGroupForEditing = (VClassGroup)cgDao.getGroupByURI(uriToFind);
                    } catch (java.io.UnsupportedEncodingException uee) {
                        // forget it
                    }
                }
            } else {
                vclassGroupForEditing = new VClassGroup();
            }
            epo.setOriginalBean(vclassGroupForEditing);
        } else {
            vclassGroupForEditing = (VClassGroup) epo.getNewBean();
        }

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }
        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new VclassGroupInsertPageForwarder(currPortalId));
        //make a postdelete pageforwarder that will send us to the list of classes
        epo.setPostDeletePageForwarder(new UrlForwarder("listGroups?home="+currPortalId));

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

        String html = FormUtils.htmlFormFromBean(vclassGroupForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/classgroup_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Classgroup Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","VClassGroup");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
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
        private int portalId = 1;

        public VclassGroupInsertPageForwarder(int currPortalId) {
            portalId = currPortalId;
        }

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newVclassGroupUrl = "listGroups?home="+portalId;
            try {
                response.sendRedirect(newVclassGroupUrl);
            } catch (IOException ioe) {
                log.error("VclassGroupInsertPageForwarder could not send redirect.");
            }
        }
    }

}
