/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;


/**
 * This could be called the "Redirect to where I say, damm it" ResponseValue.
 * 
 * It redirects to the URL specified.  It does not attempt to add a 
 * context node.  This is useful when you want to redirect to a URL
 * created by the UrlBuilder which uses statics to sneak a context
 * into the URL strings it creates.    
 */
public class DirectRedirectResponseValues extends RedirectResponseValues {

    /** This will redirect to the url. It will not add the context to the url.*/
    public DirectRedirectResponseValues(String url, int statusCode) {
        super(url, statusCode);        
    }
    
    /** This will redirect to the url. It will not add the context to the url.*/
    public DirectRedirectResponseValues(String url){
        super(url);
    }

    /**
     * Does not add context.
     */
    @Override
    protected String getRedirectUrl(String url) {
        return url;
    }        
}
