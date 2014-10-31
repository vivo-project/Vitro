/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Collections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionListener;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

/**
 * TODO
 */
public class FauxPropertyRetryController extends BaseEditController {
	private static final Log log = LogFactory
			.getLog(FauxPropertyRetryController.class);
	
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse response) {
		if (!isAuthorizedToDisplayPage(req, response,
				SimplePermission.EDIT_ONTOLOGY.ACTION)) {
			return;
		}

		VitroRequest request = new VitroRequest(req);

		// create an EditProcessObject for this and put it in the session
		EditProcessObject epo = super.createEpo(request);

		ServletContext ctx = getServletContext();

		FauxPropertyDao fpDao = ModelAccess.on(ctx).getWebappDaoFactory()
				.getFauxPropertyDao();
		epo.setDataAccessObject(fpDao);

		FauxProperty fpForEditing = null;
		if (epo.getUseRecycledBean()) {
			fpForEditing = (FauxProperty) epo.getNewBean();
		} else {
			String create = request.getParameter("create");
			String baseUri = request.getParameter("baseUri");
			String rangeUri = request.getParameter("rangeUri");
			String domainUri = request.getParameter("domainUri");
			if (create != null) {
				fpForEditing = new FauxProperty(null, baseUri, null);
				epo.setAction("insert");
			} else {
				fpForEditing = fpDao.getFauxPropertyByUris(domainUri, baseUri,
						rangeUri);
				if (fpForEditing == null) {
					throw new IllegalArgumentException(
							"FauxProperty does not exist for <" + domainUri
									+ "> ==> <" + baseUri + "> ==> <"
									+ rangeUri + ">");
				}
				epo.setAction("update");
			}
			epo.setOriginalBean(fpForEditing);
		}

		// set any validators
		// TODO NONE YET

		// set up any listeners
		epo.setChangeListenerList(Collections
				.singletonList(new PropertyRestrictionListener(ctx)));

		// where should the postinsert pageforwarder go?
		// TODO
		// make a postdelete pageforwarder that will send us to the control
		// panel for the base property.
		// TODO

		FormObject foo = new FormObject();
		foo.setErrorMap(epo.getErrMsgMap());

		// We will need to set a lot of option lists and stuff.
		// TODO

		// Put attributes on the request so the JSP can populate the fields.
		// request.setAttribute("transitive",propertyForEditing.getTransitive());
		// request.setAttribute("objectIndividualSortPropertyURI",
		// propertyForEditing.getObjectIndividualSortPropertyURI());
		// TODO

		// checkboxes are pretty annoying : we don't know if someone *unchecked*
		// a box, so we have to default to false on updates.
		// propertyForEditing.setSymmetric(false);
		// TODO

		epo.setFormObject(foo);

		FormUtils.populateFormFromBean(fpForEditing, epo.getAction(), foo,
				epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("colspan","5");
        request.setAttribute("formJsp","/templates/edit/specific/fauxProperty_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Faux Property Editing Form");
        request.setAttribute("_action",epo.getAction());
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("Could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

}
