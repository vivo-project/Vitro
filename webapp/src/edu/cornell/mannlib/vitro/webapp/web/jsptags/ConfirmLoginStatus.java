/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

/**
 * JSP tag to generate the HTML of links for edit, delete or add of a Property.
 * 
 * Maybe we should have a mode where it just sets a var to a map with "href" =
 * "edit/editDatapropDispatch.jsp?subjectUri=..." and "type" = "delete"
 * 
 * @author bdc34
 * 
 */
public class ConfirmLoginStatus extends BodyTagSupport {
	private static final Log log = LogFactory.getLog(ConfirmLoginStatus.class);

	int level = LoginStatusBean.NON_EDITOR;
	boolean allowSelfEditing;
	String beanAttributeName;

	public String getLevel() {
		return String.valueOf(level);
	}

	public void setLevel(String levelString) {
		if ("DBA".equals(levelString)) {
			this.level = LoginStatusBean.DBA;
		} else if ("CURATOR".equals(levelString)) {
			this.level = LoginStatusBean.CURATOR;
		} else if ("EDITOR".equals(levelString)) {
			this.level = LoginStatusBean.EDITOR;
		} else if ("NON_EDITOR".equals(levelString)) {
			this.level = LoginStatusBean.NON_EDITOR;
		} else {
			throw new IllegalArgumentException("Level attribute '"
					+ levelString + "' is not valid.");
		}
	}

	public void setAllowSelfEditing(boolean allowSelfEditing) {
		this.allowSelfEditing = allowSelfEditing;
	}

	public boolean getAllowSelfEditing() {
		return this.allowSelfEditing;
	}

	public String getBean() {
		return this.beanAttributeName;
	}

	public void setbean(String beanAttributeName) {
		this.beanAttributeName = beanAttributeName;
	}

	@Override
	public int doEndTag() throws JspException {
		LoginStatusBean loginBean = LoginStatusBean.getBean(getRequest());
		boolean isLoggedIn = loginBean.isLoggedInAtLeast(level);

		boolean isSelfEditing = VitroRequestPrep.isSelfEditing(getRequest());

		log.debug("loginLevel=" + loginBean.getSecurityLevel()
				+ ", requiredLevel=" + level + ", selfEditingAllowed="
				+ allowSelfEditing + ", isSelfEditing=" + isSelfEditing);

		if (isLoggedIn || (allowSelfEditing && isSelfEditing)) {
			log.debug("Login status confirmed.");
			return setBeanAndReturn(loginBean);
		} else {
			log.debug("Login status not confirmed.");
			return redirectAndSkipPage();
		}

	}

	private int setBeanAndReturn(LoginStatusBean loginBean) {
		if (beanAttributeName != null) {
			getRequest().setAttribute(beanAttributeName, loginBean);
		}
		return EVAL_PAGE;
	}

	private int redirectAndSkipPage() throws JspException {
		VitroHttpServlet.redirectToLoginPage(getRequest(), getResponse());
		return SKIP_PAGE;
	}

	private HttpServletRequest getRequest() {
		return ((HttpServletRequest) pageContext.getRequest());
	}

	private HttpServletResponse getResponse() {
		return (HttpServletResponse) pageContext.getResponse();
	}
}
