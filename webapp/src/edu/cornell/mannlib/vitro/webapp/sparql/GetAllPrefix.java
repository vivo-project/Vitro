/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.sparql;

import java.io.IOException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.SiteAdminController;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;

/**
 * This servlet gets all the prefix for initizing the sparql query builder.
 * 
 * @author yuysun
 */

public class GetAllPrefix extends BaseEditController {

	private static final Log log = LogFactory.getLog(SiteAdminController.class
			.getName());

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
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
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			super.doGet(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if( !checkLoginStatus(request, response) )
        	return;
		VitroRequest vreq = new VitroRequest(request);


		// EditProcessObject epo = super.createEpo(request);
		OntologyDao daoObj = vreq.getFullWebappDaoFactory().getOntologyDao();
		List ontologiesObj = daoObj.getAllOntologies();
		ArrayList prefixList = new ArrayList();

		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String respo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		respo += "<options>";
		if (ontologiesObj != null && ontologiesObj.size() > 0) {

			Iterator ontItr = ontologiesObj.iterator();
			while (ontItr.hasNext()) {
				Ontology ont = (Ontology) ontItr.next();
				if (ont.getPrefix() != null) {
					respo += "<option>" + "<key>" + ont.getPrefix() + "</key>"
							+ "<value>" + ont.getURI() + "</value>"
							+ "</option>";
				}
			}

		}
		respo += "</options>";
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
