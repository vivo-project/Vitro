/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * Spin off a thread that will try to connect to Solr.
 * 
 * We need to do this in a separate thread because if Solr is in the same Tomcat
 * instance as Vitro, it may not be initialized until after Vitro is
 * initialized. Which is to say, after this Listener has run.
 * 
 * If we can't connect to Solr, add a Warning item to the StartupStatus.
 */
public class SolrSmokeTest implements ServletContextListener {
	private static final Log log = LogFactory.getLog(SolrSmokeTest.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

		String solrUrlString = ConfigurationProperties.getBean(sce)
				.getProperty("vitro.local.solr.url", "");
		if (solrUrlString.isEmpty()) {
			ss.fatal(this, "Can't connect to Solr search engine. "
					+ "deploy.properties must contain a value for "
					+ "vitro.local.solr.url");
			return;
		}

		URL solrUrl = null;

		try {
			solrUrl = new URL(solrUrlString);
		} catch (MalformedURLException e) {
			ss.fatal(this, "Can't connect to Solr search engine. "
					+ "The value for vitro.local.solr.url "
					+ "in deploy.properties is not a valid URL: '"
					+ solrUrlString + "'", e);
		}

		ss.info(this, "Starting thread for Solr test.");
		new SolrSmokeTestThread(this, solrUrl, ss).start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to tear down.
	}

	private static class SolrSmokeTestThread extends VitroBackgroundThread {
		/* Use this code instead of an exception to track socket timeout. */
		private static final int SOCKET_TIMEOUT_STATUS = -500; 
		
		private static final long SLEEP_INTERVAL = 10000; // 10 seconds
		private final SolrSmokeTest listener;
		private final URL solrUrl;
		private final StartupStatus ss;
		private final HttpClient httpClient = new HttpClient();

		private int statusCode;

		public SolrSmokeTestThread(SolrSmokeTest listener, URL solrUrl,
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
				tryToConnect();
				if (!isDone()) {
					sleep();
					tryToConnect();
					if (!isDone()) {
						sleep();
						tryToConnect();
					}
				}

				if (statusCode == HttpStatus.SC_OK) {
					reportSuccess();
				} else if (statusCode == HttpStatus.SC_FORBIDDEN) {
					warnForbidden();
				} else if (statusCode == SOCKET_TIMEOUT_STATUS) {
					warnSocketTimeout();
				} else {
					warnBadHttpStatus();
				}
			} catch (HttpException e) {
				warnProtocolViolation(e);
			} catch (UnknownHostException e) {
				warnUnknownHost(e);
			} catch (ConnectException e) {
				warnConnectionRefused(e);
			} catch (IOException e) {
				warnTransportError(e);
			}
		}

		private void tryToConnect() throws IOException {
			GetMethod method = new GetMethod(solrUrl.toExternalForm());
			try {
				log.debug("Trying to connect to Solr");
				statusCode = httpClient.executeMethod(method);
				log.debug("HTTP status was " + statusCode);

				// clear the buffer.
				InputStream stream = method.getResponseBodyAsStream();
				stream.close();
			} catch (SocketTimeoutException e) {
				// Catch the exception so we can retry this.
				// Save the status so we know why we failed.
				statusCode = SOCKET_TIMEOUT_STATUS;
			} finally {
				method.releaseConnection();
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
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace(); // Should never happen
			}
		}

		private void reportSuccess() {
			ss.info(listener,
					"Successfully connected to the Solr search engine.");
		}

		private void warnSocketTimeout() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The socket connection has repeatedly timed out. "
					+ "Check the value of vitro.local.solr.url in "
					+ "deploy.properties. Is Solr responding at that URL?");
		}

		private void warnForbidden() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The Solr server will not accept connections from this "
					+ "host. Check the value of "
					+ "vitro.local.solr.ipaddress.mask in "
					+ "deploy.properties -- "
					+ "does it authorize access from this IP address?");
		}

		private void warnBadHttpStatus() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The Solr server returned a status code of " + statusCode
					+ ". Check the value of vitro.local.solr.url in "
					+ "deploy.properties.");
		}

		private void warnProtocolViolation(HttpException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a protocol violation: " + e.getMessage(), e);
		}

		private void warnUnknownHost(UnknownHostException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. '"
					+ e.getMessage() + "' is an unknown host."
					+ "Check the value of vitro.local.solr.url in "
					+ "deploy.properties.", e);
		}

		private void warnConnectionRefused(ConnectException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The host refused the connection. "
					+ "Is it possible that the port number is incorrect? "
					+ "Check the value of vitro.local.solr.url in "
					+ "deploy.properties.", e);
		}

		private void warnTransportError(IOException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a transport error: " + e.getMessage(), e);
		}

	}
}
