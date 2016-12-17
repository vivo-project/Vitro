/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

/**
 * Allow the user to dump the knowledge base from either RDFService, or restore
 * it.
 * 
 * Show the user the selection page. If they select "dump" parameters, redirect
 * to an appropriate filename-based URL, so they will receive a nicely named
 * file. If they chose to "restore", just do it.
 * 
 * The first request, the selection and the redirected dump should all be GET
 * requests. A restore should be a POST request.
 */
public class DumpRestoreController extends FreemarkerHttpServlet {

	private static final RequestedAction REQUIRED_ACTION = SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION;

	static final String ACTION_DUMP = "/dump";
	static final String ACTION_RESTORE = "/restore";
	static final String ACTION_SELECT = "/select";
	static final String PARAMETER_WHICH = "which";
	static final String PARAMETER_SOURCE_FILE = "sourceFile";
	static final String PARAMETER_PURGE = "purge";
	static final String ATTRIBUTE_TRIPLE_COUNT = "tripleCount";

	private static final String TEMPLATE_NAME = "datatools-dumpRestore.ftl";

	/**
	 * Override this to change the maximum size of uploaded files in multipart
	 * requests.
	 */
	@Override
	public long maximumMultipartFileSize() {
		long gigabyte = 1024L * 1024L * 1024L;
		return 100L * gigabyte; // permit really big uploads.
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		if (!isAuthorizedToDisplayPage(req, resp, REQUIRED_ACTION)) {
			return;
		}

		try {
			String action = req.getPathInfo();
			if (ACTION_SELECT.equals(action)) {
				new DumpModelsAction(req, resp).redirectToFilename();
			} else if (StringUtils.startsWith(action, ACTION_DUMP)) {
				new DumpModelsAction(req, resp).dumpModels();
			} else {
				super.doGet(req, resp);
			}
		} catch (BadRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		if (!PolicyHelper.isAuthorizedForActions(req, REQUIRED_ACTION)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		}

		try {
			if (ACTION_RESTORE.equals(req.getPathInfo())) {
				long tripleCount = new RestoreModelsAction(req, resp)
						.restoreModels();
				req.setAttribute(ATTRIBUTE_TRIPLE_COUNT, tripleCount);
				super.doGet(req, resp);
			} else {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		} catch (BadRequestException | RDFServiceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
		Map<String, Object> bodyMap = new HashMap<>();

		bodyMap.put("selectUrl",
				UrlBuilder.getUrl(vreq.getServletPath() + ACTION_SELECT));
		bodyMap.put("restoreUrl",
				UrlBuilder.getUrl(vreq.getServletPath() + ACTION_RESTORE));

		Object tripleCount = vreq.getAttribute(ATTRIBUTE_TRIPLE_COUNT);
		if (tripleCount instanceof Long) {
			bodyMap.put("tripleCount", tripleCount);
		}

		return new TemplateResponseValues(TEMPLATE_NAME, bodyMap);
	}

	/**
	 * Indicates a problem with the request parameters.
	 */
	static class BadRequestException extends Exception {
		public BadRequestException(String message) {
			super(message);
		}
	}
}
