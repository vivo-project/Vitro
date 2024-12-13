/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.searchengine.base.SearchEngineUtil;
import edu.cornell.mannlib.vitro.webapp.utils.http.HttpClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;
import org.apache.http.util.EntityUtils;

/**
 * Spin off a thread that will try to connect to Solr.
 *
 * We need to do this in a separate thread because if Solr is in the same Tomcat
 * instance as Vitro, it may not be initialized until after Vitro is
 * initialized. Which is to say, after this Listener has run.
 *
 * If we can't connect to Solr, add a Warning item to the StartupStatus.
 */
public class SolrSmokeTest {
	private static final Log log = LogFactory.getLog(SolrSmokeTest.class);

	private final ServletContextListener listener;

	/*
	 * We don't want to treat socket timeout as a non-recoverable error like the
	 * other exceptions. So pretend there's a status code for it instead.
	 */
	private static final int SOCKET_TIMEOUT_STATUS = -500;

	public SolrSmokeTest(ServletContextListener listener) {
		this.listener = listener;
	}

	public void doTest(ServletContextEvent sce) {
		final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

		String solrUrlString = SearchEngineUtil.getSearchEngineURLProperty();
		if (Objects.isNull(solrUrlString) || solrUrlString.isEmpty()) {
			ss.fatal(listener, "Can't connect to Solr search engine. "
					+ "runtime.properties must contain a value for "
					+ "vitro.local.searchengine.url (vitro.local.solr.url)");
			return;
		}

		URL solrUrl = null;

		try {
			solrUrl = new URL(solrUrlString);
		} catch (MalformedURLException e) {
			ss.fatal(listener, "Can't connect to Solr search engine. "
					+ "The value for vitro.local.searchengine.url (vitro.local.solr.url) "
					+ "in runtime.properties is not a valid URL: '"
					+ solrUrlString + "'", e);
		}

		ss.info(listener, "Starting thread for Solr test.");
		new SolrSmokeTestThread(listener, solrUrl, ss).start();
	}

	private static class SolrSmokeTestThread extends VitroBackgroundThread {
		private final ServletContextListener listener;
		private final URL solrUrl;
		private final StartupStatus ss;

		public SolrSmokeTestThread(ServletContextListener listener, URL solrUrl,
				StartupStatus ss) {
			super("SolrSmokeTest");
			this.listener = listener;
			this.solrUrl = solrUrl;
			this.ss = ss;
		}

		/**
		 * Try to connect until we suceed, until we detect an unrecoverable
		 * error, or until we have failed 3 times.
		 */
		@Override
		public void run() {
			try {
				new SolrHomePager(solrUrl).connect();
				reportSuccess();
			} catch (SolrProblemException e) {
				reportProblem(e);
				return;
			}

			try {
				new SolrPinger(solrUrl).ping();
				reportGoodPing();
			} catch (SolrProblemException e) {
				reportPingProblem(e);
            }
		}

		private void reportSuccess() {
			ss.info(listener,
					"Successfully connected to the Solr search server.");
		}

		private void reportGoodPing() {
			ss.info(listener, "The Solr search server responded to a 'ping'.");
		}

		private void reportProblem(SolrProblemException e) {
			int status = e.getStatusCode();
			Throwable cause = e.getCause();

			if (status == SOCKET_TIMEOUT_STATUS) {
				warnSocketTimeout();
			} else if (status != 0) {
				warnBadHttpStatus(status);
			} else if (cause instanceof HttpException) {
				warnProtocolViolation((HttpException) cause);
			} else if (cause instanceof UnknownHostException) {
				warnUnknownHost((UnknownHostException) cause);
			} else if (cause instanceof ConnectException) {
				warnConnectionRefused((ConnectException) cause);
			} else if (cause instanceof IOException) {
				warnTransportError((IOException) cause);
			} else {
				warnUnknownProblem(e);
			}
		}

		private void reportPingProblem(SolrProblemException e) {
			ss.warning(
					listener,
					"The Solr search engine did not respond to a 'ping' request",
					e);
		}

		private void warnSocketTimeout() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The socket connection has repeatedly timed out. "
					+ "Check the value of vitro.local.searchengine.url (vitro.local.solr.url) in "
					+ "runtime.properties. Is Solr responding at that URL?");
		}

		private void warnBadHttpStatus(int status) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The Solr server returned a status code of " + status
					+ ". Check the value of vitro.local.searchengine.url (vitro.local.solr.url) in "
					+ "runtime.properties.");
		}

		private void warnProtocolViolation(HttpException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a protocol violation: " + e.getMessage(), e);
		}

		private void warnUnknownHost(UnknownHostException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. '"
					+ e.getMessage() + "' is an unknown host."
					+ "Check the value of vitro.local.searchengine.url (vitro.local.solr.url) in "
					+ "runtime.properties.", e);
		}

		private void warnConnectionRefused(ConnectException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The host refused the connection. "
					+ "Is it possible that the port number is incorrect? "
					+ "Check the value of vitro.local.searchengine.url (vitro.local.solr.url) in "
					+ "runtime.properties.", e);
		}

		private void warnTransportError(IOException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a transport error: " + e.getMessage(), e);
		}

		private void warnUnknownProblem(SolrProblemException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Unrecognized error: " + e.getMessage(), e);
		}

	}

	/**
	 * <pre>
	 * This gets a little tricky. Try to connect until:
	 *   -- we suceed
	 *   -- we detect a non-recoverable error
	 *   -- we fail three times
	 * </pre>
	 */
	private static class SolrHomePager {
		private static final long SLEEP_INTERVAL = 20000; // 20 seconds
        private static final long SLEEP_MAX = 300000; // maximum sleep time: 5 minutes
        private static long SLEEP_DURATION = 0; // how long have we been sleeping?

		private final URL solrUrl;
		private final HttpClient httpClient = HttpClientFactory.getHttpClient();

		private int statusCode;

		public SolrHomePager(URL solrUrl) {
			this.solrUrl = solrUrl;
		}

		public void connect() throws SolrProblemException {
			tryToConnect();

            while (!isDone() && SLEEP_DURATION < SLEEP_MAX) {
				sleep();
				tryToConnect();
			}

			if (statusCode != HttpStatus.SC_OK) {
				throw new SolrProblemException(statusCode);
			}
		}

		private void tryToConnect() throws SolrProblemException {
            SolrSmokeTest.log.debug("Trying to connect to Solr, wait up to " + SLEEP_MAX / 60000 + " minutes - " +
                    (int)(SLEEP_DURATION * 100.0 / SLEEP_MAX) + "%");

			try {
				HttpGet method = new HttpGet(solrUrl.toExternalForm() + "/select");
                HttpResponse response = httpClient.execute(method);
				try {
					statusCode = response.getStatusLine().getStatusCode();
					SolrSmokeTest.log.debug("HTTP status was " + statusCode);
				} finally {
					EntityUtils.consume(response.getEntity());
				}
            } catch (IOException e) {
				// Catch the exception so we can retry this.
				// Save the status so we know why we failed.
				statusCode = SolrSmokeTest.SOCKET_TIMEOUT_STATUS;
			} catch (Exception e) {
				throw new SolrProblemException(e);
			}
		}

		/**
		 * Stop trying to connect if we succeed, or if we receive an error that
		 * won't change on retry.
		 */
		private boolean isDone() {
			return (statusCode == HttpStatus.SC_OK)
					|| (statusCode == HttpStatus.SC_FORBIDDEN);
		}

		private void sleep() {
			try {
                SLEEP_DURATION += SLEEP_INTERVAL;
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace(); // Should never happen
			}
		}

	}

	/**
	 * Issue a "ping" to Solr. If we get here, we've already established
	 * contact, so any error is a fatal one.
	 */
	private static class SolrPinger {
		private final URL solrUrl;
		private final HttpClient httpClient = HttpClientFactory.getHttpClient();

		public SolrPinger(URL solrUrl) {
			this.solrUrl = solrUrl;
		}

		public void ping() throws SolrProblemException {
			try {
				HttpGet method = new HttpGet(solrUrl.toExternalForm()
						+ "/admin/ping");
				SolrSmokeTest.log.debug("Trying to ping Solr");
				HttpResponse response = httpClient.execute(method);
				try {
					SolrSmokeTest.log.debug("Finished pinging Solr");
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode != HttpStatus.SC_OK) {
						throw new SolrProblemException(statusCode);
					}
				} finally {
					EntityUtils.consume(response.getEntity());
				}
			} catch (IOException e) {
				throw new SolrProblemException(e);
			}
		}
	}

	private static class SolrProblemException extends Exception {
		private final int statusCode;

		SolrProblemException(int statusCode) {
			super("HTTP status code = " + statusCode);
			this.statusCode = statusCode;
		}

		SolrProblemException(Throwable cause) {
			super(cause);
			this.statusCode = 0;
		}

		int getStatusCode() {
			return this.statusCode;
		}
	}
}
