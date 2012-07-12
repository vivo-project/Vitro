/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;

/**
 * Does a Solr search for individuals, and uses the short view to render each of
 * the results.
 */
public class GetRenderedSolrIndividualsByVClass extends JsonObjectProducer {
	private static final Log log = LogFactory
			.getLog(GetRenderedSolrIndividualsByVClass.class);

	protected GetRenderedSolrIndividualsByVClass(VitroRequest vreq) {
		super(vreq);
	}

	/**
	 * Search for individuals by VClass. The class URI and the paging
	 * information are in the request parameters.
	 */
	@Override
	protected JSONObject process() throws Exception {
		JSONObject rObj = null;
		VClass vclass = getVclassParameter(vreq);
		String vclassId = vclass.getURI();

		vreq.setAttribute("displayType", vclassId);
		rObj = JsonServlet.getSolrIndividualsByVClass(vclassId, vreq, ctx);
		addShortViewRenderings(rObj);

		return rObj;
	}

	/**
	 * Look through the return object. For each individual, render the short
	 * view and insert the resulting HTML into the object.
	 */
	private void addShortViewRenderings(JSONObject rObj) throws JSONException {
		JSONArray individuals = rObj.getJSONArray("individuals");
		String vclassName = rObj.getJSONObject("vclass").getString("name");
		for (int i = 0; i < individuals.length(); i++) {
			JSONObject individual = individuals.getJSONObject(i);
			individual.put("shortViewHtml",
					renderShortView(individual.getString("URI"), vclassName));
		}
	}

	private String renderShortView(String individualUri, String vclassName) {
		IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
		Individual individual = iDao.getIndividualByURI(individualUri);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("individual",
				new IndividualTemplateModel(individual, vreq));
		modelMap.put("vclass", vclassName);

		ShortViewService svs = ShortViewServiceSetup.getService(ctx);
		return svs.renderShortView(individual, ShortViewContext.BROWSE,
				modelMap, vreq);
	}
}
