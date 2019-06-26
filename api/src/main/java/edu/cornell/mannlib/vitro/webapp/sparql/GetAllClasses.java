/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * This servlet gets all the classes for initizing the sparql query builder.
 *
 * @author yuysun
 */

@WebServlet(name = "GetAllClasses", urlPatterns = {"/admin/getAllClasses"})
public class GetAllClasses extends BaseEditController {

	private static final Log log = LogFactory.getLog(GetAllClasses.class);

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
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_MISCELLANEOUS_PAGES.ACTION)) {
        	return;
		}

		VitroRequest vreq = new VitroRequest(request);

		// EditProcessObject epo = super.createEpo(request);

		List classGroups = vreq.getUnfilteredWebappDaoFactory().getVClassGroupDao()
				.getPublicGroupsWithVClasses(true, true, false); // order by
																	// displayRank,
																	// include
																	// uninstantiated
																	// classes,
																	// don't get
																	// the
																	// counts of
																	// individuals

		Iterator classGroupIt = classGroups.iterator();

		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		StringBuilder respo = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		respo.append("<options>");

		while (classGroupIt.hasNext()) {
			VClassGroup group = (VClassGroup) classGroupIt.next();
			List<VClass> classes = group.getVitroClassList();
			for (VClass clazz : classes) {
				respo.append("<option>" + "<key>").append(clazz.getPickListName()).append("</key>").append("<value>").append(clazz.getURI()).append("</value>").append("</option>");
			}
		}
		respo.append("</options>");
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
