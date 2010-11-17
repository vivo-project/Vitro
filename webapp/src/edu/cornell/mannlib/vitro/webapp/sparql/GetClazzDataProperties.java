package edu.cornell.mannlib.vitro.webapp.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.SiteAdminController;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

/**
 * This servlet gets all the data properties for a given subject.
 * 
 * @param vClassURI
 * @author yuysun
 */

public class GetClazzDataProperties extends BaseEditController {

	private static final Log log = LogFactory.getLog(SiteAdminController.class
			.getName());

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			super.doGet(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		VitroRequest vreq = new VitroRequest(request);

		Object obj = vreq.getSession().getAttribute("loginHandler");
		LoginFormBean loginHandler = null;
		if (obj != null && obj instanceof LoginFormBean)
			loginHandler = ((LoginFormBean) obj);
		if (loginHandler == null
				|| !"authenticated".equalsIgnoreCase(loginHandler
						.getLoginStatus()) ||
				// rjy7 Allows any editor (including self-editors) access to
				// this servlet.
				// This servlet is now requested via Ajax from some custom
				// forms, so anyone
				// using the custom form needs access rights.
				Integer.parseInt(loginHandler.getLoginRole()) < LoginFormBean.NON_EDITOR) {
			HttpSession session = request.getSession(true);

			session.setAttribute("postLoginRequest", vreq.getRequestURI()
					+ (vreq.getQueryString() != null ? ('?' + vreq
							.getQueryString()) : ""));
			String redirectURL = request.getContextPath()
					+ Controllers.SITE_ADMIN + "?login=block";
			response.sendRedirect(redirectURL);
			return;
		}

		String vClassURI = vreq.getParameter("vClassURI");
		if (vClassURI == null || vClassURI.trim().equals("")) {
			return;
		}

		String respo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		respo += "<options>";

		// Add rdfs:label to the list
		respo += "<option>" + "<key>" + "label" + "</key>" + "<value>"
				+ "http://www.w3.org/2000/01/rdf-schema#label" + "</value>"
				+ "</option>";

		DataPropertyDao ddao = vreq.getFullWebappDaoFactory()
				.getDataPropertyDao();

		Collection<DataProperty> dataProps = ddao
				.getDataPropertiesForVClass(vClassURI);
		Iterator<DataProperty> dataPropIt = dataProps.iterator();
		HashSet<String> dpropURIs = new HashSet<String>();
		while (dataPropIt.hasNext()) {
			DataProperty dp = dataPropIt.next();
			if (!(dpropURIs.contains(dp.getURI()))) {
				dpropURIs.add(dp.getURI());
				DataProperty dprop = (DataProperty) ddao
						.getDataPropertyByURI(dp.getURI());
				if (dprop != null) {
					respo += "<option>" + "<key>" + dprop.getLocalName()
							+ "</key>" + "<value>" + dprop.getURI()
							+ "</value>" + "</option>";
				}
			}
		}
		respo += "</options>";
		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		out.println(respo);
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}
}
