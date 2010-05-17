/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.menu;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;

/** A main menu constructed from persisted tab data
 * 
 * @author rjy7
 *
 */
public class TabMenu extends MainMenu {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TabMenu.class.getName());
       
    public TabMenu(VitroRequest vreq, int portalId) {
        super(vreq, portalId);
        
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
            addItem(tab.getTitle(), "/index.jsp?primary=" + tab.getTabId());
            // RY Also need to loop through nested tab levels, but not doing that now.
        }
        
        // Hard-coded tabs. It's not really a good idea to have these here, since any menu item that doesn't
        // come from the db should be accessible to the template to change the text. But we need them here
        // to apply the "active" mechanism.
        addItem("Index", "/browse");     
    }

}
