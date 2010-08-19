/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;

/**
 * Intended to generate the hierarchal tab menu.
 *
 */
public class TabMenu {
    private static final Log log = LogFactory.getLog(TabMenu.class.getName());

    //we got lists, we got tables, take your pick.
    static String itemOpen ="<li>", itemClose="</li>\n";
    static String itemOpenActive ="<li class=\"activePrimaryTab\">";
    static String secondaryItemOpen ="<td>", secondaryItemClose="</td>\n";
    static String secondaryItemOpenActive ="<td class=\"activeSecondaryTab\">";
    static String primaryOpen="<ul id=\"primary\">\n", primaryClose="</ul>\n";
//    static String secondaryOpen="<ul id=\"secondary\">\n", secondaryClose="</ul>\n";
//    static String itemOpen ="<td>", itemClose="</td>\n";
//    static String primaryOpen="<table id='primary'><tr>\n", primaryClose="</tr></table>";
    static String secondaryOpen="<table id=\"secondary\"><tr>\n", secondaryClose="</tr></table>";
    static String primaryActiveClass = "activePrimaryTab";
    static String secondaryActiveClass ="activeSecondaryTab";

    /**
     * The is the method you should call to get an html element for the
     * tab menu of a page.  There must be a better way to do this, call
     * out to a jsp? some kind of template?  For now the goal is to consolidate
     * the point of creation of the menu so that all code uses the same
     * method and we can improve it later.
     *
     * @param req - needs to have the Attribute "portalBean"
     * @return
     */
    public static String getPrimaryTabMenu(VitroRequest vreq){
        String ret = primaryOpen;
        String label = null;
        try {
            int portalId=Portal.DEFAULT_PORTAL_ID;
            Portal portal=null;
            Object obj = vreq.getAttribute("portalBean");
            if( obj == null ) {
                log.error("getPrimaryTabMenu() must have attribute 'portalBean' in vitro request");
            } else {
                portal = (Portal)obj;
                portalId=portal.getPortalId();
            }
            List primaryTabs = vreq.getWebappDaoFactory().getTabDao().getPrimaryTabs(portalId);
            if (primaryTabs != null) {
                Iterator itPrime = primaryTabs.iterator();
                while( itPrime.hasNext() ){
                    Tab tab = (Tab) itPrime.next();
                    boolean active = isTabActive(vreq, tab.getTabId());
                    boolean isRootTab = false;
                    //are we at the root tab for the portal?
                    if (portal!=null){
                        isRootTab = (portal.getRootTabId()==tab.getTabId());
                    }else{
                        isRootTab = (tab.getTabId() == Portal.DEFAULT_ROOT_TAB_ID);
                    }

                    label = isRootTab?"Home":null;
                    ret +=  "\t";
                    if (active) {
                        ret += itemOpenActive;
                        ret += TabWebUtil.tab2TabAnchorElement(tab, vreq,
                               TabWebUtil.PRIMARY, primaryActiveClass, label, portalId)
                               + "\t" + itemClose;
                    }
                    else {
                        ret += itemOpen;
                        ret += TabWebUtil.tab2TabAnchorElement(tab, vreq,
                           TabWebUtil.PRIMARY, null, label, portalId)
                           + "\t" + itemClose;
                    }
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return ret + primaryClose;
    }

    /**
     * Gets secondary tabs for tab menu.
     * @param req
     * @return
     */
    public static String getSecondaryTabMenu(VitroRequest vreq){
        try {
            List pathToRoot = getTabsAboveCurrent(vreq);
            int primaryTabId = -1;
            if( pathToRoot != null && pathToRoot.size() > 0 ){
                primaryTabId = ((Integer)pathToRoot.get(0)).intValue();
            } else {
                //default to home tab?
                Portal portal=(Portal)vreq.getAttribute("portalBean");
                if( portal == null ) {
                    log.error("getSecondaryTabMenu() must have attribute 'portalBean' in vitro request");
                    primaryTabId=Portal.DEFAULT_ROOT_TAB_ID;
                } else {
                    primaryTabId = portal.getRootTabId();
                }
             }
            return getSecondaryTabMenu(vreq,primaryTabId);
        } catch (Throwable t) {
            t.printStackTrace();
            return "";
        }
    }

    private static String getSecondaryTabMenu(VitroRequest vreq, int primaryTabId){
        try {
            int portalId=Portal.DEFAULT_PORTAL_ID;
            Portal portal=null;
            Object obj = vreq.getAttribute("portalBean");
            if( obj == null ) {
                log.error("getPrimaryTabMenu() must have attribute 'portalBean' in vitro request");
            } else {
                portal = (Portal)obj;
                portalId=portal.getPortalId();
            }
            String ret="";
            List secondaryTabs = vreq.getWebappDaoFactory().getTabDao().getSecondaryTabs( primaryTabId );
            if (secondaryTabs != null && secondaryTabs.size()>0){
                ret += secondaryOpen;
                Iterator it = secondaryTabs.iterator();
                while( it.hasNext() ){
                    Tab tab = (Tab) it.next();
                    boolean active = isTabActive(vreq, tab.getTabId());
                    ret += "\t";
                    if (active) {
                        ret += secondaryItemOpen;
                        ret += TabWebUtil.tab2TabAnchorElement(tab,vreq,TabWebUtil.SECONDARY,secondaryActiveClass,null,portalId)+secondaryItemClose;
                    }
                    else {
                        ret += secondaryItemOpen;
                        ret += TabWebUtil.tab2TabAnchorElement(tab,vreq,TabWebUtil.SECONDARY,null,null,portalId)+secondaryItemClose;
                    }
                }
                ret += secondaryClose;
            }
            return ret;
        } catch (Throwable t) {
            t.printStackTrace();
            return "";
        }
    }

    /**
     * A tab is active is it is the current tab, or it is a broader tab.
     * Currently this uses getTabsAboveCurrent() which queries the db
     * for the tab hierarchy above the current tab.  This assumes that
     * all tabs have only one broaded tab.
     *
     * @param req
     * @param tabInQuestion
     */
    public static boolean isTabActive(VitroRequest vreq, int tabInQuestion){
        int current = TabWebUtil.getTabIdFromRequest(vreq);
        if( current == tabInQuestion ) return true;
        List above = getTabsAboveCurrent(vreq);
        if( above != null && above.size() > 0 ){
            HashSet set = new HashSet( above );
            return set.contains(new Integer( tabInQuestion ));
        } else
            return false;
    }

    private static List getTabsAboveCurrent( VitroRequest vreq){
        List above = (List)vreq.getAttribute("tabsAboveCurrent");
        if( above == null ){
            int currentTab = TabWebUtil.getTabIdFromRequest(vreq);
            above = vreq.getWebappDaoFactory().getTabDao().getTabHierarchy(currentTab, -1);
            vreq.setAttribute("tabsAboveCurrent", above);
        }
        return above;
    }
}
