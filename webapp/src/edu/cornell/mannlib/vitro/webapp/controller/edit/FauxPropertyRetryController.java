/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionListener;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
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

		// create an EditProcessObject for this and put it in the session
		EditProcessObject epo = super.createEpo(req);

		// Populate it.
		EpoPopulator populator = new EpoPopulator(req, epo);
		populator.populate();

		req.setAttribute("bodyJsp", "/templates/edit/formBasic.jsp");
		req.setAttribute("colspan", "5");
		req.setAttribute("formJsp",
				"/templates/edit/specific/fauxProperty_retry.jsp");
		req.setAttribute("scripts", "/templates/edit/formBasic.js");
		req.setAttribute("title", "Faux Property Editing Form");
		req.setAttribute("_action", epo.getAction());
		setRequestAttributes(req, epo);

		try {
			RequestDispatcher rd = req
					.getRequestDispatcher(Controllers.BASIC_JSP);
			rd.forward(req, response);
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

	private static class EpoPopulator {
		private final VitroRequest req;
		private final ServletContext ctx;
		private final EditProcessObject epo;

		private final FauxPropertyDao fpDao;

		private FauxProperty beanForEditing;
		private Property baseProperty;

		EpoPopulator(HttpServletRequest req, EditProcessObject epo) {
			this.req = new VitroRequest(req);
			this.ctx = req.getSession().getServletContext();

			this.epo = epo;

			this.fpDao = ModelAccess.on(ctx).getWebappDaoFactory()
					.getFauxPropertyDao();

		}

		void populate() {
			epo.setDataAccessObject(fpDao);
			epo.setAction(determineAction());

			if (epo.getUseRecycledBean()) {
				beanForEditing = (FauxProperty) epo.getNewBean();
			} else {
				beanForEditing = locateBeanForEditing();
				epo.setOriginalBean(beanForEditing);
			}

			this.baseProperty = req.getUnfilteredWebappDaoFactory()
					.getObjectPropertyDao()
					.getObjectPropertyByURI(beanForEditing.getURI());

			doABunchOfOtherJunk();
		}

		private String determineAction() {
			return (req.getParameter("create") == null) ? "update" : "insert";
		}

		private FauxProperty locateBeanForEditing() {
			String baseUri = req.getParameter("baseUri");
			String rangeUri = req.getParameter("rangeUri");
			String domainUri = req.getParameter("domainUri");

			if (epo.getAction().equals("insert")) {
				return new FauxProperty(null, baseUri, null);
			}

			FauxProperty bean = fpDao.getFauxPropertyByUris(domainUri, baseUri,
					rangeUri);
			if (bean == null) {
				throw new IllegalArgumentException(
						"FauxProperty does not exist for <" + domainUri
								+ "> ==> <" + baseUri + "> ==> <" + rangeUri
								+ ">");
			}
			return bean;
		}

		private void doABunchOfOtherJunk() {
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

			foo.setOptionLists(new HashMap<>(createOptionsMap()));

			// We will need to set a lot of option lists and stuff.
			// TODO

			// Put attributes on the request so the JSP can populate the fields.
			// request.setAttribute("transitive",propertyForEditing.getTransitive());
			// request.setAttribute("objectIndividualSortPropertyURI",
			// propertyForEditing.getObjectIndividualSortPropertyURI());
			// TODO

			// checkboxes are pretty annoying : we don't know if someone
			// *unchecked*
			// a box, so we have to default to false on updates.
			// propertyForEditing.setSymmetric(false);
			// TODO

			epo.setFormObject(foo);

			FormUtils.populateFormFromBean(beanForEditing, epo.getAction(),
					foo, epo.getBadValueMap());
		}

		private Map<String, List<Option>> createOptionsMap() {
			Map<String, List<Option>> map = new HashMap<>();
			map.put("DomainVClassURI",
					createRootedVClassOptionList(
							baseProperty.getDomainVClassURI(),
							beanForEditing.getDomainURI()));
			map.put("RangeVClassURI",
					createRootedVClassOptionList(
							baseProperty.getRangeVClassURI(),
							beanForEditing.getRangeURI()));
			return map;
		}

		private List<Option> createRootedVClassOptionList(String rootVClassUri,
				String currentSelection) {
			WebappDaoFactory wadf = req.getUnfilteredWebappDaoFactory();

			List<Option> list = new ArrayList<>();
			list.add(new Option("", "(none specified)"));

			if (rootVClassUri == null) {
				list.addAll(FormUtils.makeVClassOptionList(wadf,
						currentSelection));
			} else {
				list.addAll(FormUtils.makeOptionListOfSubVClasses(wadf,
						rootVClassUri, currentSelection));
			}

			return list;
		}

	}

}
