/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.PageController;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
/**
 * This filter is intended to route requests to pages defined in the display model.
 *
 * It should be last in the chain of filters since it will not call filters further
 * down the chain.
 *
 * It should only be applied to requests, not forwards, includes or errors.
 */
@WebFilter(filterName = "PageRoutingFilter", urlPatterns = {"/*"}, dispatcherTypes = {DispatcherType.REQUEST})
public class PageRoutingFilter implements Filter{
    protected FilterConfig filterConfig;

    private final static Log log = LogFactory.getLog( PageRoutingFilter.class);

    protected final static String URL_PART_PATTERN = "(/[^/]*).*";
    protected final static String PAGE_CONTROLLER_NAME = "PageController";
    protected final static String HOME_CONTROLLER_NAME = "HomePageController";

    protected final Pattern urlPartPattern = Pattern.compile(URL_PART_PATTERN);

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.filterConfig = arg0;
        log.debug("pageRoutingFilter setup");
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain)
        throws IOException, ServletException {
        ServletContext ctx = filterConfig.getServletContext();

		PageDao pageDao = ModelAccess.on(ctx).getWebappDaoFactory().getPageDao();
        Map<String,String> urlMappings = pageDao.getPageMappings();

        // get URL without hostname or servlet context
        HttpServletResponse response = (HttpServletResponse) arg1;
        HttpServletRequest req = (HttpServletRequest) arg0;
        // UQAM Manage UTF-8 for i18n
 //       req.setCharacterEncoding("UTF-8");
 //       response.setContentType("text/html; charset=UTF-8");
 //       response.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // check for first part of path
        // ex. /hats/superHat -> /hats
        Matcher m = urlPartPattern.matcher(path);
        if( m.matches() && m.groupCount() >= 1){
            String path1stPart = m.group(1);
            String pageUri = urlMappings.get(path1stPart);

            //try it with a leading slash?
            if( pageUri == null )
                pageUri = urlMappings.get("/"+path1stPart);

            if( pageUri != null && ! pageUri.isEmpty() ){
                log.debug(path + "is a request to a page defined in the display model as " + pageUri );

                //add the pageUri to the request scope for use by the PageController
                PageController.putPageUri(req, pageUri);

                //This will send requests to HomePageController or PageController
                String controllerName = getControllerToForwardTo(req, pageUri, pageDao);
                log.debug(path + " is being forwarded to controller " + controllerName);

                RequestDispatcher rd = ctx.getNamedDispatcher( controllerName );
                if( rd == null ){
                    log.error(path + " should be forwarded to controller " + controllerName + " but there " +
                    		"is no servlet named that defined for the web application in web.xml");
                    //TODO: what should be done in this case?
                }

                rd.forward(req, response);
            }else if( "/".equals( path ) || path.isEmpty() ){
                log.debug("url '" +path + "' is being forward to home controller" );
                RequestDispatcher rd = ctx.getNamedDispatcher( HOME_CONTROLLER_NAME );
                rd.forward(req, response);
            }else{
                doNonDisplayPage(path,arg0,arg1,chain);
            }
        }else{
            doNonDisplayPage(path,arg0,arg1,chain);
        }
    }

    protected void doNonDisplayPage(String path, ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException, ServletException{
        log.debug(path + "this isn't a request to a page defined in the display model, handle it normally.");
        chain.doFilter(arg0, arg1);
    }

    protected String getControllerToForwardTo(HttpServletRequest req,
            String pageUri, PageDao pageDao) {
        String homePageUri = pageDao.getHomePageUri();
        if( pageUri != null && pageUri.equals(homePageUri) )
            return HOME_CONTROLLER_NAME;
        else
            return PAGE_CONTROLLER_NAME;
    }

    @Override
    public void destroy() {
       //nothing to do here
    }
}
