/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Checks incomming requests to see if they should be routed to a portal.
 * This will override any parameters, attributes, and session.attributes.
 *
 * The portal prefixes come from the Portal objects and currently are stored
 * in the Portals table in the urlprefix column.  The portal prefixes
 * are only loaded at the servlet start up.  So if you make a change the
 * context has to be restarted.
 *
 * The incomming url will look something like this:
 * http://hostname/contextpath/part1/part2?parameter1&parameter2...parameterN
 *
 * We will drop the hostname and contextpath and look at:
 * /part1/part2?parameter1&parameter2...parameterN
 *
 * Then we will extract part1.  If part1 is not in the list of protected
 * directories, and is in the prefix2portalid Hash then the request
 * will be forced into the portalid and the prefix will be stripped from
 * the url.  Then the request will be forwarded to the stripped url.
 *
 * If the stripped request is "", "/" or "index.jsp" the request will
 * be forwarded to the defaultIndex.
 *
 * Once the Portal object is created it is placed in the request scope
 * under the key "portalBean" and in the session scope under "currentPortal"
 *
 * Here is what needs to go into the web.xml:
 *
    <filter>
        <filter-name>Portal Picker Filter</filter-name>
        <filter-class>edu.cornell.mannlib.vitro.filters.PortalPickerFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>Portal Picker Filter</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>
 * We will use the
 * @author bdc34
 * some of this code is from URLRewriteFilter
 */
public class PortalPickerFilter implements Filter {
    private FilterConfig filterConfig = null;
    private static HashMap<String,Integer> prefix2portalid;
    private static HashMap<Integer, String> portalid2prefix;
    private static HashSet<String> protectedPrefixes;
    private String defaultIndex = "/index.jsp";

    /**
     * This is the key to a String that gets put in
     * the request. "true" indicates that portal
     * prefix filtering is active.
     */
    public static final String USING_PORTAL_PREFIXES = "usingPortalPrefixes";
    /**
     * These are directories that no protal should ever use as a prefix
     * since they are directories that we might want to access in the webapp.
     */
    String[] protectedPrefixV = {
            "admin",
            "js",
            "help",
            "quickedits",
            "src",
            "templates",
            "themes",
            "WEB-INF",
            "xsl"
    };

    static boolean isPortalPickingActive;

    private static final Log log = LogFactory.getLog(PortalPickerFilter.class.getName());

    public static PortalPickerFilter getPortalPickerFilter(ServletContext context) {
    	return (PortalPickerFilter) context.getAttribute(PortalPickerFilter.class.getName());
    }
    
    public HashMap<String,Integer> getPrefix2PortalIdMap() {
    	return prefix2portalid;
    }
    
    public HashMap<Integer,String> getPortalId2PrefixMap() {
    	return portalid2prefix;
    }
    
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            log.info("init() of filter");
            filterConfig.getServletContext().setAttribute(PortalPickerFilter.class.getName(),this);
            this.filterConfig = filterConfig;
            this.prefix2portalid = new HashMap<String,Integer>();

            String defaultIndex = (String)filterConfig.getInitParameter("default");
            if(defaultIndex != null )
                this.defaultIndex = defaultIndex;

            protectedPrefixes = new HashSet<String>();
            for( String dir : protectedPrefixV ){
                protectedPrefixes.add(dir);
            }

            WebappDaoFactory fac = (WebappDaoFactory) filterConfig.getServletContext().getAttribute("webappDaoFactory");
            setupPortalMappings( fac.getPortalDao().getAllPortals() );
            log.debug("init() done");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets up the portal mappings.  May be called at context init or at any other time.
     *
     * Notice that this isn't thread safe.  The main problem is reading the
     * prefix2portalid and isPortalPickingActive by the doFilter() method
     * while setupPortalMappings() is being run.  This could be solved
     * by making doFilter synchronized but then only one thread could
     * be in doFilter at a time and that might be a performance issue.
     *
     * Since setupPortalMappings is synchronized we cannot get an
     * inconsistent state after it returns.  There will be a short
     * time when it is running when the prefix2portalid and isPortalPickingActive
     * might be inconsistent.  If a different thread calls doFilter at
     * that time and gets a bad result we should be able to live with
     * it.
     *
     */
    public static synchronized void setupPortalMappings(Collection<Portal>portals) {
        if( portals == null || portals.size() < 1 ){
            log.debug("No portal mappings found in db. Filtering for portals will be inactive");
            prefix2portalid = new HashMap<String,Integer>();
            isPortalPickingActive = false;
            return;
        }

        int mappingCount = 0;
        boolean prefixCollision = false;
        HashMap<String,Integer> newPrefixMap = new HashMap<String,Integer>();
        HashMap<Integer,String> newPortalIdMap = new HashMap<Integer,String>();
        for(Portal portal : portals){
            if( portal == null ) continue;
            String urlprefix = portal.getUrlprefix();

            if(urlprefix == null || urlprefix.trim().length() == 0
                    || "&nbsp;".equalsIgnoreCase(urlprefix)){
                log.debug("no url prefix mapping for portal "
                        + portal.getAppName()  + " id " + portal.getPortalId());
                continue;
            }
            urlprefix = urlprefix.trim();
            if( protectedPrefixes.contains( urlprefix )){
                log.error("the prefix " + urlprefix + " is a directory that is in the webapp"+
                        " and may not be used as a prefix for portal " + portal.getPortalId());
                continue;
            }

            if( newPrefixMap.containsKey( urlprefix )){
                Integer i = newPrefixMap.get( urlprefix );
                log.debug("multiple portals have the url prefix " + urlprefix +
                        ", both portals id:" + i.toString() + " and id:" + portal.getPortalId());
                newPrefixMap.remove( urlprefix );
                newPortalIdMap.remove(i);
                prefixCollision = true;
            }

            log.debug("urlprefix:'" + urlprefix + "' -> \t\tportalid: " + portal.getPortalId() );
            newPrefixMap.put(urlprefix, new Integer(portal.getPortalId()));
            newPortalIdMap.put(new Integer(portal.getPortalId()), urlprefix);
            
            mappingCount++;
        }

        if( mappingCount > 0 ){
            prefix2portalid = newPrefixMap;
            portalid2prefix = newPortalIdMap;
            isPortalPickingActive = true;
        } else {
            prefix2portalid = new HashMap<String,Integer>();
            portalid2prefix = new HashMap<Integer,String>();
            isPortalPickingActive = false;
        }

       log.info("final mappings:");
       for( String key : prefix2portalid.keySet()){
                Integer id = prefix2portalid.get(key);
                log.info("portalid: " + id + "\turlprefix:'" + key + "'");
       }
       if( prefixCollision )
           log.info("there were at least two portals that had the same prefix.");

    }

    /**
     * This is the method that will be called on an requests that pass through
     * this filter.  It will check for a prefix and if found, strip it off,
     * force the request to that portal and forward the request to
     * the stripped url.
     *
     * If no prefix is found it will just pass it along.
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if( !isPortalPickingActive ){ //bail right away if not active
            chain.doFilter(request,response);
            return;
        }

        HttpServletResponse hResponse = (HttpServletResponse)response;
        HttpServletRequest hRequest = (HttpServletRequest) request;

        hRequest.setAttribute(USING_PORTAL_PREFIXES, "true");

        String originalUrl = getUrl(hRequest);
        String prefix = getPrefix(originalUrl);

        /* we have a prefix, it is not protected and it is in the mapping */
        if( prefix != null && prefix.length() > 0
            && !protectedPrefixes.contains(prefix)
            && prefix2portalid.containsKey(prefix) ){
            /* found a prefix so set the home attribute and strip the prefix */
            final Integer id = prefix2portalid.get(prefix)  ;

            if( id != null ){
                if (hResponse.isCommitted()) {
                    log.error("response is comitted cannot forward " +
                            " (check you haven't done anything to the response (ie, written to it) before here)");
                    return;
                }

                if( checkForTralingSlashProblem(originalUrl, prefix) ){
                    String redirect = hRequest.getRequestURI() + '/';
                    if( log.isDebugEnabled() )
                        log.debug("Redirecting because of missing trailing slash.  orginal Url: " + originalUrl + " redirect: " + redirect);
                    hResponse.sendRedirect( redirect );
                }else{
                    hRequest.setAttribute("convertParameterEncoding", new Boolean(false)); // forwarding seems to make VitroRequest's encoding-converting methods redundant; need to disable them for this request
                    forceToPortal(hRequest,hResponse,id);
                    final String target = getTarget(originalUrl, prefix);
                    final RequestDispatcher rq = getRequestDispatcher(hRequest, target);
                    if( log.isDebugEnabled() )
                        log.debug("prefix found and is valid. orginal Url: " + originalUrl + " prefix: " + prefix
                                + " forwared to: " + target);
                    rq.forward(hRequest, hResponse);
                }
            }
        }else{
            if( log.isDebugEnabled() )
                log.debug("No usable prefix found. orginal Url: " + originalUrl + " invalid prefix: " + prefix
                        + " forwared to: " + originalUrl);
            chain.doFilter(hRequest, hResponse);
        }
    }

    /**
     * Modifies the hRequest so that the Vitro system will consider it
     * to be in the portal given by id.
     *
     * @param hRequest
     * @param hResponse
     * @param id
     */
    private final void forceToPortal(HttpServletRequest hRequest, HttpServletResponse hResponse, Integer id){
        VitroRequestPrep.forceToPortal(hRequest,id);
    }

    /**
     * Gets the url that we rewrite the originalUrl to. This does the
     * mapping to the default page.
     *
     * "/prefix"             -> $defaultIndex
     * "/prefix/"            -> $defaultIndex
     * "/prefix/index.jsp"   -> $defaultIndex
     * "/prefix/bla"         -> "/bla"
     * "/prefix/bla?what=ya" -> "/bla?what=ya"
     *
     * @param originalUrl
     * @param prefix
     * @return
     */
    private final String getTarget(String originalUrl, String prefix){
        if( originalUrl != null && originalUrl.length() > prefix.length() ){
            String out = originalUrl.substring(prefix.length() + 1 );

            if( "/".equals(out) || "".equals(out) || out.startsWith("index.jsp") )
                return defaultIndex;

            return out;
        }else{
            return originalUrl;
        }
    }
    /**
     * Gets the url prefix.  Return empty string if no prefix found.
     * We expect that the originalUrl will have a leading slash.
     *
     * We want:
     *   /                         ->  ""
     *   /cals/index.jsp&bla=3     ->  "cals"
     *   /cals                     ->  "cals"
     *   /entity?id=233            ->  ""
     *   /themes/images/bleck.gif  ->  "themes"  we have a a list of protected dirs
     *
     * @return prefix
     */
    private final String getPrefix(String originalUrl) {
        if (originalUrl == null || originalUrl.length() < 3)
            return "";

        String prefix = "";
        int secondslash = originalUrl.substring(1).indexOf('/');
        int end = -1;
        if (secondslash > 1 && originalUrl.length() > secondslash + 1) {
            // look for form "/prefix/blabla"
            log.debug("form a");
            prefix = originalUrl.substring(1, secondslash + 1);
        } else {
            if( secondslash < 1){
                //we have not found a second slash, something like "/prefix" or maybe "/prefix?home=bla"
                int questionmark = originalUrl.substring(1).indexOf('?');
                if( questionmark >= 1 ){
                    log.debug("form b");
                    prefix = originalUrl.substring(1,questionmark + 1); // form like "/prefix?bla=bla"
                }else{
                    log.debug("form c");
                    prefix = originalUrl.substring(1); //form like "/prefix"
                }
            }
        }
        return prefix;
    }

    /**
     * When we get a request like http://bla.edu/landgrant the urls are not
     * set relative to landgrant but bla.edu.  The effect is that the browser
     * looks for http://bla.edu/entity?id=33 and not http://bla.edu/landgrant/entity?id=33
     *
     * We solve this by checking for the condition of http://bla.edu/prefix with no trailing
     * slash.  This is our Trailing Slash Problem.  We redirect these problem requests
     * to http://bla.edu/prefix/
     *
     * @param url
     * @param prefix
     * @return
     */
    private boolean checkForTralingSlashProblem(String url, String prefix){
        log.debug("url: " + url + " prefix: " + prefix);
        if( url == null ) return false;
        if( url.equals(prefix) || url.equals('/'+prefix ) )
            return true;
        else
            return false;
    }

    /**
     * Gets the url out of the request.  Strips off contextPath
     */
    private final String getUrl(HttpServletRequest hRequest){
        String originalUrl = hRequest.getRequestURI();
        if (originalUrl == null || originalUrl.trim().length() == 0 ) {
            log.debug("unable to fetch request uri from request.  This shouldn't happen, it may indicate that " +
                    "the web application server has a bug or that the request was not pased correctly.");
            return null;
        }

//        try {
//            originalUrl = URLDecoder.decode(originalUrl, "utf-8");
//            if (log.isDebugEnabled()) {
//                log.debug("after utf-8 decoding " + originalUrl);
//            }
//        } catch (java.io.UnsupportedEncodingException e) {
//            log.warn("the jvm doesn't seem to support decoding utf-8, matches may not occur correctly.");
//            return originalUrl;
//        }

        // check for context path and remove if it's there
        String contextPath = hRequest.getContextPath();
        if (contextPath != null && originalUrl.startsWith(contextPath)) {
            originalUrl = originalUrl.substring(contextPath.length());
        }

        // add the query string on uri (note, some web app containers do this)
        if (originalUrl.indexOf("?") == -1) {
            String query = hRequest.getQueryString();
            if (query != null) {
                query = query.trim();
                if (query.length() > 0) {
                    originalUrl = originalUrl + "?" + query;
                }
            }
        }

        return originalUrl;
    }

    private RequestDispatcher getRequestDispatcher(final HttpServletRequest hsRequest, String toUrl) throws ServletException {
        final RequestDispatcher rq = hsRequest.getRequestDispatcher(toUrl);
        if (rq == null) {
            // this might be a 404 possibly something else, could re-throw a 404 but is best to throw servlet exception
            throw new ServletException("unable to get request dispatcher for " + toUrl);
        }
        return rq;
    }

    public void destroy() {
        prefix2portalid = null;
        filterConfig = null;
    }

}
