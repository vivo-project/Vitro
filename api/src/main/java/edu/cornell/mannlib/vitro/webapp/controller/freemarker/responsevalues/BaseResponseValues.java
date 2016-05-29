/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public abstract class BaseResponseValues implements ResponseValues {

    private int statusCode = 0;
    private ContentType contentType = null;
    
    BaseResponseValues() { }
    
    BaseResponseValues(int statusCode) {
        this.statusCode = statusCode;
    }

    BaseResponseValues(ContentType contentType) {
        this.contentType = contentType;
    }

    BaseResponseValues(ContentType contentType, int statusCode) {
        this.contentType = contentType;
        this.statusCode = statusCode;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getTemplateName() {
        throw new UnsupportedOperationException(
                "This is not a template response.");
    }

    @Override
    public Map<String, Object> getMap() {
        throw new UnsupportedOperationException(
                "This is not a template response.");
    }

    @Override
    public String getRedirectUrl() {
        throw new UnsupportedOperationException(
                "This is not a redirect response.");
    }
    
    @Override
    public Map<String, String> getHeader() {
        throw new UnsupportedOperationException(
                "This is not a header response.");
    }
    
    @Override
    public String getForwardUrl() {
        throw new UnsupportedOperationException(
                "This is not a forwarding response.");
    }
    
    @Override
    public Throwable getException() {
        throw new UnsupportedOperationException(
                "This is not an exception response.");
    }

    @Override
    public Model getModel() {
        throw new UnsupportedOperationException(
                "This is not an RDF response.");
    }
}
