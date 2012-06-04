/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Provide the ability to process a Freemarker template outside of the context
 * of a FreemarkerHttpServlet.
 * 
 * The most likely place to use this is when rendering a short view that was
 * invoked through an AJAX call.
 */
public interface FreemarkerProcessingService {
	/**
	 * Is there an accessible template by this name?
	 * 
	 * The question is asked in the context of the current request, which
	 * determines the theme directory.
	 * 
	 * @throws TemplateProcessingException
	 *             If the template is found, but cannot be parsed.
	 */
	boolean isTemplateAvailable(String templateName, HttpServletRequest req)
			throws TemplateProcessingException;

	/**
	 * Process a Freemarker template with a data map, producing string of HTML.
	 * 
	 * This is done in the context of the current HttpServletRequest, which
	 * provides a wide range of ancillary information, including (but not
	 * limited to) theme directory, context path, info on logged-in user,
	 * authorizations for the current user, etc., etc.
	 */
	String renderTemplate(String templateName, Map<String, Object> map,
			HttpServletRequest req) throws TemplateProcessingException;

	/**
	 * Indicates a failure to render the given template with the given data.
	 */
	@SuppressWarnings("serial")
	public static class TemplateProcessingException extends Exception {

		public TemplateProcessingException() {
			super();
		}

		public TemplateProcessingException(String message) {
			super(message);
		}

		public TemplateProcessingException(Throwable cause) {
			super(cause);
		}

		public TemplateProcessingException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * Template parser detected a problem.
	 */
	@SuppressWarnings("serial")
	public static class TemplateParsingException extends
			TemplateProcessingException {

		public TemplateParsingException() {
			super();
		}

		public TemplateParsingException(String message) {
			super(message);
		}

		public TemplateParsingException(Throwable cause) {
			super(cause);
		}

		public TemplateParsingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
