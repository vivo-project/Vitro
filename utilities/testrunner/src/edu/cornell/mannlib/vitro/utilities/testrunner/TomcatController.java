/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;
import edu.cornell.mannlib.vitro.utilities.testrunner.tomcat.HttpHelper;

/**
 * Start and stop the webapp, so we can clean the database.
 */
public class TomcatController {
	private static final Pattern PATTERN_WEBAPP_LISTING = Pattern
			.compile("/(\\w+):(\\w+):");

	private final SeleniumRunnerParameters parms;
	private final ModelCleanerProperties properties;
	private final Listener listener;

	private final String tomcatBaseUrl;
	private final String tomcatManagerUrl;
	private final String tomcatManagerUsername;
	private final String tomcatManagerPassword;
	private final String webappName;

	public TomcatController(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.properties = parms.getModelCleanerProperties();
		this.listener = parms.getListener();

		this.webappName = properties.getVivoWebappName();
		this.tomcatBaseUrl = figureBaseUrl();
		this.tomcatManagerUrl = this.tomcatBaseUrl + "/manager";
		this.tomcatManagerUsername = properties.getTomcatManagerUsername();
		this.tomcatManagerPassword = properties.getTomcatManagerPassword();

		checkThatTomcatIsReady();
	}

	private String figureBaseUrl() {
		String url = parms.getWebsiteUrl();
		int end = url.lastIndexOf(webappName);
		return url.substring(0, end);
	}

	/**
	 * Insure that Tomcat is running and has the ability to start and stop VIVO.
	 */
	private void checkThatTomcatIsReady() {
		HttpHelper hh = new HttpHelper();

		// Is Tomcat responding?
		if (!hh.getPage(tomcatBaseUrl)) {
			throw newHttpException(hh, "Tomcat does not respond");
		}

		// Does the manager respond?
		hh.getPage(tomcatManagerUrl + "/list");
		if (hh.getStatus() == 404) {
			throw newHttpException(hh,
					"Tomcat manager application does not respond. "
							+ "Is it installed?");
		}

		// Do we have the correct authorization for the manager?
		hh.getPage(tomcatManagerUrl + "/list", tomcatManagerUsername,
				tomcatManagerPassword);
		if (hh.getStatus() != 200) {
			throw newHttpException(hh, "Failed to list Tomcat applications");
		}

		// Is the VIVO application running?
		boolean running = isVivoRunning(hh.getResponseText());

		if (running) {
			stopTheWebapp();
		}

		// Be sure that we can start it.
		startTheWebapp();
	}

	/**
	 * Tell Tomcat to start the webapp. Check the response.
	 */
	public void startTheWebapp() {
		String startCommand = tomcatManagerUrl + "/start?path=/" + webappName;
		listener.webappStarting(startCommand);

		HttpHelper hh = new HttpHelper();
		hh.getPage(startCommand, tomcatManagerUsername, tomcatManagerPassword);

		if ((hh.getStatus() != 200) || (!hh.getResponseText().startsWith("OK"))) {
			listener.webappStartFailed(hh.getStatus());
			throw newHttpException(hh, "Failed to start the webapp '"
					+ webappName + "'");
		}

		listener.webappStarted();
	}

	/**
	 * Tell Tomcat to stop the webapp. Check the response.
	 */
	public void stopTheWebapp() {
		String stopCommand = tomcatManagerUrl + "/stop?path=/" + webappName;
		listener.webappStopping(stopCommand);

		HttpHelper hh = new HttpHelper();
		hh.getPage(stopCommand, tomcatManagerUsername, tomcatManagerPassword);

		if ((hh.getStatus() != 200) || (!hh.getResponseText().startsWith("OK"))) {
			listener.webappStopFailed(hh.getStatus());
			throw newHttpException(hh, "Failed to stop the webapp '"
					+ webappName + "'");
		}

		listener.webappStopped();
	}

	/**
	 * Is the VIVO application listed, and is it running?
	 */
	private boolean isVivoRunning(String responseText) {
		boolean found = false;
		boolean running = false;

		BufferedReader r = new BufferedReader(new StringReader(responseText));
		try {
			String line;
			while (null != (line = r.readLine())) {
				Matcher m = PATTERN_WEBAPP_LISTING.matcher(line);
				if (m.find()) {
					if (this.webappName.equals(m.group(1))) {
						found = true;
						if ("running".equals(m.group(2))) {
							running = true;
						}
						break;
					}
				}
			}
			r.close();
		} catch (IOException e) {
			// Can't happen when reading from a string.
			e.printStackTrace();
		}

		if (!found) {
			throw new FatalException("Webapp '" + this.webappName
					+ "' not found in Tomcat's list of webapps: \n"
					+ responseText);
		}

		return running;
	}

	/**
	 * Generate a {@link FatalException} that contains a bunch of info from the
	 * {@link HttpHelper}.
	 */
	private FatalException newHttpException(HttpHelper hh, String text) {
		return new FatalException(text + "   status is " + hh.getStatus()
				+ ", response text is '" + hh.getResponseText() + "'");
	}

	/**
	 * The run is finished. Do we need to do anything?
	 */
	public void cleanup() {
		// Leave the webapp running.
	}

}
