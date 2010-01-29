package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;

public class AllTabsForPortalListingController extends BaseEditController {
    
    private static final int NUM_COLS = 11;

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

        HashMap types = new HashMap();
        types.put(18,"subcollection category");
        types.put(20,"subcollection");
        types.put(22,"collection");
        types.put(24,"secondary tab");
        types.put(26,"primary tab content");
        types.put(28,"primary tab");

        TabDao dao = getWebappDaoFactory().getTabDao();
        List tabs = dao.getTabsForPortal(portal.getPortalId());
        Collections.sort(tabs, new TabComparator());
        
        ArrayList results = new ArrayList();
        results.add("XX");
        results.add("title");
        results.add("tab id");
        results.add("tab type");
        //results.add("description");
        results.add("display rank");
        results.add("entity links");
        results.add("day limit");
        results.add("sort field");
        results.add("sort dir");
        results.add("flag2mode");
        results.add("flag2set");


        if (tabs != null) {
            Iterator tabIt = tabs.iterator();
            while (tabIt.hasNext()) {
                Tab tab = (Tab) tabIt.next();
                results.add("XX");
                if (tab.getTitle() != null)
                    results.add("<a href=\"./tabEdit?id="+tab.getTabId()+"&amp;home="+portal.getPortalId()+"\">"+tab.getTitle()+"</a>");
                else
                    results.add("");
                results.add(String.valueOf(tab.getTabId()));
                String tabtype = (String) types.get(tab.getTabtypeId());
                results.add(tabtype!=null ? tabtype : "-");
                //results.add(tab.getDescription()!=null ? tab.getDescription() : "-");
                results.add(Integer.valueOf(tab.getDisplayRank()).toString());
                results.add(tab.getEntityLinkMethod()!=null ? tab.getEntityLinkMethod() : "-");
                results.add(Integer.valueOf(tab.getDayLimit()).toString());
                results.add(tab.getEntitySortField()!=null ? tab.getEntitySortField() : "-");
                results.add(tab.getEntitySortDirection()!=null ? tab.getEntitySortDirection() : "-");
                results.add(tab.getFlag2Mode()!=null ? tab.getFlag2Mode() : "-");
                results.add(tab.getFlag2Set()!=null ? tab.getFlag2Set() : "-");   
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(NUM_COLS));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Tabs");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new tab");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Tab");
        request.setAttribute("home",portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
    private class TabComparator implements Comparator {
        public int compare (Object o1, Object o2) {
        	Tab tab1 = (Tab)o1;
        	Tab tab2 = (Tab)o2;
        	if(tab1 == null && tab2 == null) return 0;
        	int diff = tab1.getTabId() - tab2.getTabId(); // tab1.getDisplayRank() - tab2.getDisplayRank()
        	if(diff == 0)
        		return tab1.getTitle().compareToIgnoreCase(tab2.getTitle());
        	return diff;
        }
    }
}
