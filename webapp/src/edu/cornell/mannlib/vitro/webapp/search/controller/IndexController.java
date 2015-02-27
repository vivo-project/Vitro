/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.RebuildCounts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.StatementCounts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.UriCounts;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingServiceSetup;

/**
 * Accepts requests to display the current status of the search index, or to
 * initiate a rebuild.
 * 
 * A DISPLAY or REBUILD request is handled like any other FreemarkerHttpServlet.
 * A STATUS is an AJAX request, we override doGet() so we can format the
 * template without enclosing it in a body template.
 * 
 * When initialized, this servlet adds a listener to the SearchIndexer, so it
 * can maintain a history of activity. This will provide the contents of the
 * display.
 */
public class IndexController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory.getLog(IndexController.class);

	/**
	 * <pre>
	 * This request might be:
	 * DISPLAY (default) -- Send the template that will contain the status display. 
	 * STATUS  -- Send the current status and history.
	 * REBUILD -- Initiate a rebuild. Then act like DISPLAY.
	 * </pre>
	 */
	private enum RequestType {
		DISPLAY, STATUS, REBUILD;

		/** What type of request is this? */
		static RequestType fromRequest(HttpServletRequest req) {
			if (hasParameter(req, "rebuild")) {
				return REBUILD;
			} else if (hasParameter(req, "status")) {
				return STATUS;
			} else {
				return DISPLAY;
			}
		}

		private static boolean hasParameter(HttpServletRequest req, String key) {
			String value = req.getParameter(key);
			return (value != null) && (!value.isEmpty());
		}
	}

	private static final String PAGE_URL = "/SearchIndex";
	private static final String PAGE_TEMPLATE_NAME = "searchIndex.ftl";
	private static final String STATUS_TEMPLATE_NAME = "searchIndexStatus.ftl";

	public static final RequestedAction REQUIRED_ACTIONS = SimplePermission.MANAGE_SEARCH_INDEX.ACTION;

	private SearchIndexer indexer;
	private static IndexHistory history;

	@Override
	public void init() throws ServletException {
		this.indexer = ApplicationUtils.instance().getSearchIndexer();
		super.init();
	}

	/**
	 * Called by SearchIndexerSetup to provide a history that dates from
	 * startup, not just from servlet load time.
	 */
	public static void setHistory(IndexHistory history) {
		IndexController.history = history;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		switch (RequestType.fromRequest(req)) {
		case STATUS:
			showStatus(req, resp);
			break;
		default:
			super.doGet(req, resp);
			break;
		}
	}

	@Override
	protected String getTitle(String siteName, VitroRequest vreq) {
		return "Rebuild Search Index";
	}

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return REQUIRED_ACTIONS;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		switch (RequestType.fromRequest(vreq)) {
		case REBUILD:
			requestRebuild();
			try {
				// Pause, giving a chance to start the task queue.
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Don't care, so pass it along.
				Thread.currentThread().interrupt();
			}
			return new RedirectResponseValues(PAGE_URL);
		default:
			return showDisplay();
		}
	}

	private ResponseValues showDisplay() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("statusUrl", UrlBuilder.getUrl(PAGE_URL, "status", "true"));
		body.put("rebuildUrl", UrlBuilder.getUrl(PAGE_URL, "rebuild", "true"));
		return new TemplateResponseValues(PAGE_TEMPLATE_NAME, body);
	}

	private void showStatus(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (!PolicyHelper.isAuthorizedForActions(req, REQUIRED_ACTIONS)) {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			resp.getWriter().write(
					"You are not authorized to access this page.");
			return;
		}

		try {
			Map<String, Object> body = new HashMap<>();
			body.put("statusUrl", UrlBuilder.getUrl(PAGE_URL, "status", "true"));
			body.put("rebuildUrl",
					UrlBuilder.getUrl(PAGE_URL, "rebuild", "true"));
			body.put("status", buildStatusMap(indexer.getStatus()));
			if (history != null) {
				body.put("history", history.toMaps());
			}

			String rendered = FreemarkerProcessingServiceSetup.getService(
					getServletContext()).renderTemplate(STATUS_TEMPLATE_NAME,
					body, req);
			resp.getWriter().write(rendered);
		} catch (Exception e) {
			resp.setStatus(500);
			resp.getWriter().write(e.toString());
			log.error(e, e);
		}
	}

	private void requestRebuild() {
		indexer.rebuildIndex();
	}

	private Map<String, Object> buildStatusMap(SearchIndexerStatus status) {
		Map<String, Object> map = new HashMap<>();
		State state = status.getState();
		map.put("statusType", state);
		map.put("since", status.getSince());

		if (state == State.PROCESSING_URIS) {
			UriCounts counts = status.getCounts().asUriCounts();
			map.put("updated", counts.getUpdated());
			map.put("deleted", counts.getDeleted());
			map.put("remaining", counts.getRemaining());
			map.put("total", counts.getTotal());
			map.put("elapsed", breakDownElapsedTime(status.getSince()));
			map.put("expectedCompletion",
					figureExpectedCompletion(status.getSince(),
							counts.getTotal(),
							counts.getTotal() - counts.getRemaining()));
		} else if (state == State.PROCESSING_STMTS) {
			StatementCounts counts = status.getCounts().asStatementCounts();
			map.put("processed", counts.getProcessed());
			map.put("remaining", counts.getRemaining());
			map.put("total", counts.getTotal());
			map.put("elapsed", breakDownElapsedTime(status.getSince()));
			map.put("expectedCompletion",
					figureExpectedCompletion(status.getSince(),
							counts.getTotal(), counts.getProcessed()));
		} else if (state == State.REBUILDING) {
			RebuildCounts counts = status.getCounts().asRebuildCounts();
			map.put("documentsBefore", counts.getDocumentsBefore());
			map.put("documentsAfter", counts.getDocumentsAfter());
		} else {
			// nothing for IDLE or SHUTDOWN, except what's already there.
		}

		return map;
	}

	private Date figureExpectedCompletion(Date startTime, long totalToDo,
			long completedCount) {
		Date now = new Date();
		long elapsedMillis = now.getTime() - startTime.getTime();
		if (elapsedMillis <= 0) {
			return now;
		}
		if (completedCount <= 0) {
			return now;
		}
		if (totalToDo <= completedCount) {
			return now;
		}

		long millisPerRecord = elapsedMillis / completedCount;
		long expectedDuration = totalToDo * millisPerRecord;
		return new Date(expectedDuration + startTime.getTime());
	}

	private int[] breakDownElapsedTime(Date since) {
		long elapsedMillis = new Date().getTime() - since.getTime();
		long seconds = (elapsedMillis / 1000L) % 60L;
		long minutes = (elapsedMillis / 60000L) % 60L;
		long hours = elapsedMillis / 3600000L;
		return new int[] { (int) hours, (int) minutes, (int) seconds };
	}
}
