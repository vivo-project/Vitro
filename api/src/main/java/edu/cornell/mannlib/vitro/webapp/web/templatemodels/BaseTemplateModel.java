/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.web.AntiScript;

public abstract class BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseTemplateModel.class);

	private static final String URI_CHARACTERS = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&%'()*+,;=";
    
    // Convenience method so subclasses can call getUrl(path)
    protected String getUrl(String path) {
        return UrlBuilder.getUrl(path);
    }

    // Convenience method so subclasses can call getUrl(path, params)
    protected String getUrl(String path, ParamMap params) {
        return UrlBuilder.getUrl(path, params);
    }
    
    // Convenience method so subclasses can call getUrl(path, params)
    protected String getUrl(String path, String... params) {
        return UrlBuilder.getUrl(path, params);
    }

    /**
     * Used to do any processing for display of URIs or URLs.  
     * 
     * If we used AntiSami on a URI it would escape any ampersands as &amp;
     * and perhaps do other nastiness as well. Instead we delete any character 
     * that shouldn't be in a URI.
     */
    protected String cleanURIForDisplay( String dirty ){
        if( dirty == null )
            return null;
        
    	StringBuilder clean = new StringBuilder(dirty.length());
    	for (char ch: dirty.toCharArray()) {
    		if (URI_CHARACTERS.indexOf(ch) != -1) {
    			clean.append(ch);
    		}
    	}
        return clean.toString();
    }
    
    /**
     * Used to do any processing for display of general text.  
     * Currently this only checks for XSS exploits.
     */
    protected String cleanTextForDisplay( String dirty){
        return simpleScriptStripper(dirty);
    }
    
    /**
     * Used to do any processing for display of values in
     * a map.  Map may be modified. 
     */
    protected <T> void cleanMapValuesForDisplay( Map<T,String> map){
        for( T key : map.keySet() ){
            map.put(key, simpleScriptStripper(map.get(key)) );
        }
    }

//    private static final Pattern stripScriptTags   = Pattern.compile("<script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>");
//    private static final Pattern stripOnAttributes = Pattern.compile(" on[a-z]*=\"(?:[^\\\\\"]|\\\\.)*\"");
    private static final Pattern stripJavascript   = Pattern.compile("(<script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>)|( on[a-z]*=\"(?:[^\\\\\"]|\\\\.)*\")");

    protected String simpleScriptStripper(String dirtyInput) {
        if( dirtyInput == null )
            return null;

        if (dirtyInput.contains("<script") || dirtyInput.contains(" on")) {
            return stripJavascript.matcher(dirtyInput).replaceAll("");
        }

        return dirtyInput;
    }
}
