/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.POLICY_NEUTRAL;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.impl.RequiredFieldValidator;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionListener;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.utils.RoleLevelOptionsSetup;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
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

		req.setAttribute("colspan", "5");
		req.setAttribute("formJsp",
				"/templates/edit/specific/fauxProperty_retry.jsp");
		req.setAttribute("scripts", "/templates/edit/formBasic.js");
		req.setAttribute("title", "Faux Property Editing Form");
		req.setAttribute("_action", epo.getAction());
		setRequestAttributes(req, epo);

		try {
			JSPPageHandler.renderBasicPage(req, response, "/templates/edit/formBasic.jsp");
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
		private final WebappDaoFactory wadf;

		private final EditProcessObject epo;

		private final FauxPropertyDao fpDao;

		private FauxProperty beanForEditing;
		private Property baseProperty;

		EpoPopulator(HttpServletRequest req, EditProcessObject epo) {
			this.req = new VitroRequest(req);
			this.ctx = req.getSession().getServletContext();
			this.wadf = ModelAccess.on(req).getWebappDaoFactory(POLICY_NEUTRAL);

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

			addCheckboxValuesToTheRequest();

			setFieldValidators();
			setListeners();
			setForwarders();

			assembleFormObjectAndConnectToEpo();
		}

		private String determineAction() {
			return (req.getParameter("create") == null) ? "update" : "insert";
		}

		private FauxProperty locateBeanForEditing() {
			String baseUri = req.getParameter("baseUri");
			String rangeUri = req.getParameter("rangeUri");
			String domainUri = req.getParameter("domainUri");

			if (epo.getAction().equals("insert")) {
				return newFauxProperty(baseUri);
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

		/**
		 * Create a new FauxProperty object and let it inherit some values from
		 * its base property.
		 */
		private FauxProperty newFauxProperty(String baseUri) {
			FauxProperty fp = new FauxProperty(null, baseUri, null);
			ObjectPropertyDao opDao = wadf.getObjectPropertyDao();
			ObjectProperty base = opDao.getObjectPropertyByURI(baseUri);
			fp.setGroupURI(base.getGroupURI());
			fp.setRangeURI(base.getRangeVClassURI());
			fp.setDomainURI(base.getDomainVClassURI());
			fp.setHiddenFromDisplayBelowRoleLevel(base
					.getHiddenFromDisplayBelowRoleLevel());
			fp.setHiddenFromPublishBelowRoleLevel(base
					.getHiddenFromPublishBelowRoleLevel());
			fp.setProhibitedFromUpdateBelowRoleLevel(base
					.getProhibitedFromUpdateBelowRoleLevel());
			fp.setCustomEntryForm(base.getCustomEntryForm());
			log.debug("Created new FauxProperty: " + fp);
			return fp;
		}

		private void addCheckboxValuesToTheRequest() {
			req.setAttribute("selectFromExisting",
					beanForEditing.isSelectFromExisting());
			req.setAttribute("offerCreateNewOption",
					beanForEditing.isOfferCreateNewOption());
			req.setAttribute("collateBySubclass",
					beanForEditing.isCollateBySubclass());

			// checkboxes on HTML forms are pretty annoying : we don't know if
			// someone *unchecked* a box, so we have to default to false on
			// updates.
			if (beanForEditing.getURI() != null) {
				beanForEditing.setSelectFromExisting(false);
				beanForEditing.setOfferCreateNewOption(false);
				beanForEditing.setCollateBySubclass(false);
			}
		}

		private void setFieldValidators() {
			epo.getValidatorMap()
					.put("RangeURI",
							Arrays.asList(new Validator[] { new RequiredFieldValidator() }));
		}

		private void setListeners() {
			epo.setChangeListenerList(Collections
					.singletonList(new PropertyRestrictionListener()));
		}

		private void setForwarders() {
			// where should the postinsert pageforwarder go?
			// TODO
			// make a postdelete pageforwarder that will send us to the control
			// panel for the base property.
			// TODO
		}

		private void assembleFormObjectAndConnectToEpo() {
			FormObject foo = new FormObject();
			foo.setErrorMap(epo.getErrMsgMap());
			foo.setOptionLists(new HashMap<>(createOptionsMap()));
			epo.setFormObject(foo);
			FormUtils.populateFormFromBean(beanForEditing, epo.getAction(),
					foo, epo.getBadValueMap());
		}

		private Map<String, List<Option>> createOptionsMap() {
			Map<String, List<Option>> map = new HashMap<>();
			map.put("GroupURI", createClassGroupOptionList());
			map.put("DomainURI", buildDomainOptionList());
			map.put("RangeURI", buildRangeOptionList());
			map.put("HiddenFromDisplayBelowRoleLevelUsingRoleUri",
					RoleLevelOptionsSetup.getDisplayOptionsList(beanForEditing));
			map.put("ProhibitedFromUpdateBelowRoleLevelUsingRoleUri",
					RoleLevelOptionsSetup.getUpdateOptionsList(beanForEditing));
			map.put("HiddenFromPublishBelowRoleLevelUsingRoleUri",
					RoleLevelOptionsSetup.getPublishOptionsList(beanForEditing));
			return map;
		}

		private List<Option> createClassGroupOptionList() {
			List<Option> groupOptList = getGroupOptList(beanForEditing
					.getGroupURI());
			Collections.sort(groupOptList,
					new OptionsBodyComparator(req.getCollator()));
			groupOptList.add(0, new Option("", "none"));
			return groupOptList;
		}

		private List<Option> getGroupOptList(String currentGroupURI) {
			List<PropertyGroup> groups = wadf.getPropertyGroupDao()
					.getPublicGroups(true);
			if (currentGroupURI == null) {
				return FormUtils.makeOptionListFromBeans(groups, "URI", "Name",
						"", null, false);

			} else {
				return FormUtils.makeOptionListFromBeans(groups, "URI", "Name",
						currentGroupURI, null, true);
			}
		}

		private List<Option> buildDomainOptionList() {
			List<Option> list = new ArrayList<>();
			if (baseProperty.getDomainVClassURI() == null) {
				list.addAll(FormUtils.makeVClassOptionList(wadf,
						beanForEditing.getDomainURI()));
			} else {
				list.addAll(FormUtils.makeOptionListOfSubVClasses(wadf,
						baseProperty.getDomainVClassURI(),
						beanForEditing.getDomainURI()));
			}
			list.add(0, new Option("", "(none specified)"));
			return list;
		}

		private List<Option> buildRangeOptionList() {
			List<Option> list = new ArrayList<>();
			if (baseProperty.getRangeVClassURI() == null) {
				list.addAll(FormUtils.makeVClassOptionList(wadf,
						beanForEditing.getRangeURI()));
			} else {
				list.addAll(FormUtils.makeOptionListOfSubVClasses(wadf,
						baseProperty.getRangeVClassURI(),
						beanForEditing.getRangeURI()));
				if (containsVCardKind(list)) {
					mergeInAllVCardClasses(list);
				}
			}
			list.add(0, new Option("", "(none specified)"));
			return list;
		}

		private static final String VCARD_KIND_URI = "http://www.w3.org/2006/vcard/ns#Kind";
		private static final String VCARD_NAMESPACE = "http://www.w3.org/2006/vcard/ns#";

		private boolean containsVCardKind(List<Option> list) {
			for (Option option : list) {
				if (VCARD_KIND_URI.equals(option.getValue())) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Add to the list any class that is in the vCard namespace and is not
		 * already in the list. Sort the list.
		 */
		private void mergeInAllVCardClasses(List<Option> list) {
			Set<String> existingUrls = new HashSet<>();
			for (Option option : list) {
				existingUrls.add(option.getValue());
			}
			for (Option option : FormUtils.makeVClassOptionList(wadf,
					beanForEditing.getRangeURI())) {
				if (option.getValue().startsWith(VCARD_NAMESPACE)
						&& !existingUrls.contains(option.getValue())) {
					list.add(option);
				}
			}
			Collections.sort(list, new Comparator<Option>() {
				@Override
				public int compare(Option o1, Option o2) {
					return o1.getBody().compareTo(o2.getBody());
				}
			});
		}

		private static class OptionsBodyComparator implements
				Comparator<Option> {
			private final Collator collator;

			public OptionsBodyComparator(Collator collator) {
				this.collator = collator;
			}

			@Override
			public int compare(Option o1, Option o2) {
				return collator.compare(o1.getBody(), o2.getBody());
			}
		}

	}

}
