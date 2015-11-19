/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import org.apache.commons.lang.StringEscapeUtils;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;

public class LinkTemplateModel extends BaseTemplateModel {
    
    private String url;
    private String text;
    
    public LinkTemplateModel() { }
    
    public LinkTemplateModel(String text, String path) {
        setText(text);
        setUrl(path);
    }
    
    public LinkTemplateModel(String text, String path, String...params) {
        setText(text);
        setUrl(path, params);
    }
    
    public LinkTemplateModel(String text, String path, ParamMap params) {
        setText(text);
        setUrl(path, params);
    }

    protected void setUrl(String path) {
        url = UrlBuilder.getUrl(path);
    }
    
    protected void setUrl(String path, String... params) {
        url = UrlBuilder.getUrl(path, params);
    }
    
    protected void setUrl(String path, ParamMap params) {
        url = UrlBuilder.getUrl(path, params);
    }

    protected void setText(String text) {
        this.text = StringEscapeUtils.escapeHtml(text);
    }
    
    /* Template properties */
    
    public String getUrl() {
        return cleanTextForDisplay( url );
    }    

    public String getText() {
        return cleanTextForDisplay(text);
    }
}
