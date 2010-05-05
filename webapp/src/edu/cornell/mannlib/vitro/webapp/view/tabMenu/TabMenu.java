/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.tabMenu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;

public class TabMenu extends ArrayList {
    
    private static final Log log = LogFactory.getLog(TabMenu.class.getName());
    
    private VitroRequest vreq;
    private int portalId;
    private String contextPath;
    
    public TabMenu(VitroRequest vreq, int portalId) {
        this.vreq = vreq;
        this.portalId = portalId;  
        this.contextPath = vreq.getContextPath();
        
        //Tabs stored in database
        List<Tab> primaryTabs = vreq.getWebappDaoFactory().getTabDao().getPrimaryTabs(portalId);        
        int tabId = TabWebUtil.getTabIdFromRequest(vreq); 
        int rootId = TabWebUtil.getRootTabId(vreq); 
        List tabLevels = vreq.getWebappDaoFactory().getTabDao().getTabHierarcy(tabId,rootId);
        vreq.setAttribute("tabLevels", tabLevels); 
        Iterator<Tab> primaryTabIterator = primaryTabs.iterator();
        Iterator tabLevelIterator = tabLevels.iterator();
        Tab tab;
        while (primaryTabIterator.hasNext()) {
            tab = (Tab) primaryTabIterator.next();
            this.add(new TabMenuItem(tab.getTitle(), "index.jsp?primary=" + tab.getTabId(), vreq));
            // RY Also need to loop through nested tab levels, but not doing that now.
        }
        
        // Hard-coded tabs
        this.add(new TabMenuItem("Index", "browsecontroller", vreq));
        this.add(new TabMenuItem("Index - FM", "browsecontroller-fm", vreq));      
    }

}
