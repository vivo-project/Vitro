/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.web.images.PlaceholderUtil;

/**
 * Get more information (class label and image URL) about a selected proxy.
 */
public class MoreProfileInfo extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(MoreProfileInfo.class);

	private static final String PARAMETER_PROFILE_URI = "uri";

	private final ObjectPropertyStatementDao opsDao;

	private final String profileUri;

	public MoreProfileInfo(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		super(servlet, vreq, resp);
		opsDao = vreq.getWebappDaoFactory().getObjectPropertyStatementDao();

		profileUri = getStringParameter(PARAMETER_PROFILE_URI, "");
	}

	@Override
	public String prepareResponse() throws IOException {
		log.debug("profile URI is '" + profileUri + "'");
		if (profileUri.isEmpty()) {
			return EMPTY_RESPONSE;
		}

		Individual profileInd = indDao.getIndividualByURI(profileUri);
		if (profileInd == null) {
			log.debug("no such individual");
			return EMPTY_RESPONSE;
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("imageUrl", getFullImageUrl(profileInd));
		map.put("classLabel", getMostSpecificTypeLabel(profileInd.getURI()));

		ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
		ObjectNode jsonObj = JsonNodeFactory.instance.objectNode();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			jsonObj.put(entry.getKey(), entry.getValue());
		}
		jsonArray.add(jsonObj);
		String response = jsonArray.toString();

		log.debug("response is '" + response + "'");
		return response;
	}

	private String getMostSpecificTypeLabel(String uri) {
		Map<String, String> types = opsDao
				.getMostSpecificTypesInClassgroupsForIndividual(uri);
		if (types.isEmpty()) {
			return "";
		} else {
			return types.values().iterator().next();
		}
	}

	private String getFullImageUrl(Individual ind) {
		String path = ind.getThumbUrl();
		if ((path == null) || path.isEmpty()) {
			path = PlaceholderUtil.getPlaceholderImagePathForIndividual(vreq,
					ind.getURI());
		}
		return UrlBuilder.getUrl(path);
	}
}
