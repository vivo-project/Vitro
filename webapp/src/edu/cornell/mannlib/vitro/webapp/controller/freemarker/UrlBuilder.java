/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.filters.PortalPickerFilter;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class UrlBuilder {

    private static final Log log = LogFactory.getLog(UrlBuilder.class.getName());
    
    private Portal portal;
        
    // This is static so that getUrl() can be static and refer to contextPath.
    // getUrl() is static so that view objects can call it without 
    // having to instantiate an object and inject knowledge of the context path, which 
    // they don't have.
    protected static String contextPath = null;
    
    private static boolean isMultiPortal = PortalPickerFilter.isPortalPickingActive();
    
    public enum Routes {
        ABOUT("/about"),
        BROWSE("/browse"),
        CONTACT("/contact"),
        INDIVIDUAL("/individual"),
        INDIVIDUAL_LIST("/entitylist"), // "/individuallist"
        SEARCH("/search"),
        TERMS_OF_USE("/termsOfUse"),
        
        // put under /admin
        LOGIN("/siteAdmin"),
        LOGOUT("/login_process.jsp"),
        SITE_ADMIN("/siteAdmin");
        
        private final String path;
        
        Routes(String path) {
            this.path = path;
        }
        
        public String path() {
            return path;
        }

        public String url() {
            return getUrl(path);
        }
        
        public String url(Params params) {
            return getUrl(path, params);
        }
        
        public String toString() {
            return path();
        }        
    }
    
    public UrlBuilder(Portal portal) {
        this.portal = portal;
    }
    
    public String getHomeUrl() {
        String rootBreadCrumbUrl = portal.getRootBreadCrumbURL();
        return StringUtils.isEmpty(rootBreadCrumbUrl) ? contextPath : rootBreadCrumbUrl;  
    }
    
    public String getLogoutUrl() {
        return getPortalUrl(Routes.LOGOUT.url(), new Params("loginSubmitMode", "logout"));
    }
    
    public Params getPortalParam() {
        return new Params("home", "" + portal.getPortalId());    
    }

    public String getPortalUrl(String path) {
        return isMultiPortal ? getUrl(path, getPortalParam()) : getUrl(path);
    }
    
    public String getPortalUrl(String path, Params params) {
        if (isMultiPortal) {
            params.putAll(getPortalParam());
        }
        return getUrl(path, params);
    }
    
    public String getPortalUrl(Routes route) {
        return getPortalUrl(route.path());
    }
    
    public String getPortalUrl(Routes route, Params params) {
        return getPortalUrl(route.path(), params);
    }
    
    public static class Params extends HashMap<String, String> { 
        private static final long serialVersionUID = 1L;
        
        public Params() { }
        
        public Params(String...strings) {
            this();
            int stringCount = strings.length;
            for (int i = 0; i < stringCount; i=i+2) {
                // Skip the last item if there's an odd number
                if (i == stringCount-1) { break; }
                this.put(strings[i], strings[i+1]);
            }
        }       
    }
    
    /********** Static utility methods **********/
    
    public static String getUrl(String path) {
        if ( ! path.startsWith("/") ) {
            path = "/" + path;
        }
        return contextPath + path;
    }
    
    public static String getUrl(String path, Params params) {
        
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
