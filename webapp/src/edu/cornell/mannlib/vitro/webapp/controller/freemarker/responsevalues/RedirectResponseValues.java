 /* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;

public class RedirectResponseValues extends BaseResponseValues {

    private final String redirectUrl;

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
    
    private String getRedirectUrl(String redirectUrl) {
        return redirectUrl.contains("://") ? redirectUrl : UrlBuilder.getUrl(redirectUrl);
    }

}

