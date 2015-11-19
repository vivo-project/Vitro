/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * A filter is a class that the tomcat servlet container
 * can pass a request to before or after it is handled by
 * a servlet.
 *
 * Add something like this to web.xml:
 *
 * <!-- Filters ******************************************************* -->
       <filter>
        <filter-name>Test Filter</filter-name>
        <filter-class>edu.cornell.mannlib.vitro.filters.TestFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>Test Filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
 *
 *
 */
public class TestFilter implements Filter {
    private FilterConfig filterConfig = null;
    private static final Log log = LogFactory.getLog(TestFilter.class.getName());
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        log.debug("in TestFilter.doFilter()");
        request.setAttribute("TestAttr","This is a test value, it could be a setup VitroFacade");
        chain.doFilter(request, response);
    }

    public void destroy() {
        filterConfig = null;
    }

}
