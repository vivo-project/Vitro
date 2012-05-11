/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * HttpServiceFacade uses this if the registered servlet or registered resource
 * doesn't provide one of its own.
 * 
 * The spec requires this implementation of getResource(), which is
 * bundle-dependent, so each bundle gets its own default instance of
 * HttpContext.
 */
class DefaultHttpContext implements HttpContext {
	private static final Log log = LogFactory.getLog(DefaultHttpContext.class);

	private final Bundle bundle;

	public DefaultHttpContext(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		log.debug("handleSecurity permits any request.");
		return true;
	}

	@Override
	public URL getResource(String name) {
		URL url = bundle.getResource(name);
		log.debug("getResource, name=" + name + ", url=" + url);
		return url;
	}

	@Override
	public String getMimeType(String name) {
		log.debug("Get mime type, name=" + name);
		/*
		 * If this method returns null, the HttpService is free to determine the
		 * MIME type as it wishes.
		 */
		return null;
	}

}