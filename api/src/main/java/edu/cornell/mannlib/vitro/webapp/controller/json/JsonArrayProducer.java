/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * A base for classes that produce a JSON array based on the parameters in the
 * VitroRequest.
 * 
 * Catches any exceptions. Logs the error and returns an empty JSON array.
 */
public abstract class JsonArrayProducer extends JsonProducer {
	private static final Log log = LogFactory.getLog(JsonArrayProducer.class);

	protected final VitroRequest vreq;
	protected final ServletContext ctx;

	protected JsonArrayProducer(VitroRequest vreq) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();
	}

	/**
	 * Sub-classes implement this method. Given the request, produce a JSON
	 * object as the result.
	 */
	protected abstract JSONArray process() throws Exception;

	public final void process(HttpServletResponse resp) throws IOException {
		JSONArray jsonArray = null;
		try {
			jsonArray = process();
		} catch (Exception e) {
			log.error("Failed to create JSON response" + e);
			resp.setStatus(500 /* HttpURLConnection.HTTP_SERVER_ERROR */);
		}

		if (jsonArray == null) {
			jsonArray = new JSONArray();
		}

		log.debug("Response to JSON request: " + jsonArray.toString());

		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		Writer writer = resp.getWriter();
		writer.write(jsonArray.toString());
	}
}
