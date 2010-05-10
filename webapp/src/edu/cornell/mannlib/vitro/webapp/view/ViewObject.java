/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public abstract class ViewObject {

    private static final Log log = LogFactory.getLog(ViewObject.class.getName());

    public static String contextPath;
    
    protected static String getUrl(String path) {
        return FreeMarkerHttpServlet.getUrl(path);
    }
    
    protected String encodeUrl(String url) {
        String encoding = "ISO-8859-1";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(url, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding url " + url + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return encodedUrl;
    }

}
