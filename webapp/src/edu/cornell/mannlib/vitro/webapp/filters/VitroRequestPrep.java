/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.FilterFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HideFromDisplayByPolicyFilter;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.flags.FlagException;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.flags.SunsetFlag;

/**
 * This sets up several objects in the Request scope for each
 * incoming HTTP request.  This is done in a Filter so
 * that controllers and JSPs get the same setup.
 *
 * This code configures the WebappDaoFactory for each request.
 * 
 * @author bdc34
 *
 */
public class VitroRequestPrep implements Filter {
    ServletContext _context;
    ApplicationBean _appbean;    
    
    static FilterFactory filterFactory = null;
    
    /**
     * The filter will be applied to all incoming urls,
     this is a list of URI patterns to skip.  These are
     matched against the requestURI sans query paramerts,
     * ex
     * "/vitro/index.jsp"
     * "/vitro/themes/enhanced/css/edit.css"
     *
    */
    Pattern[] skipPatterns = {
            Pattern.compile(".*\\.(gif|GIF|jpg|jpeg)$"),
            Pattern.compile(".*\\.css$"),
            Pattern.compile(".*\\.js$"),
            Pattern.compile("/.*/themes/.*/site_icons/.*"),
            Pattern.compile("/.*/images/.*")
    };

    private static final Log log = LogFactory.getLog(VitroRequestPrep.class.getName());

    @Override
	public void doFilter(ServletRequest  request,
                          ServletResponse response,
                          FilterChain     chain)
    throws IOException, ServletException {
    	// If this isn't an HttpServletRequest, we might as well fail now.
    	HttpServletRequest req = (HttpServletRequest) request;

        //don't waste time running this filter again.
        if( req.getAttribute("VitroRequestPrep.setup") != null ){
            log.debug("VitroRequestPrep has already been executed at least once, not re-executing.");
            Integer a =(Integer) req.getAttribute("VitroRequestPrep.setup");
            req.setAttribute("VitroRequestPrep.setup", new Integer( a + 1 ) );
            chain.doFilter(req, response);
            return;
        }

        for( Pattern skipPattern : skipPatterns){
            Matcher match =skipPattern.matcher( req.getRequestURI() );
            if( match.matches()  ){
                log.debug("request matched a skipPattern, skipping VitroRequestPrep"); 
                chain.doFilter(req, response);
                return;
            }
        }
        
        VitroRequest vreq = new VitroRequest(req);

		if (log.isDebugEnabled()) {
			try {
				String logRequestStr = vreq.getRequestURI();
				if ( (vreq.getQueryString() != null) && (vreq.getQueryString().length()>0) ) {
					logRequestStr += "?" + vreq.getQueryString();
				}
				log.debug("RequestURI: "+logRequestStr);
			} catch (Exception e) {
				// Just in case something goes horribly wrong
				// Don't want logging to kill the request
			}
		}

        //-- setup appBean --//
        vreq.setAppBean(_appbean);

        //-- setup sunserFlag --//
        SunsetFlag sunsetFlag = new SunsetFlag();
        if( _appbean != null )
            sunsetFlag.filterBySunsetDate = _appbean.isOnlyCurrent();
        vreq.setSunsetFlag(sunsetFlag);

        //-- setup DAO factory --//
        WebappDaoFactory wdf = getWebappDaoFactory(vreq);
        //TODO: get accept-language from request and set as preferred languages
        
        //-- setup portal and portalFlag --//
        Portal portal = null;
        PortalFlag portalFlag = null;
        PortalDao portalDao = wdf.getPortalDao();
        try{
            portal = getCurrentPortalBean(req, true, portalDao);
            if ( (portal == null) && (response instanceof HttpServletResponse) ) {
            	((HttpServletResponse)response).sendError(404);
            	return;
            }
            vreq.setPortal(portal);
            portalFlag = new PortalFlag(req,_appbean, portal, wdf);
            vreq.setPortalFlag(portalFlag);
        }catch(FlagException ex){
            System.out.println("could not make portal flag" + ex);
        }
                       
        WebappDaoFactory sessionDaoFactory = null;
    	Object o = req.getSession().getAttribute("webappDaoFactory");
    	if ( (o != null) && (o instanceof WebappDaoFactory) ) {
    		sessionDaoFactory = (WebappDaoFactory) o;
    	}
        
        RoleLevel role = RoleLevel.getRoleFromLoginStatus(req);
        log.debug("setting role to "+role.getShorthand());
        
        if (sessionDaoFactory != null) {
        	log.debug("Found a WebappDaoFactory in the session and using it for this request");
        	wdf = sessionDaoFactory;
        } else if (portal.getWebappDaoFactory() != null) {
            log.debug("Found a WebappDaoFactory in the portal and using it for this request");
        	wdf = portal.getWebappDaoFactory();  // BJL 2008-03-05 : I'm trying this out for portals that filter by having their own submodel        	
        } else {        	
	        VitroFilters filters = null;
			        
	        filters = getFiltersFromContextFilterFactory(req, wdf);
	        
	        if( filters != null ){
	            log.debug("Wrapping WebappDaoFactory in portal filters");
	            wdf = new WebappDaoFactoryFiltering(wdf, filters);
	        }
        }                          

		/*
		 * display filtering happens now at any level, all the time; editing
		 * pages get their WebappDaoFactories differently
		 */
		HideFromDisplayByPolicyFilter filter = new HideFromDisplayByPolicyFilter(
				RequestIdentifiers.getIdBundleForRequest(req),
				ServletPolicyList.getPolicies(_context));
		vreq.setWebappDaoFactory(new WebappDaoFactoryFiltering(wdf, filter));
		
        // support for Dataset interface if using Jena in-memory model
        if (vreq.getDataset() == null) {
        	Dataset dataset = WebappDaoFactoryJena.makeInMemoryDataset(
        	        vreq.getAssertionsOntModel(), vreq.getInferenceOntModel());
        	vreq.setDataset(dataset);
        }
        
        req.setAttribute("VitroRequestPrep.setup", new Integer(1));
        chain.doFilter(req, response);
    }

    private WebappDaoFactory getWebappDaoFactory(VitroRequest vreq){
    	WebappDaoFactory webappDaoFactory = vreq.getWebappDaoFactory();
        return (webappDaoFactory != null) ? webappDaoFactory :
        	(WebappDaoFactory) _context.getAttribute("webappDaoFactory");
    }

    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
        _context = filterConfig.getServletContext();

        Object o =  _context.getAttribute("applicationBean");
        if (o instanceof ApplicationBean) {
            _appbean = (ApplicationBean) o; 
        } else {
            _appbean = new ApplicationBean();
        }
    }    
    
    public VitroFilters getFiltersFromContextFilterFactory( HttpServletRequest request, WebappDaoFactory wdf){
        FilterFactory ff = getFilterFactory();
        if( ff == null ){ 
            return null;
        } else {
            return ff.getFilters(request, wdf);
        }
    }
    
    public FilterFactory getFilterFactory(){
        return(FilterFactory)_context.getAttribute("FilterFactory");        
    }
    public static void setFilterFactory(ServletContext sc, FilterFactory ff){
        sc.setAttribute("FilterFactory", ff);
    }
    
    /* ********** Static Utility Methods **************** */
    /**
     *  This method attempts to get a useful Portal object given the state of the request and session.
     *  If one is found stick the id in the current session and stick the Portal object in the
     *  current session.
     *
     * @param request
     * @param forcePortalParameter - if true, ignore any portal info in the session
     * and try to use the HTTP parameter for the portalID
     * bdc34: I don't know where forcePortalParameter is being used, it might be legacy code.
     * jc55:  The ability to force a change in the portal was implemented for the CALS Research
     *        portals, where curators wanted the ability for people to pick a portal to search
     *        in and then be "forced" into that portal when results were displayed.
     *        It may also be used to initially get people into the portal from a /lifesci or /socsci parameter
     * It is safe to pass false if you are not sure what to pass here.
     * @return
     */
    private static Portal getCurrentPortalBean(HttpServletRequest request, boolean forcePortalParameter, PortalDao portalDao) {
        if(log.isDebugEnabled())
            log.debug("entering getCurrentPortalBean, forcePortalParameter:" + forcePortalParameter);

        if( request == null ){
            log.error("getCurrentPortalBean() needs a usable VitroRequest to access the PortalDao");
            return null;
        }
        Portal portal = null;
        HttpSession currentSession = request.getSession(true);
        if( forcePortalParameter ){
            int forcedPortalId = getCurrentPortalId(request,true);
            portal = portalDao.getPortal(forcedPortalId);
        }else{
            /* Look for a portal Object in the session */
            if( currentSession != null )
                portal = getPortalFromSession(currentSession);
        }
        if( portal == null ){
            int portalIdFromRequest = getCurrentPortalId(request,false);
            portal = portalDao.getPortal(portalIdFromRequest);
        }
        if( portal != null )
            putPortalIntoSession(currentSession,portal);

        return portal;
    }

    /**
     * returns null if no portal found.
     * @param session
     * @return
     */
    private static Portal getPortalFromSession(HttpSession session){
        if( session == null )
            return null;
        Object currentPortal = session.getAttribute("currentPortal");
        if( currentPortal != null && currentPortal instanceof Portal){
            if(log.isDebugEnabled())
                log.debug("Found Portal in session: " +((Portal)currentPortal).getPortalId());
            return (Portal)currentPortal;
        }else
            return null;
    }

    /**
     * returns -1 if no portal id was found in session.
     * @param currentSession
     * @return
     */
    private static int getPortalIdFromSession(HttpSession currentSession){
        int portalId = -1;
        Object currentPortalObj=currentSession.getAttribute("currentPortalId");
        if (currentPortalObj != null && currentPortalObj instanceof Integer) {
            int portalIdFromSession=((Integer)currentPortalObj).intValue();
            if(log.isDebugEnabled())
                log.debug("Found portal id in session: " + portalIdFromSession);
            if ( portalIdFromSession > 0 ){
                portalId = portalIdFromSession;
            }
        }
        return portalId;
    }

    private static void putPortalIntoSession(HttpSession session, Portal portal){
        session.setAttribute("currentPortalId",new Integer(portal.getPortalId()));
        session.setAttribute("currentPortal",portal);
    }

    /**
     * Gets the id of the portal indicated by the request or session object.
     * Changing 10/11/07 so private; all
     * @param request
     * @param forcePortalParameter
     * @return
     */
    private static int getCurrentPortalId(HttpServletRequest req, boolean forcePortalParameter) {
        if(log.isDebugEnabled())
            log.debug("entering getCurrentPortalId, forcePortalParameter:" + forcePortalParameter);

        VitroRequest request = new VitroRequest(req);
        
        int portalId = Portal.DEFAULT_PORTAL_ID;
        int portalIdFromSession=-1;
        HttpSession currentSession = request.getSession(true);

        /* Look for a portal id in the session */
        boolean noPortalFoundInSession = true;
        portalIdFromSession = getPortalIdFromSession(currentSession);
        if ( portalIdFromSession > 0 ){
            portalId = portalIdFromSession;
            noPortalFoundInSession = false;
        } else {
            portalId = Portal.DEFAULT_PORTAL_ID;
            noPortalFoundInSession = true;
        }


        if (forcePortalParameter || noPortalFoundInSession ) {
            /* if forcePortalParameter is true, ignore the portal id from the session */
            String idStr ="undefined";
            if( request.getAttribute("home") != null)
                idStr = (String)request.getAttribute("home");
            else if( request.getParameter("home") != null)
                idStr = request.getParameter("home");
            else if( request.getAttribute("home") != null)
                idStr = (String)request.getAttribute("home");
            else
                idStr = "undefined";

            try {
                int portalIdForced=Integer.parseInt(idStr);
                if (portalIdFromSession>=0 && portalIdFromSession!=portalIdForced) { // invalidate saved search preferences
                    currentSession.removeAttribute("flag1pref");
                    currentSession.removeAttribute("flag2pref");
                    currentSession.removeAttribute("flag3pref");
                }
                portalId = portalIdForced;
                if(log.isDebugEnabled())
                    log.debug("forcePortalParameter so using portal id " + portalId + " from parameter 'home'");
            } catch (NumberFormatException ex) {
                /* if we can't format the parameter, try portalid from session, if no session portal id then a default portal id */
                if ( portalIdFromSession > 0 ) {
                    portalId = portalIdFromSession;
                    if(log.isDebugEnabled())
                        log.debug("tried to forcePortalParameter but could not parse '"
                                + idStr +"' using id of " + portalId + " from session.");
                } else {
                    portalId = Portal.DEFAULT_PORTAL_ID;
                    if(log.isDebugEnabled())
                        log.debug("tried to forcePortalParameter but could not parse '"
                                + idStr +"' using default portal id of " + Portal.DEFAULT_PORTAL_ID +
                                " This happens when no incoming home parameter and current session is null");
                }
            } /* end of catch (NumberFormatException ex) */
        }/* end of if(forcePortalParameter) */

        return portalId;
    }

    /**
     * This should wipe out all portal state and force the portal to be the indicated one.
     * @param hRequest
     * @param hResponse
     * @param id
     * This is only used in PortalPickerFilter
     */
    public static void forceToPortal(HttpServletRequest request, Integer portalId){
        HttpSession currentSession = request.getSession(true);
        currentSession.setAttribute("currentPortalId", portalId);
        currentSession.setAttribute("currentPortal", null);
        (new VitroRequest(request)).setPortalId( portalId.toString() );
    }

	/**
	 * Check to see whether any of the current identifiers is a SelfEditing
	 * identifier.
	 */
	public static boolean isSelfEditing(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}

		IdentifierBundle idBundle = RequestIdentifiers.getIdBundleForRequest(request);
		SelfEditing selfId = SelfEditingIdentifierFactory.getSelfEditingIdentifier(idBundle);
		if (selfId == null) {
			return false;
		}
		
		return true;
	}

    @Override
	public void destroy() {
    	// Nothing to do.
    }


}
