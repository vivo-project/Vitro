 /* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;

/**
 * Also see DirectRedirectResponseValues
 *
 */
public class RedirectResponseValues extends BaseResponseValues {

    private final String redirectUrl;

    /** 
     * The string redirectUrl will get the context added.    
       If you want a redirect for a URL that has the context already added, 
       as is the case if a UrlBuilder was used. use the class DirectRedirectResponseValues. 
       
       This will attempt to handle an off site redirect by checking for
        "://" in the URL. 
     */
    public RedirectResponseValues(String redirectUrl) {
        this.redirectUrl = getRedirectUrl(redirectUrl);
    }

    public RedirectResponseValues(String redirectUrl, int statusCode) {
        super(statusCode);
        this.redirectUrl = getRedirectUrl(redirectUrl);
    }
    
    public RedirectResponseValues(Route redirectUrl) {
        this.redirectUrl = UrlBuilder.getUrl(redirectUrl);
    }
    
    @Override
    public String getRedirectUrl() {
        return this.redirectUrl;
    }
    
    protected String getRedirectUrl(String redirectUrl) {
        return redirectUrl.contains("://") ? redirectUrl : UrlBuilder.getUrl(redirectUrl);
    }

}

