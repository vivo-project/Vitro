/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * A base for classes that produce a JSON object, based on the parameters in the
 * request.
 * 
 * The result is never empty. At worst, it is an object that contains only an
 * "errorMessage" field.
 * 
 * If an exception occurrs during processing, The "errorMessage" field will
 * contain the exception message and the response status will be set to 500
 * (server error). Normally, "errorMessage" will be empty, and the status will
 * default to 200 (OK).
 */
public abstract class JsonObjectProducer extends JsonProducer {
	private static final Log log = LogFactory.getLog(JsonObjectProducer.class);

	protected final VitroRequest vreq;
	protected final ServletContext ctx;

	protected JsonObjectProducer(VitroRequest vreq) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();
	}

	/**
	 * Sub-classes implement this method. Given the request, produce a JSON
	 * object as the result.
	 */
	protected abstract JSONObject process() throws Exception;

	public final void process(HttpServletResponse resp) throws IOException {
		JSONObject jsonObject = null;
		String errorMessage = "";

		try {
			jsonObject = process();
		} catch (Exception e) {
			log.error("Failed to create JSON response", e);
			errorMessage = e.toString();
			resp.setStatus(500 /* HttpURLConnection.HTTP_SERVER_ERROR */);
		}

		if (jsonObject == null) {
			jsonObject = new JSONObject();
		}

		log.debug("Response to JSON request: " + jsonObject.toString());

		try {
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("application/json;charset=UTF-8");
			Writer writer = resp.getWriter();

			jsonObject.put("errorMessage", errorMessage);
			writer.write(jsonObject.toString());
		} catch (JSONException e) {
			log.error(e, e);
		}
	}
}
