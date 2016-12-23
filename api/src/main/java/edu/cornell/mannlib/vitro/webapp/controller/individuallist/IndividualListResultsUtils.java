/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individuallist;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.PageRecord;

/**
 * Utility methods for procesing the paged results of a query for a list of Individuals.
 * 
 * Right now, there is only a method to wrap the results in Json.
 */
public class IndividualListResultsUtils {
	private static final Log log = LogFactory
			.getLog(IndividualListResultsUtils.class);

	/**
	 * Process results related to VClass or vclasses. Handles both single and
	 * multiple vclasses being sent.
	 */
	public static JSONObject wrapIndividualListResultsInJson(IndividualListResults results, VitroRequest vreq,
			boolean multipleVclasses) {
		JSONObject rObj = new JSONObject();

		if (log.isDebugEnabled()) {
			dumpParametersFromRequest(vreq);
		}

		try {
			List<VClass> vclasses = buildListOfRequestedVClasses(vreq);

			VClass vclass = null;
			// if single vclass expected, then include vclass. 
			// This relates to what the expected behavior is, not size of list
			if (!multipleVclasses) {
				vclass = vclasses.get(0);
				// currently used for ClassGroupPage
			} else {
				// For now, utilize very last VClass (assume that that is the one to be employed)
				// TODO: Find more general way of dealing with this: put multiple ones in?
				vclass = vclasses.get(vclasses.size() - 1);
				// rObj.put("vclasses", new JSONObject().put("URIs",vitroClassIdStr).put("name",vclass.getName()));
			}

			rObj.put("vclass", packageVClassAsJson(vclass));
			rObj.put("totalCount", results.getTotalCount());
			rObj.put("alpha", results.getAlpha());
			rObj.put("individuals",	packageIndividualsAsJson(vreq, results.getEntities()));
			rObj.put("pages", packagePageRecordsAsJson(results.getPages()));
			rObj.put("letters", packageLettersAsJson());
		} catch (Exception ex) {
			log.error("Error occurred in processing JSON object", ex);
		}
		return rObj;
	}

	private static List<VClass> buildListOfRequestedVClasses(VitroRequest vreq)
			throws Exception {
		String[] vitroClassIdStr = vreq.getParameterValues("vclassId");
		if (ArrayUtils.isEmpty(vitroClassIdStr)) {
			log.error("parameter vclassId URI parameter expected ");
			throw new Exception("parameter vclassId URI parameter expected ");
		}

		List<VClass> list = new ArrayList<>();
		for (String vclassId : vitroClassIdStr) {
			VClass vclass = vreq.getWebappDaoFactory().getVClassDao()
					.getVClassByURI(vclassId);
			if (vclass == null) {
				log.error("Couldn't retrieve vclass ");
				throw new Exception("Class " + vclassId + " not found");
			}
			list.add(vclass);
		}
		return list;
	}

	private static JSONObject packageVClassAsJson(VClass vclass)
			throws JSONException {
		JSONObject jvclass = new JSONObject();
		jvclass.put("URI", vclass.getURI());
		jvclass.put("name", vclass.getName());
		return jvclass;
	}

	private static JSONArray packageLettersAsJson() throws JSONException,
			UnsupportedEncodingException {
		List<String> letters = Controllers.getLetters();
		JSONArray jletters = new JSONArray();
		for (String s : letters) {
			JSONObject jo = new JSONObject();
			jo.put("text", s);
			jo.put("param", "alpha=" + URLEncoder.encode(s, "UTF-8"));
			jletters.put(jo);
		}
		return jletters;
	}

	private static JSONArray packagePageRecordsAsJson(List<PageRecord> pages)
			throws JSONException {
		JSONArray wpages = new JSONArray();
		for (PageRecord pr : pages) {
			JSONObject p = new JSONObject();
			p.put("text", pr.text);
			p.put("param", pr.param);
			p.put("index", pr.index);
			wpages.put(p);
		}
		return wpages;
	}

	private static JSONArray packageIndividualsAsJson(VitroRequest vreq,
			List<Individual> inds) throws JSONException {
		log.debug("Number of individuals returned from request: " + inds.size());

		JSONArray jInds = new JSONArray();
		for (Individual ind : inds) {
			jInds.put(IndividualJsonWrapper.packageIndividualAsJson(vreq, ind));
		}
		return jInds;
	}

	private static void dumpParametersFromRequest(VitroRequest vreq) {
		Map<String, String[]> pMap = vreq.getParameterMap();
		for (String name : pMap.keySet()) {
			for (String value : pMap.get(name)) {
				log.debug("value for " + name + ": '" + value + "'");
			}
		}
	}

}
