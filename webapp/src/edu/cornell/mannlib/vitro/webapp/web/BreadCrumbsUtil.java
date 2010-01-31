/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Intended to generate the bread crumb html element.
 * @author bdc34
 *
 */
public class BreadCrumbsUtil {
    private static final Log log = LogFactory.getLog(BreadCrumbsUtil.class.getName());

    /** separator used between bread crumbs */
    public static String separator = " &gt; ";

    /**
     * Populates a breadcrumb string for the current set of tabs, working
     * backward up the chain to the root.
     * In general this method finds the tab id that the request is on and then
     * gets the tab hierarchy from TabDao.  For each tab in the hierarchy
     * a bread crumb is added.
     * There are two deviations from this simple procedure:
     * 1) if there is a "RootBreadCrumb" defined in the portal bean of the
     *    request then that will be the first crumb.
     * 2) if the id of the first tab in the hierarchy equals the portal.rootTabId()
     *    then that crumb will have the label of  portalBean.getAppName()
     */
    public static String getBreadCrumbsDiv(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        String ret = "<div class='breadCrumbs'> ";
        try {
            String label = null, spacer = null;
            int tabId = TabWebUtil.getTabIdFromRequest(request); // has to be able to find a portal to get a tab id if none in request
            int rootId = TabWebUtil.getRootTabId(request); // also has to be able to find a portal to get a root tab is if none in request
            int depth = 0;
            // this is a third, final shot at getting a populated portal if none was in the request
            Portal portal= vreq.getPortal();

            //get the "RootBreadCrumb" if there is one
            ret += getRootBreadCrumb(vreq, separator, portal);

            List chain = vreq.getWebappDaoFactory().getTabDao().getTabHierarcy(tabId,rootId);
            for(int i=0; i<chain.size(); i++){
                Integer id = (Integer)chain.get(i);
                if( rootId == id.intValue() ){
                    depth = TabWebUtil.PRIMARY;
                    label = portal.getAppName();
                    spacer = "";
                } else {
                    depth =  i-1;
                    label = null;
                    spacer = separator;
                }
                ret += spacer + makeBreadCrumbElement(vreq,id.intValue(),depth,label);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return ret + " </div>";
    }

    /**
     * gets the root bread crumb from portal bean
     * @param req
     * @return
     */
    public static String getRootBreadCrumb(VitroRequest vreq,String spacer,Portal portal){
        String crumb = "";
        if (portal==null){
            log.error("getRootBreadCrumb() was passed a null portal");
        } else if (portal.getRootBreadCrumbURL() != null &&
            portal.getRootBreadCrumbAnchor() != null) {
            spacer = (spacer == null)? "": spacer;
            crumb="<a href='"+portal.getRootBreadCrumbURL()
                + "' > " + portal.getRootBreadCrumbAnchor() + "</a>"
                + spacer;
        }
        return crumb;
    }

//    if (portalBean.getRootBreadCrumbURL() != null && !portalBean.getRootBreadCrumbURL().equals("") && !portalBean.getRootBreadCrumbURL().equals("&nbsp;")) { %>
//    <a href="<%=portalBean.getRootBreadCrumbURL()%>"><%=portalBean.getRootBreadCrumbAnchor()%></a>
//    <%
//    } else { %>
//    <a href="index.jsp?home=<%=portalBean.getPortalId()%>"><%=portalBean.getAppName()%></a>

    /**
     * *******************************************************
     * @param req
     * @param tabId
     * @param depth
     * @param label, should be null but if not, then use as label
     * @return
     */
    private static String makeBreadCrumbElement(VitroRequest vreq, int tabId, int depth, String label){
        if( depth < 0 )
            depth = 0;
        String href = "index.jsp?"+TabWebUtil.tabDepthNames[depth]+"="+tabId;
        label = (label == null) ? vreq.getWebappDaoFactory().getTabDao().getNameForTabId(tabId) : label;
        return "<a href='"+href+"'>"+label+"</a>";
    }
}
