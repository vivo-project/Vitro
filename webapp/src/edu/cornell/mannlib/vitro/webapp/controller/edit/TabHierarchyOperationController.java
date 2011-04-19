/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseTabEditorPages;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;

@RequiresAuthorizationFor(UseTabEditorPages.class)
public class TabHierarchyOperationController extends BaseEditController {

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
	public void doPost(HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
    	String defaultLandingPage = getDefaultLandingPage(request);
        TabDao tDao = request.getFullWebappDaoFactory().getTabDao();

        if (request.getParameter("_cancel") == null) {
        
	        String action = request.getParameter("primaryAction");
	        if (action == null) {
	            if (request.getParameter("_insert") != null) {
	                action = "_insert";
	            }
	        }
	        
	        String[] childIdStr = request.getParameterValues("ChildId");
	        String[] parentIdStr = request.getParameterValues("ParentId");
	        
	        if (action.equals("_remove") && childIdStr != null && parentIdStr != null) {
	        	if (childIdStr.length>1 || (childIdStr.length==1 && parentIdStr.length==1)) {
	        		try {
	        			int parentId = Integer.decode(parentIdStr[0]);
	        			Tab parent = tDao.getTab(parentId);
		        		for (int childIndex=0; childIndex<childIdStr.length; childIndex++) {
		        			try {
		        				int childId = Integer.decode(childIdStr[childIndex]);
		        				Tab child = tDao.getTab(childId);
		        				tDao.removeParentTab(child, parent);
		        			} catch (NumberFormatException nfe) {}
		        		}
	        		} catch (NumberFormatException nfe) {}
	        	} else {
	        		try {
	        			int childId = Integer.decode(childIdStr[0]);
	        			Tab child = tDao.getTab(childId);
		        		for (int parentIndex=0; parentIndex<parentIdStr.length; parentIndex++) {
		        			try {
		        				int parentId = Integer.decode(parentIdStr[parentIndex]);
		        				Tab parent = tDao.getTab(parentId);
		        				tDao.removeParentTab(child, parent);
		        			} catch (NumberFormatException nfe) {}
		        		}
	        		} catch (NumberFormatException nfe) {}
	        	}
	        } else if (action.equals("_insert")) {
	        	try {
	        		int childId = Integer.decode(childIdStr[0]);
	                int parentId = Integer.decode(parentIdStr[0]);
	                Tab child = tDao.getTab(childId);
	                Tab parent = tDao.getTab(parentId);
	                tDao.addParentTab(child, parent);
	        	} catch (NumberFormatException nfe) {}
	        }
        
        }

        //if no page forwarder was set, just go back to referring page:
        //the referer stuff all will be changed so as not to rely on the HTTP header
        EditProcessObject epo = super.createEpo(request);
        String referer = epo.getReferer();
        if (referer == null) {
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                response.sendRedirect(referer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
