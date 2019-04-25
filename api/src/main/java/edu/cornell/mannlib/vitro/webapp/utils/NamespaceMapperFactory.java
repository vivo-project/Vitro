/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import javax.servlet.ServletContext;

public class NamespaceMapperFactory {

	public static NamespaceMapper getNamespaceMapper(ServletContext servletContext) {
		return (NamespaceMapper) servletContext.getAttribute("NamespaceMapper");
	}

}
