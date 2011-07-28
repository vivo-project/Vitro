/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

/**
 * This is a class to provide methods to strip bad HTML from user input.
 * The primary goal of this is to avoid XSS attacks.  
 */
public class AntiScript {

    private static final Log log = LogFactory.getLog(AntiScript.class);
    
    
    private static final String ANTI_SCRIPT_POLICY = "ANTI_SCRIPT_POLICY";
    private static final String ANTI_SCRIPT_SCANNER = "ANTI_SCRIPT_SCANNER";
    private static String ANTI_SCRIPT_POLICY_FILE = "/WEB-INF/classes/edu/cornell/mannlib/vitro/webapp/web/antisamy-vitro-1.4.4.xml";

    /**
     * This will attempt to return HTML that has been cleaned up according
     * to the policy.  
     * 
     * If there is any error during the scan, an error message
     * will be returned instead of the HTML.  This might not be ideal so
     * consider changing it once we see how this works. Other options include
     * returning an empty string or some other error message.  Returning 
     * the un-scanned HTML is not a secure option as it may contain scripts.
     * 
     * This will return null if dirtyInput is null.
     */
    public static String cleanText( String dirtyInput, ServletContext context){
        if( dirtyInput == null )
            return null;
        
        AntiSamy as = getHTMLScanner(context);        
        CleanResults cr;
        try {
            cr = as.scan(dirtyInput);
            return cr.getCleanHTML();
        } catch (ScanException e) {
            log.error("Error while scaning HTML" ,e );
        } catch (PolicyException e) {
            log.error("Error while scanning HTML", e);
        }        
        return "AntiScript: HTML caused scan error.";
    }
    
    /**
     * Method to clean a URL or URI.  
     */
    public static String cleanURI( String dirtyInput, ServletContext context){
        return cleanText(dirtyInput,context);
    }
    
    /**
     * Method to clean all of the values in a map where the values are of
     * type String.
     */
    public static <T> void cleanMapValues( Map<T,String> map, ServletContext context){
        for( T key : map.keySet() ){            
            map.put(key, cleanText(map.get(key), context));
        }        
    }
    
    /**
     * Try to get the policy from the servlet context, if none exists, create a new one.
     * This is a anti-script policy for use with OWASP AntiSamy, not a vivo auth Policy.
     * Returns null if no policy can be created.
     */
    protected static Policy getAntiScriptPolicy(ServletContext context){
        Object obj = context.getAttribute( ANTI_SCRIPT_POLICY );
        if( obj == null ){
            Policy newPolicy;
            try {
                String url = ANTI_SCRIPT_POLICY_FILE;
                URL policyFile= context.getResource( url );                                                             
                newPolicy = Policy.getInstance( policyFile );
                context.setAttribute(ANTI_SCRIPT_POLICY, newPolicy);
                log.debug("anti-script policy loaded successfully");
                return newPolicy;
            } catch (PolicyException e) {
                log.error("Anti-Script policy not setup.", e);
                return null;
            } catch (MalformedURLException e) {
                log.error("Anti-Script policy not setup.", e);
                return null;
            }            
        } else {
            return (Policy)obj;
        }                       
    }
    
    /**
     * Try to get an AntiSamy HTML scanner object that is sharied across
     * the whole web application. This may return a scanner with a null
     * policy if the policy is not setup correctly.
     */
    protected static AntiSamy getHTMLScanner( ServletContext context){
        Object obj = context.getAttribute( ANTI_SCRIPT_SCANNER );
        if( obj == null ){
            AntiSamy scanner = new AntiSamy( getAntiScriptPolicy(context));
            context.setAttribute( ANTI_SCRIPT_SCANNER , scanner);
            log.debug("anti-script scanner loaded successfully");
            return scanner;            
        }else{
            return (AntiSamy) obj;
        }                
    }     
}
