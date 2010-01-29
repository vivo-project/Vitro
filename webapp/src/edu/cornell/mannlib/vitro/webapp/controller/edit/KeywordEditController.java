package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class KeywordEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(KeywordEditController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {

        if (!checkLoginStatus(request,response,(String)request.getAttribute("fetchURI")))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("KeywordEditController caught exception calling doGet()");
        }
        VitroRequest vreq = new VitroRequest(request);
        // we need to extract the keyword id from a Fetch parameter
        String linkwhereId = request.getParameter("linkwhere");
        String theKeywordId = linkwhereId.substring(15,linkwhereId.length()-1);
        int kwId = Integer.decode(theKeywordId);

        Keyword k = vreq.getWebappDaoFactory().getKeywordDao().getKeywordById(kwId);

        EditProcessObject epo = super.createEpo(request);
        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        Portal portal = vreq.getPortal();
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("keyword", k);
        request.setAttribute("bodyJsp","/templates/edit/specific/keyterms_edit.jsp");
        request.setAttribute("portalBean",portal);
        request.setAttribute("title","Keyword Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("KeywordEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }

}
