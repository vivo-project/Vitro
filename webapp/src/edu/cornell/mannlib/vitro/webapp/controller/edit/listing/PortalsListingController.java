/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;

public class PortalsListingController extends BaseEditController {

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        VitroRequest vrequest = new VitroRequest(request);
        Portal portal = vrequest.getPortal();

        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request, response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        PortalDao dao = vrequest.getFullWebappDaoFactory().getPortalDao();

        Collection portals = dao.getAllPortals();

        ArrayList results = new ArrayList();
        results.add("XX");
        results.add("ID number");
        results.add("Portal");
        results.add("");
        

        if (portals != null) {
            Iterator portalIt = portals.iterator();
            while (portalIt.hasNext()) {
                Portal p = (Portal) portalIt.next();
                results.add("XX");
                results.add(Integer.toString(p.getPortalId()));
                if (p.getAppName() != null)
                    try {
                        String pName = (p.getAppName()==null || p.getAppName().length()==0) ? Integer.toString(p.getPortalId()) : p.getAppName();
                        results.add("<a href=\"./editForm?id="+p.getPortalId()+"&amp;controller=Portal&amp;home="+portal.getPortalId()+"\">"+pName+"</a>");
                    } catch (Exception e) {
                        results.add(p.getAppName());
                    }
                else
                    results.add("");
                StringBuffer portalPath = (new StringBuffer()).append(request.getContextPath());
                if (p.getUrlprefix() != null) {
                	portalPath.append("/"+p.getUrlprefix());
                } else {
                	portalPath.append("/?home=").append(p.getPortalId());
                }
                results.add("<a href=\""+portalPath.toString()+"\">visit this portal</a>");
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(4));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Portals");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new portal");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Portal");
        request.setAttribute("home", portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
	
}
