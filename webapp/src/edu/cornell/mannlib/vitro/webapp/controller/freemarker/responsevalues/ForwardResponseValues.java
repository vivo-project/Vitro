/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;


public class ForwardResponseValues extends BaseResponseValues {
    private final String forwardUrl;

    public ForwardResponseValues(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }

    public ForwardResponseValues(String forwardUrl, int statusCode) {
        super(statusCode);
        this.forwardUrl = forwardUrl;
    }

    @Override
    public String getForwardUrl() {
        return this.forwardUrl;
    }
}
