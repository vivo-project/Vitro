/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasAssociatedIndividual;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Show a summary of who is logged in and how they are to be treated by the
 * authorization system.
 */
public class ShowAuthController extends FreemarkerHttpServlet {

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return Actions.AUTHORIZED;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {

		Map<String, Object> body = new HashMap<String, Object>();

		body.put("identifiers", RequestIdentifiers.getIdBundleForRequest(vreq));
		body.put("currentUser", LoginStatusBean.getCurrentUser(vreq));
		body.put("associatedIndividuals", getAssociatedIndividuals(vreq));
		body.put("factories", getIdentifierFactoryNames(vreq));
		body.put("policies", ServletPolicyList.getPolicies(vreq));
		body.put("matchingProperty", getMatchingProperty(vreq));

		return new TemplateResponseValues("admin-showAuth.ftl", body);
	}

	private List<String> getIdentifierFactoryNames(VitroRequest vreq) {
		ServletContext ctx = vreq.getSession().getServletContext();
		return ActiveIdentifierBundleFactories.getFactoryNames(ctx);
	}

	private String getMatchingProperty(VitroRequest vreq) {
		return ConfigurationProperties.getBean(vreq).getProperty(
				"selfEditing.idMatchingProperty", "");
	}

	private List<AssociatedIndividual> getAssociatedIndividuals(
			VitroRequest vreq) {
		List<AssociatedIndividual> list = new ArrayList<AssociatedIndividual>();
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(vreq);
		for (String uri : HasAssociatedIndividual.getIndividualUris(ids)) {
			list.add(new AssociatedIndividual(uri, mayEditIndividual(vreq, uri)));
		}
		return list;
	}

	/**
	 * Is the current user authorized to edit an arbitrary object property on
	 * this individual?
	 */
	private boolean mayEditIndividual(VitroRequest vreq, String individualUri) {
		RequestedAction action = new EditObjPropStmt(individualUri,
				RequestActionConstants.SOME_URI,
				RequestActionConstants.SOME_URI);
		return PolicyHelper.isAuthorizedForActions(vreq, action);
	}

	public class AssociatedIndividual {
		private final String uri;
		private final boolean editable;

		public AssociatedIndividual(String uri, boolean editable) {
			this.uri = uri;
			this.editable = editable;
		}

		public String getUri() {
			return uri;
		}

		public boolean isEditable() {
			return editable;
		}

	}
}
