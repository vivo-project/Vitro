/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

/**
 * A base class with some utility routines for page handler (created by
 * controller classes.
 * 
 * The controller logic is often complicated by the fact that the servlet must
 * be multi-threaded. If a "page handler" instance is created for each request,
 * it may share instance variables among its methods, which frequently makes for
 * clearer logic.
 */
public abstract class AbstractPageHandler {
	private static final Log log = LogFactory.getLog(AbstractPageHandler.class);

	protected final VitroRequest vreq;
	protected final ServletContext ctx;
	protected final OntModel userAccountsModel;
	protected final OntModel unionModel;
	protected final UserAccountsDao userAccountsDao;
	protected final VClassDao vclassDao;
	protected final IndividualDao indDao;
	protected final DataPropertyStatementDao dpsDao;
	protected final ObjectPropertyStatementDao opsDao;

	protected AbstractPageHandler(VitroRequest vreq) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();

		OntModelSelector oms = ModelContext.getUnionOntModelSelector(ctx);
		userAccountsModel = oms.getUserAccountsModel();
		unionModel = oms.getFullModel();

		WebappDaoFactory wdf = (WebappDaoFactory) this.ctx
				.getAttribute("webappDaoFactory");
		userAccountsDao = wdf.getUserAccountsDao();
		vclassDao = wdf.getVClassDao();
		indDao = wdf.getIndividualDao();
		dpsDao = wdf.getDataPropertyStatementDao();
		opsDao = wdf.getObjectPropertyStatementDao();
	}

	// ----------------------------------------------------------------------
	// Methods for handling request parameters.
	// ----------------------------------------------------------------------

	protected String getStringParameter(String key, String defaultValue) {
		String value = vreq.getParameter(key);
		return (value == null) ? defaultValue : value;
	}

	protected List<String> getStringParameters(String key) {
		String[] values = vreq.getParameterValues(key);
		if (values == null) {
			return Collections.emptyList();
		} else {
			return new ArrayList<String>(Arrays.asList(values));
		}
	}

	protected int getIntegerParameter(String key, int defaultValue) {
		String value = vreq.getParameter(key);
		if (value == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			log.warn("Invalid integer for parameter '" + key + "': " + value);
			return defaultValue;
		}
	}

	/**
	 * Check for the presence of a parameter, regardless of its value, even if
	 * it's an empty string.
	 */
	protected boolean isFlagOnRequest(String key) {
		String value = vreq.getParameter(key);
		return (value != null);
	}

	/**
	 * Treat the presence of a certain parameter, with a desired value, as a
	 * boolean flag.
	 * 
	 * An example would be radio buttons with values of "yes" and "no". The
	 * expected value would be "yes".
	 */
	protected boolean isParameterAsExpected(String key, String expected) {
		return expected.equals(getStringParameter(key, ""));
	}

	// ----------------------------------------------------------------------
	// Message methods
	// ----------------------------------------------------------------------

	/**
	 * If a Message has been set in the session, get it, store its info in the
	 * body map under "message", and remove it from the session, so it will not
	 * be displayed again.
	 */
	protected void applyMessage(HttpServletRequest req, Map<String, Object> body) {
		Message.applyMessageToBodyMap(req, body, "message");
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * Indicates that parameters have failed validation.
	 */
	protected static class InvalidParametersException extends Exception {
		public InvalidParametersException(String message) {
			super(message);
		}
	}

	/**
	 * Set one of these on the session, so it can be interpreted and displayed
	 * at the next request. It will only be displayed once.
	 * 
	 * Allows one page handler to pass a message to another page handler through
	 * a re-direct.
	 */
	public abstract static class Message {
		private static final String ATTRIBUTE = Message.class.getName();

		public static void setMessage(HttpServletRequest req, Message message) {
			log.debug("Added message to session: " + message.getMessageInfoMap());
			req.getSession().setAttribute(ATTRIBUTE, message);
		}

		public static void applyMessageToBodyMap(HttpServletRequest req,
				Map<String, Object> body, String key) {
			Object o = req.getSession().getAttribute(ATTRIBUTE);
			req.getSession().removeAttribute(ATTRIBUTE);

			if (o instanceof Message) {
				body.put(key, ((Message) o).getMessageInfoMap());
			}
		}

		public Map<String, Object> assembleMap(Object... args) {
			if (args.length % 2 != 0) {
				throw new IllegalArgumentException(
						"you must provide keys and values in pairs.");
			}

			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < args.length; i += 2) {
				if (!(args[i] instanceof String)) {
					throw new IllegalArgumentException("args[" + i
							+ "] is not a String");
				}
				map.put((String) args[i], args[i + 1]);
			}

			return map;
		}

		public abstract Map<String, Object> getMessageInfoMap();
	}
}
