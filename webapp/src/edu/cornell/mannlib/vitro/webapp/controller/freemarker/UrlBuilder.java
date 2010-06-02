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

    protected static String contextPath = null;   
    private static boolean addPortalParam = PortalPickerFilter.isPortalPickingActive();
    
    private Portal portal;
        
    public enum Route {
        ABOUT("/about"),
        BROWSE("/browse"),
        CONTACT("/contact"),
        INDIVIDUAL("/individual"),
        INDIVIDUAL_LIST("/entitylist"), // entitylist individuallist
        SEARCH("/search"),
        TERMS_OF_USE("/termsOfUse"),
        
        // RY put these under /admin/
        LOGIN("/siteAdmin"),
        LOGOUT("/login_process.jsp"),
        SITE_ADMIN("/siteAdmin");
        
        private final String path;
        
        Route(String path) {
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
        return getPortalUrl(Route.LOGOUT.url(), new Params("loginSubmitMode", "logout"));
    }
    
    public Params getPortalParam() {
        return new Params("home", "" + portal.getPortalId());    
    }

    public String getPortalUrl(String path) {
        return addPortalParam ? getUrl(path, getPortalParam()) : getUrl(path);
    }
    
    public String getPortalUrl(String path, Params params) {
        if (addPortalParam) {
            params.putAll(getPortalParam());
        }
        return getUrl(path, params);
    }
    
    public String getPortalUrl(Route route) {
        return getPortalUrl(route.path());
    }
    
    public String getPortalUrl(Route route, Params params) {
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
        path = getPath(path, params);
        return getUrl(path);       
    }
    
    public static String getPath(String path, Params params) {
        String glue = "?";
        for (String key : params.keySet()) {
            path += glue + key + "=" + urlEncode(params.get(key));
            glue = "&";
        }
        return path;       
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
