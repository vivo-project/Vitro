package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.*;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.MLevel;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

@WebServlet(name = "robot_authenticate", urlPatterns = {"/robot_authenticate"})
public class RobotAuthenticate extends Authenticate {
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		VitroRequest vreq = new VitroRequest(request);

		try {
			if (loginProcessIsRestarting(vreq)) {
				LoginProcessBean.removeBean(vreq);
			}
			if (loginProcessPagesAreEmpty(vreq)) {
				recordLoginProcessPages(vreq);
			}

			// Where do we stand in the process?
			State entryState = getCurrentLoginState(vreq);
			dumpStateToLog("entry", entryState, vreq);

			// Act on any input.
			switch (entryState) {
			case NOWHERE:
				processInputNowhere(vreq);
				break;
			case LOGGING_IN:
				processInputLoggingIn(vreq);
				break;
			case FORCED_PASSWORD_CHANGE:
				processInputChangePassword(vreq);
				break;
			default: // LOGGED_IN:
				processInputLoggedIn(vreq);
				break;
			}

			// Now where do we stand?
			State exitState = getCurrentLoginState(vreq);
			dumpStateToLog("exit", exitState, vreq);

			// Send them on their way.
			switch (exitState) {
			case NOWHERE:
				showLoginCanceled(response, vreq);
				break;
			case LOGGING_IN:
				LoginProcessBean bean = LoginProcessBean.getBean(vreq);
				Message message = bean.getMessage();
				if (MLevel.ERROR.equals(message.getMessageLevel())){
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message.getText());
				} else {
					showLoginScreen(vreq, response);
				}
				break;
			case FORCED_PASSWORD_CHANGE:
				showLoginScreen(vreq, response);
				break;
			default: // LOGGED_IN:
				showLoginComplete(response, vreq);
				break;
			}
		} catch (Exception e) {
			showSystemError(e, response);
		}
	}
}
