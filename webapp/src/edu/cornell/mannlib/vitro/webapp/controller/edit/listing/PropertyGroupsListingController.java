/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;

public class PropertyGroupsListingController extends BaseEditController {

   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response) {
     	if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
		    return;
	    }
	
        VitroRequest vrequest = new VitroRequest(request);
        Portal portal = vrequest.getPortal();

        //need to figure out how to structure the results object to put the classes underneath

        PropertyGroupDao dao = vrequest.getFullWebappDaoFactory().getPropertyGroupDao();

        List<PropertyGroup> groups = dao.getPublicGroups(true);

        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");
        results.add("Group");
        results.add("Public description");
        results.add("display rank");
        results.add("XX");

        if (groups != null) {
            Collections.sort(groups, new PropertyGroupDisplayComparator());
            Iterator<PropertyGroup> groupsIt = groups.iterator();
            while (groupsIt.hasNext()) {
                PropertyGroup pg = groupsIt.next();
                results.add("XX");
                if (pg.getName() != null) {
                    try {
                        results.add("<a href=\"./editForm?uri="+URLEncoder.encode(pg.getURI(),"UTF-8")+"&amp;home="+portal.getPortalId()+"&amp;controller=PropertyGroup\">"+pg.getName()+"</a>");
                    } catch (Exception e) {
                        results.add(pg.getName());
                    }
                } else {
                    results.add("");
                }
                results.add(pg.getPublicDescription()==null ? "unspecified" : pg.getPublicDescription());
                results.add(Integer.valueOf(pg.getDisplayRank()).toString());
                results.add("XX");
                List<Property> classList = pg.getPropertyList();
                if (classList != null && classList.size()>0) {
                    results.add("+");
                    results.add("XX");
                    results.add("Property");
                    results.add("example");
                    results.add("description");
                    results.add("@@entities");
                    Iterator<Property> propIt = classList.iterator();
                    while (propIt.hasNext()) {
                    	results.add("XX");
                        Property p = propIt.next();
                        if (p instanceof ObjectProperty) {
                        	ObjectProperty op = (ObjectProperty) p;
	                        if (op.getLocalNameWithPrefix() != null && op.getURI() != null) {
	                            try {
	                                results.add("<a href=\"propertyEdit?uri="+URLEncoder.encode(op.getURI(),"UTF-8")+"\">"+op.getLocalNameWithPrefix()+"</a>");
	                            } catch (Exception e) {
	                                results.add(op.getLocalNameWithPrefix());
	                            }
	                        } else {
	                            results.add("");
	                        }
	                        String exampleStr = (op.getExample() == null) ? "" : op.getExample();
	                        results.add(exampleStr);
	                        String descriptionStr = (op.getDescription() == null) ? "" : op.getDescription();
	                        results.add(descriptionStr);
                        } else {
                          	DataProperty dp = (DataProperty) p;
	                        if (dp.getName() != null && dp.getURI() != null) {
	                            try {
	                                results.add("<a href=\"datapropEdit?uri="+URLEncoder.encode(dp.getURI(),"UTF-8")+"\">"+dp.getName()+"</a>");
	                            } catch (Exception e) {
	                                results.add(dp.getName());
	                            }
	                        } else {
	                            results.add("");
	                        }
	                        String exampleStr = (dp.getExample() == null) ? "" : dp.getExample();
	                        results.add(exampleStr);
	                        String descriptionStr = (dp.getDescription() == null) ? "" : dp.getDescription();
	                        results.add(descriptionStr);
                        }
                        if (propIt.hasNext())
                            results.add("@@entities");
                    }
                }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(5));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Property Groups");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new property group");
        request.setAttribute("horizontalJspAddButtonControllerParam", "PropertyGroup");
        request.setAttribute("home", portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
   
    private class PropertyGroupDisplayComparator implements Comparator<PropertyGroup> {
        @Override
		public int compare (PropertyGroup o1, PropertyGroup o2) {
            try {
                int diff = o1.getDisplayRank() - o2.getDisplayRank();
                if (diff==0) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
                return diff;
            } catch (Exception e) {
                return 1;
            }
        }
    }

}
