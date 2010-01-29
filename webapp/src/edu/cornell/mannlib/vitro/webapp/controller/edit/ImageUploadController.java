package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;

public class ImageUploadController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(ImageUploadController.class.getName());

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		if (!checkLoginStatus(request,response,(String)request.getAttribute("fetchURI")))
			return;
		
		try {
			super.doGet(request,response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String individualURIStr=
		    (individualURIStr=(String)request.getAttribute("entityUri"))==null ? 
		           ((individualURIStr=request.getParameter("entityUri"))==null ? null:individualURIStr) : individualURIStr;

		if (individualURIStr != null)
			request.setAttribute("individual", getWebappDaoFactory().getIndividualDao().getIndividualByURI(individualURIStr));
		
		EditProcessObject epo = super.createEpo(request);
		FormObject foo = new FormObject();
		HashMap optionMap = new HashMap();
		foo.setOptionLists(optionMap);
		epo.setFormObject(foo);
						
		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
		request.setAttribute("bodyJsp","/templates/edit/specific/uploadimages_body.jsp");
		request.setAttribute("scripts","/templates/edit/specific/uploadimages_head.jsp");
        request.setAttribute("title","Upload Image");
        request.setAttribute("bodyAttr","onLoad=\"initDHTMLAPI();initGroupDisplay('thumbnailExtra','block');\"");
		request.setAttribute("epoKey",epo.getKey());
		try {
			rd.forward(request, response);
		} catch (Exception e) {
			log.error("ImageUploadController could not forward to view.");
			log.error(e.getMessage());
			log.error(e.getStackTrace());
		}    
	}		
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request,response);
	}
	
}
