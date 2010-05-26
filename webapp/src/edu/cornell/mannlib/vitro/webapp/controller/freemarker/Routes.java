/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class Routes {
    
    private Routes() {
        throw new AssertionError();
    }

    private static final Log log = LogFactory.getLog(Routes.class.getName());
    
    protected static String contextPath = null;
    
    protected static final String ABOUT = "/about";
    protected static final String BROWSE = "/browse";
    protected static final String CONTACT = "/contact";
    protected static final String SEARCH = "/search"; 
    protected static final String TERMS_OF_USE = "/termsOfUse";
    
    // Put these under /admin/...
    // Currently login and site admin are on the same page, but they don't have to be.
    protected static final String LOGIN = "/siteAdmin";
    protected static final String LOGOUT = "/login_process.jsp"; 
    protected static final String SITE_ADMIN = "/siteAdmin";
    
    // Public values are used by view objects
    public static final String INDIVIDUAL = "/individual";
    public static final String INDIVIDUAL_LIST = "/entitylist";  // "/entitylist"; "/individuallist"; 
    
    
    public static String getHomeUrl(Portal portal) {
        String rootBreadCrumbUrl = portal.getRootBreadCrumbURL();
        return StringUtils.isEmpty(rootBreadCrumbUrl) ? contextPath : rootBreadCrumbUrl;  
    }
    
    public static String getUrl(String path) {
        if ( ! path.startsWith("/") ) {
            path = "/" + path;
        }
        return contextPath + path;
    }
    
    public static String getUrl(String path, Map<String, String> params) {
        
        String url = getUrl(path);       
        String glue = "?";
        for (String key : params.keySet()) {
            url += glue + key + "=" + urlEncode(params.get(key));
            glue = "&";
        }
        return url;
    }

    public static String urlEncode(String url) {
        String encoding = "ISO-8859-1";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(url, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding url " + url + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return encodedUrl;
    }

    public static String urlDecode(String url) {
        String encoding = "ISO-8859-1";
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(url, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error decoding url " + url + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return decodedUrl;
    }
    
 
}
