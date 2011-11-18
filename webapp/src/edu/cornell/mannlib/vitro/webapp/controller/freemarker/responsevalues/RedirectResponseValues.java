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

    //TODO: document this.  What does this do and mean?
    //should redirectUrl have the context?  Or is the context added?
    //If the context is added, what if we already have it because 
    //UrlBuilder was used?  
    //what about an off site redirect?  Maybe check for a magic "://" ?    
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

