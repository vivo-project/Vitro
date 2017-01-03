/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class UrlBuilder {

    private static final Log log = LogFactory.getLog(UrlBuilder.class.getName());

    protected static String contextPath = null;   
        
    public enum Route {
        ABOUT("/about"),
        AUTHENTICATE("/authenticate"),
        BROWSE("/browse"),
        CONTACT("/contact"),
        DATA_PROPERTY_EDIT("/datapropEdit"),
        DISPLAY("/display"),
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
        VISUALIZATION_DATA("/visualizationData"),
        EDIT_REQUEST_DISPATCH("/editRequestDispatch");

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
        JQUERY_UI("/js/jquery-ui/css/smoothness/jquery-ui-1.12.1.css");

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
        JQUERY("/js/jquery-1.12.4.min.js"),
        JQUERY_MIGRATE("/js/jquery-migrate-1.4.1.js"),
        JQUERY_UI("/js/jquery-ui/js/jquery-ui-1.12.1.min.js"),
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
    
    private UrlBuilder() { }
  
    public static String getHomeUrl() {
    	return getUrl("");
    }
    
    // Used by templates to build urls.
    public static String getBaseUrl() {
        return contextPath;
    }
    
	public static String getLoginUrl() {
		return getUrl(Route.AUTHENTICATE, "return", "true");
	}
    
    public static String getLogoutUrl() {
        return getUrl(Route.LOGOUT);
    }
    
    public static class ParamMap extends LinkedHashMap<String, String> { 
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

    //TODO: document this as it is used all over the app
    //does this append the context?  What if params is null? 
    //What if you want a route that isn't in Route?
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
    
    public static String getIndividualProfileUrl(Individual individual, VitroRequest vreq) {
        WebappDaoFactory wadf = vreq.getWebappDaoFactory();
        String profileUrl = null;
        try {
            String localName = individual.getLocalName();
            String namespace = individual.getNamespace();
            String defaultNamespace = wadf.getDefaultNamespace();                
                    
            if (defaultNamespace.equals(namespace)) {
                profileUrl = getUrl(Route.DISPLAY.path() + "/" + localName);
            } else {
                if (wadf.getApplicationDao().isExternallyLinkedNamespace(namespace)) {
                    log.debug("Found externally linked namespace " + namespace);
                    profileUrl = namespace + localName;
                } else {
                    ParamMap params = new ParamMap("uri", individual.getURI());
                    profileUrl = getUrl(Route.INDIVIDUAL.path(), params);
                }
            }
        } catch (Exception e) {
            log.warn(e);
            return null;
        }        

        if (profileUrl != null) {
            LinkedHashMap<String, String> specialParams = getModelParams(vreq);
            if(specialParams.size() != 0) {
                profileUrl = addParams(profileUrl, new ParamMap(specialParams));
            }
        }
        
        return profileUrl;
    }

    /**
     * If you already have an Individual object around, 
     * call getIndividualProfileUrl(Individual, VitroRequest) 
     * instead of this method. 
     */
    public static String getIndividualProfileUrl(String individualUri, VitroRequest vreq) {        
        return getIndividualProfileUrl(new IndividualImpl(individualUri),  vreq);
    }    
    
    public static boolean isUriInDefaultNamespace(String individualUri, VitroRequest vreq) {
        return isUriInDefaultNamespace(individualUri, vreq.getWebappDaoFactory());
    }
    
    public static boolean isUriInDefaultNamespace(String individualUri, WebappDaoFactory wadf) {
        return isUriInDefaultNamespace( individualUri, wadf.getDefaultNamespace());
    }
    
    public static boolean isUriInDefaultNamespace(String individualUri, String defaultNamespace){
        try {
            Individual ind = new IndividualImpl(individualUri); 
            String namespace = ind.getNamespace();          
            return defaultNamespace.equals(namespace);
        } catch (Exception e) {
            log.warn(e);
            return false;
        }
    }
    
    public static String urlEncode(String str) {
        String encoding = "UTF-8";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding url " + str + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return encodedUrl;
    }

    public static String urlDecode(String str) {
        String encoding = "UTF-8";
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error decoding url " + str + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return decodedUrl;
    }
    
    //To be used in different property templates so placing method for reuse here
    //Check if special params included, specifically for menu management and other models
    public static LinkedHashMap<String,String> getModelParams(VitroRequest vreq) {
    	
    	LinkedHashMap<String,String> specialParams = new LinkedHashMap<String, String>();
    	if(vreq != null) {
    		//this parameter is sufficient to switch to menu model
    		String useMenuModelParam = vreq.getParameter(DisplayVocabulary.SWITCH_TO_DISPLAY_MODEL);
    		//the parameters below allow for using a different model
	    	String useMainModelUri = vreq.getParameter(DisplayVocabulary.USE_MODEL_PARAM);
	    	String useTboxModelUri = vreq.getParameter(DisplayVocabulary.USE_TBOX_MODEL_PARAM);
	    	String useDisplayModelUri = vreq.getParameter(DisplayVocabulary.USE_DISPLAY_MODEL_PARAM);
	    	if(useMenuModelParam != null && !useMenuModelParam.isEmpty()) {
	    		specialParams.put(DisplayVocabulary.SWITCH_TO_DISPLAY_MODEL, useMenuModelParam);
	    	}
	    	else if(useMainModelUri != null && !useMainModelUri.isEmpty()) {
	    		specialParams.put(DisplayVocabulary.USE_MODEL_PARAM, useMainModelUri);
	    		if(useTboxModelUri != null && !useTboxModelUri.isEmpty()){ 
	    			specialParams.put(DisplayVocabulary.USE_TBOX_MODEL_PARAM, useTboxModelUri);
	    		}
	    		if(useDisplayModelUri != null && !useDisplayModelUri.isEmpty()) {
	    			specialParams.put(DisplayVocabulary.USE_DISPLAY_MODEL_PARAM, useDisplayModelUri);
	    		}
	    	}
    	}
    	return specialParams;
    }

}
