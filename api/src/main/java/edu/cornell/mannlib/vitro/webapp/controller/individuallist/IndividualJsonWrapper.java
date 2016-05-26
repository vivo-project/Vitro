/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individuallist;

import java.util.Collection;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

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

	static JSONObject packageIndividualAsJson(VitroRequest vreq, Individual ind)
			throws JSONException {
		// need an unfiltered dao to get firstnames and lastnames
		WebappDaoFactory fullWdf = vreq.getUnfilteredWebappDaoFactory();

		JSONObject jo = new JSONObject();
		jo.put("URI", ind.getURI());
		jo.put("label", ind.getRdfsLabel());
		jo.put("name", ind.getName());
		jo.put("thumbUrl", ind.getThumbUrl());
		jo.put("imageUrl", ind.getImageUrl());
		jo.put("profileUrl", UrlBuilder.getIndividualProfileUrl(ind, vreq));
		jo.put("mostSpecificTypes", getMostSpecificTypes(ind, fullWdf));
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
		public void add(JSONObject jo, VitroRequest vreq, Individual ind) throws JSONException;
	}
}
