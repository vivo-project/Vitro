/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringEscapeUtils;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This is a collecton of method that deal tabs and
 * how they are used on jsp's and in servlets.
 * NOTE 0 is an invalid tab id, there should never be a tab 0;
 * -1 is also an invalid tab id.
 *
 * @author bdc34
 */
public class TabWebUtil {
    final static int ROOT_TAB_ID=Portal.DEFAULT_ROOT_TAB_ID; //Most likely vivo

     static String [] tabDepthNames =
    {"primary","secondary","collection","subcollection","more"};
     static int PRIMARY =0;
     static int SECONDARY=1;
     static int COLLECTION=2;
     static int SUBCOLLECTION=3;
     static int MORE=4;

     //some values from index.jsp
     final static boolean FILTER_BY_PORTAL               = true;
     final static int     TAB_FILTER_FLAG2               = 2; // see beans.Tab
     final static int     TAB_FILTER_FLAG3               = 3; // see beans.Tab
     final static int     TAB_FILTER_FLAG_BOTH           = 5; // see beans.Tab

    private static int getPrimaryTabIdFromRequest(HttpServletRequest request){
        return getNdepthTabId(request,tabDepthNames[PRIMARY],0);
    }
    private static int getSecondaryTabIdFromRequest(HttpServletRequest request){
        return getNdepthTabId(request,tabDepthNames[SECONDARY],0);
    }
    private static int getCollectionTabIdFromRequest(HttpServletRequest request){
        return getNdepthTabId(request,tabDepthNames[COLLECTION],0);
    }
    private static int getSubcollectionTabIdFromRequest(HttpServletRequest request){
        return getNdepthTabId(request,tabDepthNames[SUBCOLLECTION],0);
    }
    private static int getMoreTabIdFromRequest(HttpServletRequest request){
        return getNdepthTabId(request,tabDepthNames[MORE],0);
    }

    public static void tabPrep(HttpServletRequest request){
        //this will do all the legwork for getting ready to work with tabs
        //and stick the values into the request
        getTabIdFromRequest(request);
    }

    /**
     *
     * @param req
     * @author jc55
     * @return
     */
    public static int getTabIdFromRequest(HttpServletRequest request){
//       JCR 20050827 will convert so only one incoming tab id is necessary but may need
//       to support this on a legacy basis and until that is working correctly
//       BUT probably can't do this since tabs can have multiple parents and can't
//       necessarily determine the user's path to the tab from the tab alone

        Object obj = request.getAttribute("currentTabId") ;
        Integer value = (Integer)obj;

        if( value != null )
             return value.intValue();

        int depth = 0;
        int leadingTabId=0;  //0 is an invalid tab id, there should never be a tab 0;

        int incomingMoreId = getMoreTabIdFromRequest(request);
        if (incomingMoreId>0) {
            leadingTabId=incomingMoreId;
            depth = MORE;
        } else {
            int incomingSubCollectionId = getSubcollectionTabIdFromRequest(request);
            if (incomingSubCollectionId>0) {
                leadingTabId=incomingSubCollectionId;
                depth = SUBCOLLECTION;
            } else {
                int incomingCollectionId = getCollectionTabIdFromRequest(request);
                if (incomingCollectionId>0) {
                    leadingTabId=incomingCollectionId;
                    depth = COLLECTION;
                } else {
                    int incomingSecondaryTabId = getSecondaryTabIdFromRequest(request);
                    if (incomingSecondaryTabId>0) {
                        leadingTabId=incomingSecondaryTabId;
                        depth = SECONDARY;
                    } else {
                        int incomingPrimaryTabId = getPrimaryTabIdFromRequest(request);
                        if (incomingPrimaryTabId>0) {
                            leadingTabId=incomingPrimaryTabId;
                            depth = PRIMARY;
                        } else {
                            leadingTabId= getRootTabId(request);
                            depth = PRIMARY;
                        }
                    }
                }
            }
        }
        request.setAttribute("currentTabDepth", new Integer(depth));
        request.setAttribute("currentTabId",new Integer(leadingTabId));
        return leadingTabId;
    }

    public static int getRootTabId(HttpServletRequest request){
        VitroRequest vreq = new VitroRequest(request);
        Portal portal = (new VitroRequest(request)).getPortal();
        if( portal != null )
            return portal.getRootTabId();
        else
            return Portal.DEFAULT_ROOT_TAB_ID;
    }

    public final static String STASHED_KEY="stashTabsInRequestCalled";
    /**
     * puts pointers to Tab objects in request with names like
     * tab233 where the 233 is from TabBean.getTabId.
     * LeadingTab will be stashed also.
     */
    public static void stashTabsInRequest(Tab leadingTab, HttpServletRequest request)
    throws JspException{
        if(leadingTab == null){
            String e="attempt to call TabWebUtil.stashTabsInRequest() with null leadingTab";
            throw new JspException(e);
        }
        if(request == null){
            String e="attempt to call TabWebUtil.stashTabsInRequest() with null req";
            throw new JspException(e);
        }

        request.setAttribute( STASHED_KEY ,new Boolean(true));
        stashTabInReq(leadingTab,request); //stash leading tab

        Collection children = leadingTab.getChildTabs();
        if( children == null ) return;
        Iterator it = leadingTab.getChildTabs().iterator();
        while( it.hasNext() ){
            Tab tab = (Tab)it.next();
            stashTabInReq(tab,request);
        }
    }

    private static void stashTabInReq(Tab tab, HttpServletRequest request){
        request.setAttribute("tab"+String.valueOf(tab.getTabId()),tab);
    }
     /**
      * Get a TabBean that was placed in the Request by stashTabsInRequestCalled().
      * @param tabId
      * @param req
      * @return
      * @throws JspException
      */
    public static Tab findStashedTab( String tabId, HttpServletRequest request)
    throws JspException{
    	if( request == null )
    		throw new JspException("findStashedTab: request was null");
        if( request.getAttribute(STASHED_KEY) == null )
            throw new JspException("findStashedTab: called before stashTabsInRequest.");
        if( tabId == null)
            throw new JspException("findStashedTab: tabId was null");
        Object obj = request.getAttribute("tab"+tabId);
        if( obj == null )
            throw new JspException("findStashedTab: tab"+tabId+" not found in vitro request");
        if(!( obj instanceof Tab ))
            throw new JspException("findStashedTab: tab"+tabId+" was not a TabBean class"+
                    obj.getClass().getName());
        return (Tab)obj;
    }



    /**
     * Gets the page name of the jsp or whatever is needed in the
     * tab HTTP request, it will have the query ? ex: index.jsp?home=23
     *
     * @param req
     * @return
     *
     static String getIndexPage(VitroRequest vreq ){
        Portal p=RequestToPortal.getCurrent
        return "index.jsp?home="+RequestToPortal.getCurrentPortal(vreq,false);
    } */

    /**
     * Makes an anchor/link HTML element for the tab menu tag for the given tab.
     *
     * @param tab object to make a element for
     * @param req
     * @param depth the depth of the tab - see TabWebUtil.tabDepthNames
     * @param active - if true set CSS class as active
     * @param label - if not null, label overrides the tab.name
     * @param portalId - ID of the current portal in session
     * @return a HTML tag for the tab menu.
     */
     public static String tab2TabAnchorElement(Tab tab, HttpServletRequest req,
             int depth, String clazz, String label, int portalId){
//         int incomingPrimaryTabId = TabWebUtil.getPrimaryTabIdFromRequest(vreq);
         String tooltipStr = tab.getDescription();
         String cls=""; //this is the class='activeTab' for css use
         String title = "";
         String context = req.getContextPath();
         
         if (clazz != null)
             cls="class=\""+clazz+"\"";

         if (tooltipStr!=null && !tooltipStr.equals("") && !tooltipStr.equals("&nbsp;")) {
             title = " title=\""+tooltipStr+"\"";
         }

         String anchor = label != null ? label : tab.getTitle();
         String tabIdParam = tabDepthNames[depth] + "=" + tab.getTabId();
         String href="'" + context + "/index.jsp?home=" + portalId + "&amp;" + tabIdParam + "'";
         
         return "<a "+cls+title+" href="+href+">"+StringEscapeUtils.escapeXml(anchor)+"</a>";
     }

    /**
     * Used for parsing primary,secondary,etc parameters.
     * @param req
     * @param parameterName
     * @param defaultId
     * @return
     */
    private static int getNdepthTabId(HttpServletRequest request, String parameterName, int defaultId ){
        VitroRequest vreq=null;
        if (request instanceof VitroRequest) {
             vreq = (VitroRequest)request;
        } else {
            vreq = new VitroRequest(request);
        }
        // in the future when a tab has a URI, not just an id, we will want the URI
        // to be passed through a VitroRequest because of character set issues
        String idStr = vreq.getParameter(parameterName);
        int theId = defaultId;
        if ( idStr != null && !idStr.equals("") )
            try{
                theId = Integer.parseInt( idStr );
            }catch(Throwable th) {}
        return theId;
    }
}
