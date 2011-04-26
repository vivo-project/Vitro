/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageTabs;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;

public class Tabs2TabsRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(Tabs2TabsRetryController.class.getName());

    @Override
	public void doPost (HttpServletRequest req, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(req, response, new Actions(new ManageTabs()))) {
    		return;
    	}

    	VitroRequest request = new VitroRequest(req);
        Portal portal = request.getPortal();

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        FormObject foo = new FormObject();
        epo.setFormObject(foo);

        String action = "insert";

        TabDao tDao = request.getFullWebappDaoFactory().getTabDao();

        Tab child = null;
        Tab parent = null;

        if (request.getParameter("ChildId") != null) {
            try {
                int childId = Integer.decode(request.getParameter("ChildId"));
                child = tDao.getTab(childId);
            } catch (NumberFormatException e) {}
        }

        if (request.getParameter("ParentId") != null) {
            try {
                int parentId = Integer.decode(request.getParameter("ParentId"));
                parent = tDao.getTab(parentId);
            } catch (NumberFormatException e) {}
        }

        HashMap hash = new HashMap();
        foo.setOptionLists(hash);
        if (parent != null ) {
            hash.put("ChildId", FormUtils.makeOptionListFromBeans(tDao.getTabsForPortalByTabtypes(portal.getPortalId(),false, parent.getTabtypeId()),"TabId","Title",null,null));
            List parentList = new LinkedList();
            parentList.add(new Option(Integer.toString(parent.getTabId()),parent.getTitle(),true));
            hash.put("ParentId", parentList);
        } else if (child != null){
            hash.put("ParentId", FormUtils.makeOptionListFromBeans(tDao.getTabsForPortalByTabtypes(portal.getPortalId(),true,child.getTabtypeId()),"TabId","Title",null,null));
            List childList = new LinkedList();
            childList.add(new Option(Integer.toString(child.getTabId()),child.getTitle(),true));
            hash.put("ChildId", childList);
        }

        epo.setFormObject(foo);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);

        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("epo",epo);
        request.setAttribute("bodyJsp","/templates/edit/specific/tabs2tabs_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Super/Subtab Editing Form");
        request.setAttribute("_action",action);
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("Tabs2TabsRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
