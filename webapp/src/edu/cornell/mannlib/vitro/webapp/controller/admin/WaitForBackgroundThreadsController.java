/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevelStamp;

/**
 * Wait for background threads to complete. Used in Selenium testing.
 * 
 * This servlet will poll background threads (instances of
 * VitroBackgroundThread) until all living threads are idle, or until a maximum
 * wait time has been met. The wait time can be specified with a "waitLimit"
 * parameter on the request (in seconds), or the default value will be used.
 * 
 * If the maximum time expires before all threads become idle, the result will
 * be 503 (Service Unavailable)
 * 
 * Else if a "return" parameter exists, and a "referer" header exists, the
 * result will be a 307 (Temporary Redirect) back to the referer URL.
 * 
 * Otherwise, the result will be 200 (OK), with a brief message.
 */
public class WaitForBackgroundThreadsController extends VitroHttpServlet {
	private static final Log log = LogFactory
			.getLog(WaitForBackgroundThreadsController.class);

	private static final String PARAMETER_WAIT_LIMIT = "waitLimit";
	private static final String PARAMETER_RETURN = "return";
	private static final String HEADER_REFERER = "Referer";
	private static final String HEADER_LOCATION = "Location";
	private static final int DEFAULT_WAIT_LIMIT_VALUE = 30;
	private static final int POLLING_INTERVAL = 3;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		int maximumWait = figureMaximumWait(req);
		String redirect = figureRedirect(req);

		Collection<String> remainingThreads = waitForThreads(maximumWait);
		if (remainingThreads.isEmpty()) {
			if (redirect == null) {
				sendOK(resp);
			} else {
				sendRedirect(resp, redirect);
			}
		} else {
			sendFailure(resp, maximumWait, remainingThreads);
		}
	}

	/**
	 * If the "waitLimit " parameter is present and is set to a non-negative
	 * integer, use that as the maximum number of seconds. Otherwise, use the
	 * default limit.
	 */
	private int figureMaximumWait(HttpServletRequest req) {
		String valueString = req.getParameter(PARAMETER_WAIT_LIMIT);
		if (valueString == null) {
			return DEFAULT_WAIT_LIMIT_VALUE;
		}

		int value;
		try {
			value = Integer.parseInt(valueString);
		} catch (NumberFormatException e) {
			return DEFAULT_WAIT_LIMIT_VALUE;
		}

		if (value <= 0) {
			return DEFAULT_WAIT_LIMIT_VALUE;
		}

		log.debug("Maximum wait time (seconds): " + value);
		return value;
	}

	/**
	 * If there is a "return" parameter and a "referer" header, return the
	 * referer URL. Otherwise, return null.
	 */
	private String figureRedirect(HttpServletRequest req) {
		if (!req.getParameterMap().containsKey(PARAMETER_RETURN)) {
			return null;
		}

		Enumeration<?> referers = req.getHeaders(HEADER_REFERER);
		if ((referers == null) || (!referers.hasMoreElements())) {
			return null;
		}

		String redirect = (String) referers.nextElement();
		log.debug("Redirect is to '" + redirect + "'");
		return redirect;
	}

	/**
	 * Wait until all background threads have become idle, or until the time
	 * limit is passed. Return the names of any that are still active; hopefully
	 * an empty list.
	 */
	private Collection<String> waitForThreads(int maximumWait) {
		int elapsedSeconds = 0;

		while (true) {
			Collection<String> threadNames = getNamesOfBusyThreads();
			if (threadNames.isEmpty()) {
				return Collections.emptySet();
			}

			try {
				log.debug("Waiting for " + POLLING_INTERVAL + " seconds.");
				Thread.sleep(POLLING_INTERVAL * 1000);
			} catch (InterruptedException e) {
				// Why would this happen? Anyway, stop waiting.
				return Collections.singleton("Polling was interrupted");
			}

			elapsedSeconds += POLLING_INTERVAL;
			if (elapsedSeconds >= maximumWait) {
				return threadNames;
			}
		}
	}

	private Collection<String> getNamesOfBusyThreads() {
		List<String> names = new ArrayList<String>();
		for (VitroBackgroundThread thread : VitroBackgroundThread.getThreads()) {
			if (thread.isAlive()) {
				WorkLevelStamp stamp = thread.getWorkLevel();
				if ((stamp != null) && (stamp.getLevel() == WorkLevel.WORKING)) {
					names.add(thread.getName());
				}
			}
		}
		log.debug("Busy threads: " + names);
		return names;
	}

	private void sendOK(HttpServletResponse resp) throws IOException {
		log.debug("All threads are idle");
		resp.setStatus(SC_OK);
		resp.getWriter().println(
				"<html><body>All threads are idle.</body></html>");
	}

	private void sendRedirect(HttpServletResponse resp, String redirect) {
		log.debug("All threads are idle. Redirecting to '" + redirect + "'");
		resp.setStatus(SC_TEMPORARY_REDIRECT);
		resp.setHeader(HEADER_LOCATION, redirect);
	}

	private void sendFailure(HttpServletResponse resp, int maximumWait,
			Collection<String> namesOfBusyThreads) throws IOException {
		log.debug("Timeout after " + maximumWait
				+ " seconds with busy threads: " + namesOfBusyThreads);
		resp.setStatus(SC_SERVICE_UNAVAILABLE);
		resp.getWriter().println(
				"<html><body>After " + maximumWait + " seconds, "
						+ namesOfBusyThreads.size()
						+ " threads are still busy: " + namesOfBusyThreads
						+ "</body></html>");
	}
}
