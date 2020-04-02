/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;

/**
 * Does a Solr search for individuals, and uses the short view to render each of
 * the results.
 */
public class GetRandomSearchIndividualsByVClass extends GetSearchIndividualsByVClass {
	private static final Log log = LogFactory
			.getLog(GetRandomSearchIndividualsByVClass.class);

	protected GetRandomSearchIndividualsByVClass(VitroRequest vreq) {
		super(vreq);
	}

	/**
	 * Search for individuals by VClass.
	 */
	@Override
	protected ObjectNode process() throws Exception {
		ObjectNode rObj = null;

		//This gets the first vclass value and sets that as display type.
		List<String> vclassIds = super.getVclassIds(vreq);
		String vclassId = vclassIds.get(0);
		vreq.setAttribute("queryType", "random");
//		vreq.setAttribute("displayType", vclassId);

		//This will get all the solr individuals by VClass (if one value) or the intersection
		//i.e. individuals that have all the types for the different vclasses entered
		rObj = super.process();
		addShortViewRenderings(rObj);
		return rObj;
	}

	/**
	 * Look through the return object. For each individual, render the short
	 * view and insert the resulting HTML into the object.
	 */
	private void addShortViewRenderings(ObjectNode rObj) {
		ArrayNode individuals = (ArrayNode) rObj.get("individuals");
		String vclassName = rObj.get("vclass").get("name").asText();
		for (int i = 0; i < individuals.size(); i++) {
			ObjectNode individual = (ObjectNode) individuals.get(i);
			individual.put("shortViewHtml",
					renderShortView(individual.get("URI").asText(), vclassName));
		}
	}

	private String renderShortView(String individualUri, String vclassName) {
		IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
		Individual individual = iDao.getIndividualByURI(individualUri);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("individual",
				IndividualTemplateModelBuilder.build(individual, vreq));
		modelMap.put("vclass", vclassName);
		String langCtx = vreq.getLocale().getLanguage() + "-"+vreq.getLocale().getCountry();  //UQAM-Linguistic-Management build the linguistic context
		modelMap.put("langCtx", langCtx); // UQAM-Linguistic-Management add the linguistic context to map
		ShortViewService svs = ShortViewServiceSetup.getService(ctx);
		return svs.renderShortView(individual, ShortViewContext.BROWSE,
				modelMap, vreq);
	}
}
