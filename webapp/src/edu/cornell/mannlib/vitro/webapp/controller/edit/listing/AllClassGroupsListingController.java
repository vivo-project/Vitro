package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;

public class AllClassGroupsListingController extends BaseEditController {
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request, response);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Portal portal = (new VitroRequest(request)).getPortal();

        //need to figure out how to structure the results object to put the classes underneath

        VClassGroupDao dao = getWebappDaoFactory().getVClassGroupDao();

        List groups = dao.getPublicGroupsWithVClasses(); // uses an unfiltered dao so will see all classes

        ArrayList results = new ArrayList();
        results.add("XX");
        results.add("Group");
        results.add("display rank");
        results.add("last modified");
        results.add("XX");

        if (groups != null) {
            Iterator groupsIt = groups.iterator();
            while (groupsIt.hasNext()) {
                VClassGroup vcg = (VClassGroup) groupsIt.next();
                results.add("XX");
                if (vcg.getPublicName() != null) {
                    try {
                        results.add("<a href=\"./editForm?uri="+URLEncoder.encode(vcg.getURI(),"UTF-8")+"&amp;home="+portal.getPortalId()+"&amp;controller=Classgroup\">"+vcg.getPublicName()+"</a>");
                    } catch (Exception e) {
                        results.add(vcg.getPublicName());
                    }
                } else {
                    results.add("");
                }
                results.add(Integer.valueOf(vcg.getDisplayRank()).toString());
                results.add("???"); // VClassGroup doesn't yet supprt getModTime()
                results.add("XX");
                List classList = vcg.getVitroClassList();
                if (classList != null && classList.size()>0) {
                    results.add("+");
                    results.add("XX");
                    results.add("Class");
                    results.add("example");
                    results.add("description");
                    results.add("@@entities");
                    Iterator classIt = classList.iterator();
                    while (classIt.hasNext()) {
                        VClass vcw = (VClass) classIt.next();
                        results.add("XX");
                        if (vcw.getName() != null && vcw.getURI() != null) {
                            try {
                                results.add("<a href=\"vclassEdit?uri="+URLEncoder.encode(vcw.getURI(),"UTF-8")+"\">"+vcw.getName()+"</a>");
                            } catch (Exception e) {
                                results.add(vcw.getName());
                            }
                        } else {
                            results.add("");
                        }
                        String exampleStr = (vcw.getExample() == null) ? "" : vcw.getName();
                        results.add(exampleStr);
                        String descriptionStr = (vcw.getDescription() == null) ? "" : vcw.getDescription();
                        results.add(descriptionStr);
                        if (classIt.hasNext())
                            results.add("@@entities");
                    }
                }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(5));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Class Groups");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new class group");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Classgroup");
        request.setAttribute("home", portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
