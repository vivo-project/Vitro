/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public interface ResponseValues {

    public String getTemplateName();

    public int getStatusCode();

    public void setStatusCode(int statusCode);

    public Map<String, Object> getMap();
    
    public String getRedirectUrl();

    public Map<String, String> getHeader();
    
    public String getForwardUrl();

    public Throwable getException();
    
    public ContentType getContentType();
    
    public void setContentType(ContentType contentType);
    
    public Model getModel();

}
