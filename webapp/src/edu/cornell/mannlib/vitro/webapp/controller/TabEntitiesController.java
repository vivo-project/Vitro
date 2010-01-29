package edu.cornell.mannlib.vitro.webapp.controller;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;

/**
 * Produces the entity lists for tabs.
 *
 * @author bdc34
 *
 */
public class TabEntitiesController extends VitroHttpServlet {
    private static final long serialVersionUID = -5340982482787800013L;

    private static final Log log = LogFactory.getLog(TabEntitiesController.class.getName());
    public static int TAB_DEPTH_CUTOFF = 3;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /***********************************************
     Display a set of entities for a tab, these entities
     may be manually linked, auto-linked, a mix of these two,
     or a gallery.

     request.attributes
     a Tab object for the tabId must be in the attributes.
     It should have the key

     request.parameters
     "tabId" id of the tab to do entities for

     "tabDepth" String that is the depth of the tab in the display for
     which we are doing entities.
     leadingTab = 1, child of leadingTab = 2, etc.

     "alpha" if set to an uppercase letter entities will be filtered
     to have only that initial.

     bdc34 2006-01-12 created

     *
     */
public void doGet( HttpServletRequest req, HttpServletResponse response )
    throws IOException, ServletException {
        //this will setup the portal
        super.doGet(req,response);

        VitroRequest request = new VitroRequest(req);
        
        String obj = null;
        try {
           obj = request.getParameter("tabDepth");
           if( obj == null ){
            String e="TabEntitesController expects that request parameter 'tabDepth' be set"
            +", use 1 as the leading tab's depth.";
            throw new ServletException(e);
          }
         int depth = Integer.parseInt((String)obj);
         if( depth >= TAB_DEPTH_CUTOFF){
             String tabId = request.getParameter("tabId");
             log.debug("\ttab "+tabId+" is at, "+ depth+" below "+ TAB_DEPTH_CUTOFF);
             return;
         }

         String tabId = request.getParameter("tabId");
         if( tabId == null ){
             String e="TabEntitiesController expects that request parameter 'tabId' be set";
             throw new ServletException(e);
         }

         Tab tab = TabWebUtil.findStashedTab(tabId,request);
         if( tab == null ){
             String e="TabEntitiesController expects that tab"+tabId+" will be in the request attribute. "
             +"It should have been placed there by a call to TabWebUtil.stashTabsInRequest in "
             +"tabPrimary.jsp";
             throw new ServletException(e);
         }

         String alpha = request.getParameter("alpha");
         boolean doAlphaFilter = false;
         if(( alpha != null && alpha.length() == 1) || tab.getGalleryRows()>1)
             /* bjl23 20061006:
              * The tab.getGalleryRows()>1 is a hack to use this field as
              * a switch to turn on alpha filter display in
              * non-gallery tabs.  We need to add a db field for this. */
             doAlphaFilter = true;

         //now we have the parameteres from the request,
         //branch to the different types of ways to handle things
         if(depth == 1 && tab.isGallery() && !doAlphaFilter){
             doGallery(tab, request,response);
         }else if( tab.isGallery() || doAlphaFilter ){
             doAlphaFiltered(alpha,tab,request,response);
         }else if( tab.isManualLinked() ){
             doManual(tab, request, response);
         }else if( tab.isAutoLinked() ){
             doAutoLinked( tab, request, response);
         }else if( tab.isMixedLinked() ){
             doAutoLinked( tab, request, response);
         }else{
             //what to do here when the entity link mod is unknown?
             log.debug("TabEntitiesController: doing none for tabtypeid: "+ tab.getTabtypeId() +" and link mode: " + tab.getEntityLinkMethod());
         }
        } catch (Throwable e) {
            request.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = request.getRequestDispatcher("/error.jsp");
            rd.include(request, response);
        }
    }

    private void doAlphaFiltered(String alpha, Tab tab,
            HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Collection ents = tab.getRelatedEntityList(alpha);
        if( ents != null )   {
            request.setAttribute("entities", ents);
            request.setAttribute("alpha",alpha);
            request.setAttribute("count",tab.grabEntityFactory().getRelatedEntityCount()+"");
            request.setAttribute("tabParam",tab.getTabDepthName()+"="+tab.getTabId());
            request.setAttribute("letters",tab.grabEntityFactory().getLettersOfEnts());
            request.setAttribute("servlet",Controllers.TAB);
            String jsp = Controllers.ENTITY_LIST_FOR_TABS_JSP;
            RequestDispatcher rd =
                request.getRequestDispatcher(jsp);
            rd.include(request, response);
        }else{
            //no entities, do nothing
        }
    }

    private void doGallery(Tab tab, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Collection ents = tab.getRelatedEntityList(null);
        if( ents != null && ents.size() > 0){
            request.setAttribute("entities", ents);
            request.setAttribute("rows", tab.getGalleryRows());
            request.setAttribute("columns", tab.getGalleryCols());
            String jsp = Controllers.TAB_ENTITIES_LIST_GALLERY_JSP;
            RequestDispatcher rd =
                request.getRequestDispatcher(jsp);
            rd.include(request, response);
        }else{
            doAutoLinked(tab,request,response);
        }
    }

    private void doAutoLinked(Tab tab, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Collection ents = tab.getRelatedEntityList(null);
        if( ents != null && ents.size() > 0 )   {
            request.setAttribute("entities", ents);
            request.setAttribute("alpha","none");
            request.setAttribute("count",tab.getAlphaUnfilteredEntityCount()+"");
            request.setAttribute("tabParam",tab.getTabDepthName()+"="+tab.getTabId());
            request.setAttribute("letters",Controllers.getLetters());
            request.setAttribute("servlet",Controllers.TAB);
            String jsp = Controllers.ENTITY_LIST_FOR_TABS_JSP;
            RequestDispatcher rd =
                request.getRequestDispatcher(jsp);
                rd.include(request, response);
        }else{
            //no entities, do nothing
        }
    }

    private void doManual(Tab tab, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Collection ents = tab.getRelatedEntityList(null);
        if( ents != null && ents.size() > 0 )   {
            request.setAttribute("entities", ents);
            String jsp = Controllers.ENTITY_LIST_FOR_TABS_JSP;
            RequestDispatcher rd =
                request.getRequestDispatcher(jsp);
            rd.include(request, response);
        }else{
            //no entities, do nothing
        }
    }
}
