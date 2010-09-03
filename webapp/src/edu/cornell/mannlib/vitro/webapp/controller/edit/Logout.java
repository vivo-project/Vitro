/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LogoutEvent;

public class Logout extends HttpServlet /* implements SingleThreadModel */{

	private static final Log log = LogFactory.getLog(Logout.class.getName());

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			VitroRequest vreq = new VitroRequest(request);
			HttpSession session = vreq.getSession();
			if (session != null) {
				UserDao userDao = ((WebappDaoFactory) session
						.getServletContext().getAttribute("webappDaoFactory"))
						.getUserDao();
				LoginFormBean f = (LoginFormBean) session
						.getAttribute("loginHandler");
				if (f != null) {
					User user = userDao.getUserByUsername(f.getLoginName());
					if (user == null) {
						log.error("Unable to retrieve user " + f.getLoginName()
								+ " from model");
					} else {
						Authenticate.sendLoginNotifyEvent(
								new LogoutEvent(user.getURI()),
								getServletContext(), session);
					}
				}
				session.invalidate();
			}
			response.sendRedirect("./");
		} catch (Exception ex) {
			log.error(ex, ex);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}
}
