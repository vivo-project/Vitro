/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;

/**
 * This servlet gets all the data properties for a given subject.
 * 
 * @param vClassURI
 * @author yuysun
 */

public class GetClazzDataProperties extends BaseEditController {

	private static final Log log = LogFactory.getLog(GetClazzDataProperties.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!isAuthorizedToDisplayPage(request, response, new Actions(new UseMiscellaneousPages()))) {
        	return;
		}

		VitroRequest vreq = new VitroRequest(request);

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
