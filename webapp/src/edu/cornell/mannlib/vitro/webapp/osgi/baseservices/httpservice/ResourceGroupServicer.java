/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * This is how a registered resource group will process an HTTP request.
 */
public class ResourceGroupServicer extends Servicer {
	private static final Log log = LogFactory
			.getLog(ResourceGroupServicer.class);

	private final ServletContext servletContext;
	private final String internalName;

	public ResourceGroupServicer(String alias, Bundle bundle,
			HttpContext httpContext, ServletContext servletContext,
			String internalName) {
		super(alias, bundle, httpContext);
		this.servletContext = servletContext;
		this.internalName = internalName;
	}

	public String getInternalName() {
		return internalName;
	}

	/**
	 * When a resource group is registered, there is nothing to setup.
	 */
	@Override
	public void init() {
		// nothing to do.
	}

	/**
	 * When a resource group is unregistered, there is nothing to clean up.
	 */
	@Override
	public void dispose() {
		// nothing to do.
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String resourcePath = resolveResourcePath(req);
		log.debug("resolved resource path: '" + resourcePath + "'");

		URL url = getHttpContext().getResource(resourcePath);

		if (url == null) {
			send404(resp, resourcePath);
		} else {
			sendResourceContent(resp, resourcePath, url);
		}
	}

	private void send404(HttpServletResponse resp, String resourcePath)
			throws IOException {
		log.debug("can't find resource for '" + resourcePath + "'");
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void sendResourceContent(HttpServletResponse resp,
			String resourcePath, URL url) throws IOException {
		String mimeType = figureMimeType(resourcePath);
		log.debug("sending content for '" + resourcePath + "', mime type is '"
				+ mimeType + "'");

		resp.setContentType(mimeType);

		InputStream in = url.openStream();
		OutputStream out = resp.getOutputStream();
		byte[] buffer = new byte[8192];
		int howManyBytes;
		while (-1 != (howManyBytes = in.read(buffer))) {
			out.write(buffer, 0, howManyBytes);
		}
	}

	/**
	 * What path will we use to request the resource from the HttpContext that
	 * was registered with this resource group?
	 * 
	 * Use the alias and the internal name that were registered with this
	 * resource group. Also use the URI from the request. Remove the alias from
	 * the beginning of the URI, replacing it with the internal name.
	 * 
	 * The URI is formed from the servletPath and the pathInfo from the request.
	 * We can't simply use the requestUri from the request, since that includes
	 * the contextPath.
	 * 
	 * The alias of the resource group might match only a portion of the URI, if
	 * a more specific match was not found.
	 * 
	 * During this process, if either the alias or the internal name is "/", it
	 * is treated like the empty string.
	 * 
	 * Ref: Http Service Specification, Version 1.2, paragraph 102.4.
	 */
	private String resolveResourcePath(HttpServletRequest req) {
		String effectiveAlias = getAlias();
		if (effectiveAlias.equals("/")) {
			effectiveAlias = "";
		}

		String effectiveInternalName = getInternalName();
		if (effectiveInternalName.equals("/")) {
			effectiveInternalName = "";
		}

		String uri = figureRequestUri(req);

		return effectiveInternalName + uri.substring(effectiveAlias.length());
	}

	private String figureMimeType(String resourcePath) {
		String mimeType = getHttpContext().getMimeType(resourcePath);
		if (mimeType == null) {
			mimeType = servletContext.getMimeType(resourcePath);
		}
		return mimeType;
	}

	@Override
	public String toString() {
		return "ResourceGroupServicer[alias" + getAlias() + ", bundle="
				+ formatBundle() + ", internalName=" + internalName
				+ ", httpContext=" + getHttpContext() + "]";
	}
}
