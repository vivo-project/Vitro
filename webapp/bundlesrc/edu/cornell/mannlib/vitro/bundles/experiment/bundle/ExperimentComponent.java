/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.bundles.experiment.bundle;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

/**
 * TODO
 */
@Component
public class ExperimentComponent {
	private static final Log log = LogFactory.getLog(ExperimentComponent.class);

	private HttpService httpService;
	
	@Reference
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	@Activate
	public void startup() throws ServletException, NamespaceException {
		httpService.registerServlet("/totally/bogus", new BogusServlet(), null, null);
	}
	
	@Deactivate
	public void shutdown() {
		httpService.unregister("/totally/bogus");
	}

	public static class BogusServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			PrintWriter out = resp.getWriter();
			out.println("<h1>doGet in BogusServlet</h1>");
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			PrintWriter out = resp.getWriter();
			out.println("<h1>doPost in BogusServlet</h1>");
		}
		
	}
}
