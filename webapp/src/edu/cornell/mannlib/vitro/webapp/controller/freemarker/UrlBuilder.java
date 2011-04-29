/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filters.PortalPickerFilter;

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
        DATA_PROPERTY_EDIT("/datapropEdit"),
        INDIVIDUAL("/individual"),
        INDIVIDUAL_EDIT("/entityEdit"),
        INDIVIDUAL_LIST("/individuallist"),
        LOGIN("/login"), 
        LOGOUT("/logout"),
        OBJECT_PROPERTY_EDIT("/propertyEdit"),
        SEARCH("/search"),
        SITE_ADMIN("/siteAdmin"),
        TERMS_OF_USE("/termsOfUse"),
        VISUALIZATION("/visualization"),
        VISUALIZATION_SHORT("/vis"),
        VISUALIZATION_AJAX("/visualizationAjax"),
        VISUALIZATION_DATA("/visualizationData");

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
        JQUERY_UI("/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css");        

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
        JQUERY_UI("/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"),
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
        
        public ParamMap(List<String> strings) {
            this((String[]) strings.toArray());
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
        
        public void put(ParamMap params) {
            for (String key: params.keySet()) {
                put(key, params.get(key));
            }
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
        return addParams(path, params, "?");      
    }
    
    private static String addParams(String url, ParamMap params, String glue) {
        for (String key: params.keySet()) {
            String value = params.get(key);
            // rjy7 Some users might require nulls to be converted to empty
            // string, others to eliminate params with null values.
            // Here we convert to empty string to prevent an exception
            // from UrlEncoder.encode() when passed a null. Callers are advised
            // to remove null values or convert to empty strings, whichever
            // is desired in the particular instance.
            value = (value == null) ? "" : urlEncode(value);
            url += glue + key + "=" + value;
            glue = "&";
        }
        return url;        
    }
    
    public static String addParams(String url, ParamMap params) {
        String glue = url.contains("?") ? "&" : "?";
        return addParams(url, params, glue);
    }
    
    public static String addParams(String url, String...params) {
        return addParams(url, new ParamMap(params));
    }
    
    public static String addParams(String url, List<String> params) {
        return addParams(url, new ParamMap(params));
    }
    
    public static String getPath(Route route, ParamMap params) {
        return getPath(route.path(), params);
    }
    
    public static String getIndividualProfileUrl(String individualUri, WebappDaoFactory wadf) {
        Individual individual = new IndividualImpl(individualUri);
        return getIndividualProfileUrl(individual, individualUri, wadf);
    }
    
    public static String getIndividualProfileUrl(Individual individual, WebappDaoFactory wadf) {
        String individualUri = individual.getURI();
        return getIndividualProfileUrl(individual, individualUri, wadf);        
    }
    
    private static String getIndividualProfileUrl(Individual individual, String individualUri, WebappDaoFactory wadf) {
        String profileUrl = null;
        try {
            URI uri = new URIImpl(individualUri); // throws exception if individualUri is invalid
            String namespace = uri.getNamespace();
            String defaultNamespace = wadf.getDefaultNamespace();
    
            String localName = individual.getLocalName();
                    
            if (defaultNamespace.equals(namespace)) {
                profileUrl = getUrl(Route.INDIVIDUAL.path() + "/" + localName);
            } else {
                if (wadf.getApplicationDao().isExternallyLinkedNamespace(namespace)) {
                    log.debug("Found externally linked namespace " + namespace);
                    profileUrl = namespace + localName;
                } else {
                    ParamMap params = new ParamMap("uri", individualUri);
                    profileUrl = getUrl("/individual", params);
                }
            }
        } catch (Exception e) {
            log.warn(e);
            return null;
        }
        return profileUrl;        
    }
    
    public static String urlEncode(String str) {
        String encoding = "ISO-8859-1";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding url " + str + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return encodedUrl;
    }

    public static String urlDecode(String str) {
        String encoding = "ISO-8859-1";
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error decoding url " + str + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return decodedUrl;
    }

}
