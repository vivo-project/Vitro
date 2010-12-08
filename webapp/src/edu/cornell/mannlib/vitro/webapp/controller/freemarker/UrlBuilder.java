/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

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
        AUTHENTICATE("/authenticate"),
        BROWSE("/browse"),
        CONTACT("/contact"),
        INDIVIDUAL("/individual"),
        INDIVIDUAL_EDIT("/entityEdit"),
        INDIVIDUAL_LIST("/individuallist"),
        LOGIN("/login"), 
        LOGOUT("/logout"),
        SEARCH("/search"),
        SITE_ADMIN("/siteAdmin"),
        TERMS_OF_USE("/termsOfUse"),
        VISUALIZATION("/visualization");

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
        
        public String url(ParamMap params) {
            return getUrl(path, params);
        }
        
        public String toString() {
            return path();
        }        
    }
    
    public enum Css {
        CUSTOM_FORM("/edit/forms/css/customForm.css"),
        JQUERY_UI("/js/jquery-ui/css/smoothness/jquery-ui-1.8.4.custom.css");        

        private final String path;
        
        Css(String path) {
            this.path = path;           
        }
        
        public String path() {
            return path;
        }
        
        public String toString() {
            return path;
        }
    }
    
    public enum JavaScript {
        CUSTOM_FORM_UTILS("/js/customFormUtils.js"),
        JQUERY("/js/jquery.js"),
        JQUERY_UI("/js/jquery-ui/js/jquery-ui-1.8.4.custom.min.js"),
        UTILS("/js/utils.js");
        
        private final String path;
        
        JavaScript(String path) {
            this.path = path;           
        }
        
        public String path() {
            return path;
        }
        
        public String toString() {
            return path;
        }
    }
    
    public UrlBuilder(Portal portal) {
        this.portal = portal;
    }
    
    public int getPortalId() {
        return portal.getPortalId();
    }
    
    public String getHomeUrl() {
        String rootBreadCrumbUrl = portal.getRootBreadCrumbURL();
        String path = StringUtils.isEmpty(rootBreadCrumbUrl) ? "" : rootBreadCrumbUrl;
        return getUrl(path);
    }
    
    // Used by templates to build urls.
    public String getBaseUrl() {
        return contextPath;
    }
    
	public String getLoginUrl() {
		return getPortalUrl(Route.AUTHENTICATE, "return", "true");
	}
    
    public String getLogoutUrl() {
        return getPortalUrl(Route.LOGOUT);
    }
    
    public ParamMap getPortalParam() {
        return new ParamMap("home", "" + portal.getPortalId());    
    }

    public String getPortalUrl(String path) {
        return addPortalParam ? getUrl(path, getPortalParam()) : getUrl(path);
    }
    
    public String getPortalUrl(String path, ParamMap params) {
        if (addPortalParam) {
            params.putAll(getPortalParam());
        }
        return getUrl(path, params);
    }

    public String getPortalUrl(String path, String...params) {
        ParamMap urlParams = new ParamMap(params);
        return getPortalUrl(path, urlParams);
    }
    
    public String getPortalUrl(Route route) {
        return getPortalUrl(route.path());
    }
    
    public String getPortalUrl(Route route, ParamMap params) {
        return getPortalUrl(route.path(), params);
    }

    public String getPortalUrl(Route route, String...params) {
        return getPortalUrl(route.path(), params);
    }
    
    public static class ParamMap extends HashMap<String, String> { 
        private static final long serialVersionUID = 1L;
        
        public ParamMap() { }
        
        public ParamMap(String...strings) {
            int stringCount = strings.length;
            for (int i = 0; i < stringCount; i=i+2) {
                // Skip the last item if there's an odd number
                if (i == stringCount-1) { break; }
                // Skip a param with a null value
                if (strings[i+1] == null) { continue; }
                this.put(strings[i], strings[i+1]);
            }
        } 
        
        public ParamMap(Map<String, String> map) {
            putAll(map);
        }
        
        public void put(String key, int value) {
            put(key, String.valueOf(value));
        }
        
        public void put(String key, boolean value) {
            put(key, String.valueOf(value));
        }
        
    }
    
    /********** Static utility methods **********/
    
    public static String getUrl(String path) {
    	
        if ( !path.isEmpty() && !path.startsWith("/") ) {
            path = "/" + path;
        }
        path = contextPath + path;
        return path.isEmpty() ? "/" : path;
    }
    
    public static String getUrl(Route route) {
        return getUrl(route.path());
    }
    
    public static String getUrl(String path, String...params) {
        ParamMap urlParams = new ParamMap(params);
        return getUrl(path, urlParams);
    }
    
    public static String getUrl(Route route, String...params) {
        return getUrl(route.path(), params);
    }
    
    public static String getUrl(String path, ParamMap params) {
        path = getPath(path, params);
        return getUrl(path);       
    }

    public static String getUrl(Route route, ParamMap params) {
        return getUrl(route.path(), params);
    }
    
    public static String getPath(String path, ParamMap params) {
        String glue = "?";
        for (String key : params.keySet()) {
            path += glue + key + "=" + urlEncode(params.get(key));
            glue = "&";
        }
        return path;       
    }
    
    public static String getPath(Route route, ParamMap params) {
        return getPath(route.path(), params);
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
