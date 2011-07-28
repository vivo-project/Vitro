/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.icu.text.SimpleDateFormat;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;

/**
 * Back door to log in as the root user.
 * 
 * If the classpath contains a file called friend.xml, which contains a magic
 * line (see below) with today's date, or some date less than a week ago, then
 * you are logged in as root.
 * 
 * If anything else, return a 404.
 */
public class FriendController extends HttpServlet {
	private static final Log log = LogFactory.getLog(FriendController.class);

	private static final long MILLIS_IN_A_WEEK = 7L * 24L * 60L * 60L * 1000L;

	// To be valid XML, it could look like this: <date value="2011-07-01" />
	// but we don't care as long as it contains this: 9999-99-99
	private static final String DATE_PATTERN = "\\d\\d\\d\\d-\\d\\d-\\d\\d";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			if (fileContentsAreAcceptable()) {
				writeWarningToTheLog(req);
				loginAsRootUser(req);
				redirectToHomePage(resp);
			}
		} catch (Exception e) {
			log.debug("problem: " + e.getMessage());
			resp.sendError(HttpStatus.SC_NOT_FOUND);
		}
	}

	private boolean fileContentsAreAcceptable() throws Exception {
		InputStream stream = null;
		try {
			stream = openTheFile();
			String string = readFromStream(stream);
			return checkDateString(string);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	private InputStream openTheFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader()
				.getResourceAsStream("/friend.xml");
		if (stream == null) {
			throw new Exception("can't find the file.");
		}
		return stream;
	}

	private String readFromStream(InputStream stream) throws IOException {
		byte[] buffer = new byte[1024];
		int howMany = stream.read(buffer);

		if (howMany == -1) {
			return "";
		} else {
			return new String(buffer, 0, howMany);
		}
	}

	private boolean checkDateString(String string) throws Exception {
		long date = parseDateFromFile(string);
		compareAgainstDateRange(date);
		return true;
	}

	private long parseDateFromFile(String string) throws Exception {
		Pattern p = Pattern.compile(DATE_PATTERN);
		Matcher m = p.matcher(string);

		if (!m.find()) {
			throw new Exception("no date string in the file.");
		}

		return new SimpleDateFormat("yyyy-MM-dd").parse(m.group()).getTime();
	}

	private void compareAgainstDateRange(long date) throws Exception {
		long now = new Date().getTime();
		long then = now - MILLIS_IN_A_WEEK;
		if ((date > now) || (date < then)) {
			throw new Exception("date out of range.");
		}
	}

	private void writeWarningToTheLog(HttpServletRequest req) {
		log.warn("LOGGING IN VIA SESAME FROM ADDR=" + req.getRemoteAddr()
				+ ", PORT=" + req.getRemotePort() + ", HOST="
				+ req.getRemoteHost() + ", USER=" + req.getRemoteUser());
	}

	private void loginAsRootUser(HttpServletRequest req) throws Exception {
		UserAccount rootUser = getRootUser(req);
		Authenticator.getInstance(req).recordLoginAgainstUserAccount(rootUser,
				AuthenticationSource.INTERNAL);
	}

	private UserAccount getRootUser(HttpServletRequest req) throws Exception {
		UserAccountsDao uaDao = new VitroRequest(req).getWebappDaoFactory()
				.getUserAccountsDao();

		for (UserAccount ua : uaDao.getAllUserAccounts()) {
			if (ua.isRootUser()) {
				return ua;
			}
		}

		throw new Exception("couldn't find root user.");
	}

	private void redirectToHomePage(HttpServletResponse resp)
			throws IOException {
		resp.sendRedirect(UrlBuilder.getUrl("/"));
	}

}
