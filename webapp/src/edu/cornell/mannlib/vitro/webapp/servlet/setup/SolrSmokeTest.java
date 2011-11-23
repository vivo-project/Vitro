/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

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
		private final SolrSmokeTest listener;
		private final URL solrUrl;
		private final StartupStatus ss;

		public SolrSmokeTestThread(SolrSmokeTest listener, URL solrUrl,
				StartupStatus ss) {
			super("SolrSmokeTest");
			this.listener = listener;
			this.solrUrl = solrUrl;
			this.ss = ss;
		}

		@Override
		public void run() {
			HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(solrUrl.toExternalForm());
			try {
				int statusCode = client.executeMethod(method);
				method.getResponseBody();

				if (statusCode == HttpStatus.SC_OK) {
					reportSuccess();
				} else if (statusCode == HttpStatus.SC_FORBIDDEN) {
					warnForbidden();
				} else {
					warnBadHttpStatus(statusCode);
				}
			} catch (HttpException e) {
				warnProtocolViolation(e);
			} catch (IOException e) {
				warnTransportError(e);
			} finally {
				method.releaseConnection();
			}
		}

		private void reportSuccess() {
			ss.info(listener,
					"Successfully connected to the Solr search engine.");
		}

		private void warnForbidden() {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The server will not accept connections from this host. "
					+ "Check the value of vitro.local.solr.ipaddress.mask "
					+ "in deploy.properties");
		}

		private void warnBadHttpStatus(int status) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "The server returned a status code of " + status
					+ ". Check the value of vitro.local.solr.url in "
					+ "deploy.properties.");
		}

		private void warnProtocolViolation(HttpException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a protocol violation: " + e.getMessage(), e);
		}

		private void warnTransportError(IOException e) {
			ss.warning(listener, "Can't connect to the Solr search engine. "
					+ "Detected a transport error: " + e.getMessage(), e);
		}

	}
}
