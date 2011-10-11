/* $This file is dactionistrietupbuted under the terms of theare license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageSearchIndex;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevelStamp;

/**
 * Accepts requests to rebuild or update the search index. It uses an
 * IndexBuilder and finds that IndexBuilder from the servletContext using the
 * key "edu.cornel.mannlib.vitro.search.indexing.IndexBuilder"
 * 
 * That IndexBuilder will be associated with a object that implements the
 * IndexerIface.
 * 
 * An example of the IndexerIface is SolrIndexer. An example of the IndexBuilder
 * and SolrIndexer setup is in SolrSetup.
 * 
 * @author bdc34
 */
public class IndexController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory.getLog(IndexController.class);

	/**
	 * <pre>
	 * This request might be:
	 * SETUP -- Index is not building and nothing is requested. Solicit requests.
	 * REFRESH  -- Index is building, nothing is requested. Show continuing status.
	 * REBUILD -- Rebuild is requested. Set the rebuild flag and show continuing status.
	 * UPDATE -- Update is requested. Set the update flag and show continuing status.
	 * </pre>
	 */
	private enum RequestType {
		SETUP, REFRESH, REBUILD, UPDATE;

		/** What type of request is this? */
		static RequestType fromRequest(HttpServletRequest req) {
			if (hasParameter(req, "rebuild")) {
				return REBUILD;
			} else if (hasParameter(req, "update")) {
				return UPDATE;
			} else {
				ServletContext ctx = req.getSession().getServletContext();
				IndexBuilder builder = IndexBuilder.getBuilder(ctx);
				WorkLevelStamp workLevel = builder.getWorkLevel();
				if (workLevel.getLevel() == WorkLevel.WORKING) {
					return REFRESH;
				} else {
					return SETUP;
				}
			}
		}

		private static boolean hasParameter(HttpServletRequest req, String key) {
			String value = req.getParameter(key);
			return (value != null) && (!value.isEmpty());
		}
	}

	private static final String PAGE_URL = "/SearchIndex";
	private static final String TEMPLATE_NAME = "searchIndex.ftl";

	public static final Actions REQUIRED_ACTIONS = new Actions(
			new ManageSearchIndex());

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return REQUIRED_ACTIONS;
	}

	@Override
	protected String getTitle(String siteName, VitroRequest vreq) {
		return "Search Index Update or Rebuild";
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		if (RequestType.fromRequest(req) == RequestType.REFRESH) {
			resp.addHeader("Refresh", "5; " + UrlBuilder.getUrl(PAGE_URL));
		}
		super.doGet(req, resp);
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("actionUrl", UrlBuilder.getUrl(PAGE_URL));

		try {
			IndexBuilder builder = IndexBuilder.getBuilder(getServletContext());

			switch (RequestType.fromRequest(vreq)) {
			case REBUILD:
				builder.doIndexRebuild();
				return redirectToRefresh(body);
			case UPDATE:
				builder.doUpdateIndex();
				return redirectToRefresh(body);
			default:
				return showCurrentStatus(builder, body);
			}
		} catch (Exception e) {
			log.error("Error rebuilding search index", e);
			body.put("errorMessage",
					"There was an error while rebuilding the search index. "
							+ e.getMessage());
			return new ExceptionResponseValues(
					Template.ERROR_MESSAGE.toString(), body, e);
		}
	}

	private ResponseValues redirectToRefresh(Map<String, Object> body) {
		return new RedirectResponseValues(PAGE_URL);
	}

	private ResponseValues showCurrentStatus(IndexBuilder builder,
			Map<String, Object> body) {
		WorkLevelStamp stamp = builder.getWorkLevel();
		body.put("worklevel", stamp.getLevel().toString());
		body.put("since", stamp.getSince());
		body.put("hasPreviousBuild", stamp.getSince().getTime() > 0L);
		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

}
