/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vedit.forwarder.impl;

import java.io.IOException;

import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlForwarder implements PageForwarder {
    private static final Log log = LogFactory.getLog(UrlForwarder.class.getName());

    private String theUrl = null;

    public UrlForwarder (String theUrl) {
        this.theUrl = theUrl;
    }


    public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo) {
        try {
            response.sendRedirect(response.encodeRedirectURL(theUrl));
        } catch (IOException ioe) {
            log.error("doForward() could not send redirect.");
        }
    }

}
