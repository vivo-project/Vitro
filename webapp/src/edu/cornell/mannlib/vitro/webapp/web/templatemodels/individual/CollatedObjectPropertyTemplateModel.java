/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview.InvalidConfigurationException;

public class CollatedObjectPropertyTemplateModel extends
		ObjectPropertyTemplateModel {

	private static final Log log = LogFactory
			.getLog(CollatedObjectPropertyTemplateModel.class);
	private static final String SUBCLASS_VARIABLE_NAME = "subclass";

	private static final Pattern SELECT_SUBCLASS_PATTERN =
	// SELECT ?subclass
	Pattern.compile("SELECT[^{]*\\?" + SUBCLASS_VARIABLE_NAME + "\\b",
			Pattern.CASE_INSENSITIVE);
	// ORDER BY ?subclass
	// ORDER BY DESC(?subclass)
	private static final Pattern ORDER_BY_SUBCLASS_PATTERN = Pattern.compile(
			"ORDER\\s+BY\\s+(DESC\\s*\\(\\s*)?\\?" + SUBCLASS_VARIABLE_NAME,
			Pattern.CASE_INSENSITIVE);

	private final List<SubclassTemplateModel> subclasses;
	private final VClassDao vclassDao;

	CollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject,
			VitroRequest vreq, boolean editing,
			List<ObjectProperty> populatedObjectPropertyList)
			throws InvalidConfigurationException {

		super(op, subject, vreq, editing);

		vclassDao = vreq.getWebappDaoFactory().getVClassDao();

		if (populatedObjectPropertyList.contains(op)) {
			log.debug("Getting data for populated object property "
					+ op.getURI());

			/* Get the data */
			List<Map<String, String>> statementData = getStatementData();

			/* Apply post-processing */
			postprocess(statementData);

			/* Collate the data */log.debug("Collate:collating subclasses for "
					+ subjectUri + " - predicate= " + op.getURI());
			subclasses = collate(subjectUri, op, statementData, editing);

			for (SubclassTemplateModel subclass : subclasses) {
				List<ObjectPropertyStatementTemplateModel> list = subclass
						.getStatements();
				postprocessStatementList(list);
			}

			Collections.sort(subclasses);

		} else {
			log.debug("Object property " + getUri() + " is unpopulated.");
			subclasses = new ArrayList<SubclassTemplateModel>();
		}
	}

	@Override
	protected boolean isEmpty() {
		return subclasses.isEmpty();
	}

	public ConfigError checkQuery(String queryString) {

		if (StringUtils.isBlank(queryString)) {
			return ConfigError.NO_SELECT_QUERY;
		}

		Matcher m;
		m = SELECT_SUBCLASS_PATTERN.matcher(queryString);
		if (!m.find()) {
			return ConfigError.NO_SUBCLASS_SELECT;
		}

		m = ORDER_BY_SUBCLASS_PATTERN.matcher(queryString);
		if (!m.find()) {
			return ConfigError.NO_SUBCLASS_ORDER_BY;
		}

		return null;
	}

	@Override
	protected void removeDuplicates(List<Map<String, String>> data) {
		filterSubclasses(data);
		super.removeDuplicates(data);
	}

	/*
	 * The query returns subclasses of a specific superclass that the object
	 * belongs to; for example, in the case of authorInAuthorship, subclasses of
	 * core:InformationResource. Here we remove all but the most specific
	 * subclass for the object.
	 */
	private void filterSubclasses(List<Map<String, String>> statementData) {
		String objectVariableName = getObjectKey();
		if (objectVariableName == null) {
			log.error("Cannot remove duplicate statements for property "
					+ getUri() + " because no object found to dedupe.");
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Data before subclass filtering");
			logData(statementData);
		}

		// Compile a list of the statements with most specific subclasses;
		// others will be removed
		List<Map<String, String>> filteredList = new ArrayList<Map<String, String>>();
		Set<String> processedObjects = new HashSet<String>();
		for (Map<String, String> outerMap : statementData) {

			String objectUri = outerMap.get(objectVariableName);

			if (processedObjects.contains(objectUri)) {
				continue;
			}
			processedObjects.add(objectUri);
			List<Map<String, String>> dataForThisObject = new ArrayList<Map<String, String>>();
			//Retrieve the statements that are related to this specific object
			for (Map<String, String> innerMap : statementData) {
				if (innerMap.get(objectVariableName).equals(objectUri)) {
					// Subclass should at this point contain the most specific
					// type already
					dataForThisObject.add(innerMap);
				}
			}
			
			if(log.isDebugEnabled()) {
				log.debug("Object URI " + objectUri + " has number of statements = " + dataForThisObject.size());
			}
			//Subclass variable should already reflect most specifick types but there may be more than one most specific type
			Collections.sort(dataForThisObject, new DataComparatorBySubclass());
			filteredList.add(dataForThisObject.get(0));

		}

		statementData.retainAll(filteredList);

		if (log.isDebugEnabled()) {
			log.debug("Data after subclass filtering");
			logData(statementData);
		}
	}

	//Subclass variable should already contain most specific type
	//If there is more than one most specific type, then order alphabetically
	private class DataComparatorBySubclass implements
			Comparator<Map<String, String>> {

		@Override
		public int compare(Map<String, String> map1, Map<String, String> map2) {

			String subclass1 = map1.get(SUBCLASS_VARIABLE_NAME);
			String subclass2 = map2.get(SUBCLASS_VARIABLE_NAME);

			return subclass1.compareTo(subclass2);

		}
	}

	

	// Collate the statements by subclass.
	private List<SubclassTemplateModel> collate(String subjectUri,
			ObjectProperty property, List<Map<String, String>> statementData,
			boolean editing) {

		String objectKey = getObjectKey();

		List<SubclassTemplateModel> subclasses = new ArrayList<SubclassTemplateModel>();

		for (Map<String, String> map : statementData) {

			String subclassUri = map.get(SUBCLASS_VARIABLE_NAME);

			VClass vclass = vclassDao.getVClassByURI(subclassUri);

			List<ObjectPropertyStatementTemplateModel> listForThisSubclass = null;

			for (SubclassTemplateModel subclass : subclasses) {
				VClass subclassVClass = subclass.getVClass();
				if ((vclass == null && subclassVClass == null)
						|| (vclass != null && vclass.equals(subclassVClass))) {
					listForThisSubclass = subclass.getStatements();
					break;
				}
			}

			if (listForThisSubclass == null) {
				listForThisSubclass = new ArrayList<ObjectPropertyStatementTemplateModel>();
				subclasses.add(new SubclassTemplateModel(vclass,
						listForThisSubclass));
			}

			listForThisSubclass.add(new ObjectPropertyStatementTemplateModel(
					subjectUri, property, objectKey, map, getTemplateName(),
					vreq));

		}

		return subclasses;
	}

	// class SubclassComparatorByDisplayRank implements Comparator<String> {
	//
	// private List<VClass> vclasses;
	//
	// SubclassComparatorByDisplayRank(List<VClass> vclasses) {
	// this.vclasses = vclasses;
	// }
	//
	// @Override
	// public int compare(String nameLeft, String nameRight) {
	//
	// if (StringUtils.isBlank(uriLeft)) {
	// return StringUtils.isBlank(uriRight) ? 0 : 1;
	// }
	//
	// VClass vclassLeft = vclassDao.getVClassByURI(uriLeft);
	// VClass vclassRight = vclassDao.getVClassByURI(uriRight);
	//
	// if (vclassLeft == null) {
	// return vclassRight == null ? 0 : 1;
	// }
	//
	// int rankLeft = vclassLeft.getDisplayRank();
	// int rankRight = vclassRight.getDisplayRank();
	//
	// int intCompare = 0;
	//
	// // Values < 1 are undefined and go at end, not beginning
	// if (rankLeft < 1) {
	// intCompare = rankRight < 1 ? 0 : 1;
	// } else if (rankRight < 1) {
	// intCompare = -1;
	// } else {
	// intCompare = ((Integer)rankLeft).compareTo(rankRight);
	// }
	//
	// if (intCompare != 0) {
	// return intCompare;
	// }
	//
	// // If display ranks are equal, sort by name
	// if (nameLeft == null) {
	// return nameRight == null ? 0 : 1;
	// }
	// if (nameRight == null) {
	// return -1;
	// }
	// return nameLeft.compareToIgnoreCase(nameRight);
	//
	// }
	// }

	// private String getSubclassName(String subclassUri) {
	// if (subclassUri.isEmpty()) {
	// return "";
	// }
	// VClass vclass = vclassDao.getVClassByURI(subclassUri);
	// return vclass != null ? vclass.getName() : "";
	// }

	/* Template properties */

	public List<SubclassTemplateModel> getSubclasses() {
		return subclasses;
	}

	@Override
	public boolean isCollatedBySubclass() {
		return true;
	}
}
