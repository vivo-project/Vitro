 /* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

public class RedirectResponseValues extends BaseResponseValues {

    private final String redirectUrl;

    public RedirectResponseValues(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public RedirectResponseValues(String redirectUrl, int statusCode) {
        super(statusCode);
        this.redirectUrl = redirectUrl;
    }
    
    @Override
    public String getRedirectUrl() {
        return this.redirectUrl;
    }
}

