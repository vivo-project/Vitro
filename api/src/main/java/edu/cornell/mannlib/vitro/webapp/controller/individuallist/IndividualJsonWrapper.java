/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.individuallist;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Wrap an Individual in a JSON object for display by the script.
 *
 * This will be overridden in VIVO so we can have more info in the display.
 */
public class IndividualJsonWrapper {
	private static AddJSONFields addJSONFields = null;

	public static void setAddJSONFields(AddJSONFields add) {
		addJSONFields = add;
	}

	static ObjectNode packageIndividualAsJson(VitroRequest vreq, Individual ind) {
		// need an unfiltered dao to get firstnames and lastnames
		WebappDaoFactory fullWdf = vreq.getUnfilteredWebappDaoFactory();

		ObjectNode jo = JsonNodeFactory.instance.objectNode();
		jo.put("URI", ind.getURI());
		jo.put("label", ind.getRdfsLabel());
		jo.put("name", ind.getName());
		jo.put("thumbUrl", ind.getThumbUrl());
		jo.put("imageUrl", ind.getImageUrl());
		jo.put("profileUrl", UrlBuilder.getIndividualProfileUrl(ind, vreq));
		ArrayNode ao = JsonNodeFactory.instance.arrayNode();
		for (String type : getMostSpecificTypes(ind, fullWdf)) {
			ao.add(type);
		}
		jo.put("mostSpecificTypes", ao);
		if (addJSONFields != null) {
			addJSONFields.add(jo, vreq, ind);
		}
		return jo;
	}

	public static Collection<String> getMostSpecificTypes(
			Individual individual, WebappDaoFactory wdf) {
		ObjectPropertyStatementDao opsDao = wdf.getObjectPropertyStatementDao();
		Map<String, String> mostSpecificTypes = opsDao
				.getMostSpecificTypesInClassgroupsForIndividual(individual
						.getURI());
		return mostSpecificTypes.values();
	}

	public interface AddJSONFields {
		public void add(ObjectNode jo, VitroRequest vreq, Individual ind);
	}
}
