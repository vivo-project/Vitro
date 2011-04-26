/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.Checkbox;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageTabs;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.TabIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class TabEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(TabEditController.class.getName());
	private static final int NUM_COLS = 11;

    @Override
	public void doPost (HttpServletRequest request, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(request, response, new Actions(new ManageTabs()))) {
    		return;
    	}

        VitroRequest vreq = new VitroRequest(request);
        Portal portal = vreq.getPortal();

        // we need to extract the keyword id from a Fetch parameter
        String tabIdStr = request.getParameter("id");
        int tabId = -1;
        try {
            tabId = Integer.decode(tabIdStr);
        } catch (NumberFormatException e) {
            try {
                throw new ServletException(this.getClass().getName()+" expects tab id in 'id' request parameter");
            } catch (ServletException f) {
                f.printStackTrace();
            }
        }

        TabDao tDao = vreq.getFullWebappDaoFactory().getTabDao();
        VClassDao vcDao = vreq.getFullWebappDaoFactory().getVClassDao();
        VClassGroupDao vcgDao = vreq.getFullWebappDaoFactory().getVClassGroupDao();
        IndividualDao eDao = vreq.getFullWebappDaoFactory().getIndividualDao();
        Tab t = vreq.getFullWebappDaoFactory().getTabDao().getTab(tabId);

        request.setAttribute("tabId",tabId);

        ArrayList<String> results = new ArrayList<String>();
        results.add("title");
        results.add("tab id");
        results.add("description");
        results.add("tab type");
        results.add("entity link method");
        results.add("display rank");
        results.add("day limit");
        results.add("sort field");
        results.add("sort direction");
        results.add("flag2mode");
        results.add("flag2set");
        
        results.add(t.getTitle()!=null ? t.getTitle() : "no title specified");
        results.add(String.valueOf(t.getTabId()));
        results.add(t.getDescription()!=null ? t.getDescription() : "no description specified");
        /* from TabRetryController.java
        static final int[] tabtypeIds = {0,18,20,22,24,26,28};
        static final String[] tabtypeNames = {"unspecified", "subcollection category",
                            "subcollection", "collection", "secondary tab",
                            "primary tab content", "primary tab"};
        */
        HashMap<Integer, String> tabTypes = new HashMap<Integer, String>();
        tabTypes.put(18,"subcollection category");
        tabTypes.put(20,"subcollection");
        tabTypes.put(22,"collection");
        tabTypes.put(24,"secondary tab");
        tabTypes.put(26,"primary tab content");
        tabTypes.put(28,"primary tab");

        String tabtype = tabTypes.get(t.getTabtypeId());
        results.add(tabtype!=null ? tabtype : "unspecified");        
        results.add(t.getEntityLinkMethod()!=null ? t.getEntityLinkMethod() : "unspecified");
        results.add(String.valueOf(t.getDisplayRank()));
        results.add(String.valueOf(t.getDayLimit()));
        results.add(t.getEntitySortField()!=null ? t.getEntitySortField() : "unspecified");
        results.add(t.getEntitySortDirection()!=null ? t.getEntitySortDirection() : "unspecified");
        results.add(t.getFlag2Mode()!=null ? t.getFlag2Mode() : "unspecified");
        results.add(t.getFlag2Set()!=null ? t.getFlag2Set() : "unspecified");   
        
        request.setAttribute("results", results);
        request.setAttribute("columncount", new Integer(NUM_COLS));
        request.setAttribute("suppressquery", "true");

        // make form stuff

        EditProcessObject epo = super.createEpo(request);
        FormObject foo = new FormObject();

        // parent tabs
        EditProcessObject tabHierarchyEpo = super.createEpo(request);
        tabHierarchyEpo.setReferer(request.getRequestURI()+"?"+request.getQueryString());
        request.setAttribute("tabHierarchyEpoKey", tabHierarchyEpo.getKey());
        List<Checkbox> parentList = new LinkedList<Checkbox>();
        Iterator<Tab> parentIt = tDao.getParentTabs(t).iterator();
        while (parentIt.hasNext()) {
            Tab parent = parentIt.next();
            Checkbox cb = new Checkbox();
            //cb.setBody(parent.getTitle());
            cb.setBody("<a href=\"tabEdit?home="+portal.getPortalId()+"&amp;id="+parent.getTabId()+"\">"+parent.getTitle()+"</a>");
            cb.setValue(Integer.toString(parent.getTabId()));
            cb.setChecked(false);
            parentList.add(cb);
        }
        foo.getCheckboxLists().put("parentTabs",parentList);

        // child tabs
        List<Checkbox> childList = new LinkedList<Checkbox>();
        Iterator<Tab> childIt = tDao.getChildTabs(t).iterator();
        while (childIt.hasNext()) {
            Tab child = childIt.next();
            Checkbox cb = new Checkbox();
            cb.setValue(Integer.toString(child.getTabId()));
            //cb.setBody(child.getTitle());
            cb.setBody("<a href=\"tabEdit?home="+portal.getPortalId()+"&amp;id="+child.getTabId()+"\">"+child.getTitle()+"</a>");
            cb.setChecked(false);
            childList.add(cb);
        }
        foo.getCheckboxLists().put("childTabs",childList);

        HashMap OptionMap = new HashMap();
        List<VClassGroup> classGroups = vcgDao.getPublicGroupsWithVClasses(true,false,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals
        ListOrderedMap optGroupMap = new ListOrderedMap();
        for (VClassGroup group : classGroups) {
            List<VClass> classes = group.getVitroClassList();
            optGroupMap.put(group.getPublicName(),FormUtils.makeOptionListFromBeans(classes,"URI","Name",null,null,false));
        }
        OptionMap.put("VClassURI", optGroupMap);
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        List<VClass> types = new LinkedList<VClass>();
        List<String> typeURIs = tDao.getTabAutoLinkedVClassURIs(tabId);
        Iterator<String> typeURIt = typeURIs.iterator();
        while (typeURIt.hasNext()) {
            String typeURI = typeURIt.next();
            VClass type = vcDao.getVClassByURI(typeURI);
            if (type != null) {
                types.add(type);
            }
        }
        request.setAttribute("affilTypes",types);

        TabIndividualRelationDao tirDao = vreq.getFullWebappDaoFactory().getTabs2EntsDao();
        List<TabIndividualRelation> tirs = tirDao.getTabIndividualRelationsByTabURI(((WebappDaoFactory)request.getSession().getServletContext().getAttribute("webappDaoFactory")).getDefaultNamespace()+"tab"+tabId);
        List checkboxList = new ArrayList();
        Iterator<TabIndividualRelation> tirsIt = tirs.iterator();
        while (tirsIt.hasNext()) {
            TabIndividualRelation tir = tirsIt.next();
            Individual ind = eDao.getIndividualByURI(tir.getEntURI());
                if (ind != null) {
                    Checkbox cb = new Checkbox();
                    cb.setBody(ind.getName());
                    cb.setValue(tir.getURI());
                    cb.setChecked(false);
                    checkboxList.add(cb);
                }
        }
        foo.getCheckboxLists().put("affilEnts",checkboxList);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("epo",epo);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("tab", t);
        request.setAttribute("bodyJsp","/templates/edit/specific/tabs_edit.jsp");
        request.setAttribute("portalBean",portal);
        request.setAttribute("title","Tab Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("TabEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }

}
