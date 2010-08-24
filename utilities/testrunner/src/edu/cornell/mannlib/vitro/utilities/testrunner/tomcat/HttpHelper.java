/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.tomcat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;

import edu.cornell.mannlib.vitro.utilities.testrunner.FileHelper;

/**
 * TODO
 */
public class HttpHelper {
	private String responseText;
	private int status = -1;
	private Throwable exception;

	/**
	 * Read the page at the specified URL, and set the response text and status.
	 */
	public boolean getPage(String url) {
		this.responseText = null;
		this.status = -1;
		this.exception = null;

		HttpClient httpClient = new HttpClient();
		HttpMethod method = new GetMethod(url);
		Reader reader = null;
		try {
			method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			method.getParams().setSoTimeout(60000);

			httpClient.executeMethod(method);

			this.status = method.getStatusCode();

			reader = new InputStreamReader(method.getResponseBodyAsStream(),
					Charset.forName("UTF-8"));
			this.responseText = FileHelper.readAll(reader);

			return true;
		} catch (IOException e) {
			this.exception = e;
			return false;
		} finally {
			method.releaseConnection();
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read the page at the specified URL, performing authentication with the
	 * specified username and password, and set the response text and status.
	 */
	public boolean getPage(String url, String username, String password) {
		responseText = null;
		status = -1;
		this.exception = null;

		HttpClient httpClient = new HttpClient();
		httpClient.getState().setCredentials(new AuthScope(null, -1, null, "basic"),
				new UsernamePasswordCredentials(username, password));

		HttpMethod method = new GetMethod(url);

		Reader reader = null;
		try {
			method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			method.getParams().setSoTimeout(60000);

			httpClient.executeMethod(method);

			this.status = method.getStatusCode();

			reader = new InputStreamReader(method.getResponseBodyAsStream(),
					Charset.forName("UTF-8"));
			responseText = FileHelper.readAll(reader);

			return true;
		} catch (IOException e) {
			this.exception = e;
			return false;
		} finally {
			method.releaseConnection();
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getStatus() {
		return status;
	}

	public String getResponseText() {
		return responseText;
	}

	public Throwable getException() {
		return exception;
	}

	public static class HttpHelperException extends Exception {

		public HttpHelperException() {
		}

		public HttpHelperException(String message, Throwable cause) {
			super(message, cause);
		}

		public HttpHelperException(String message) {
			super(message);
		}

		public HttpHelperException(Throwable cause) {
			super(cause);
		}

	}
}
