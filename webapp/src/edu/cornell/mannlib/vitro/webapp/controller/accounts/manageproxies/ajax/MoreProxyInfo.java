/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;

/**
 * Get more information (class label and image URL) about a selected proxy.
 * 
 * If there is no image URL, just omit it from the result. The proxy already has
 * a placeholder image.
 */
public class MoreProxyInfo extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(MoreProxyInfo.class);

	private static final String PARAMETER_PROXY_URI = "uri";

	private final ObjectPropertyStatementDao opsDao;

	private final String proxyUri;

	public MoreProxyInfo(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		super(servlet, vreq, resp);
		opsDao = vreq.getWebappDaoFactory().getObjectPropertyStatementDao();

		proxyUri = getStringParameter(PARAMETER_PROXY_URI, "");
	}

	@Override
	public String prepareResponse() throws IOException, JSONException {
		log.debug("proxy URI is '" + proxyUri + "'");
		if (proxyUri.isEmpty()) {
			return EMPTY_RESPONSE;
		}

		UserAccount user = uaDao.getUserAccountByUri(proxyUri);
		if (user == null) {
			log.debug("no such user");
			return EMPTY_RESPONSE;
		}

		List<Individual> inds = SelfEditingConfiguration.getBean(vreq)
				.getAssociatedIndividuals(indDao, user);
		if (inds.isEmpty()) {
			log.debug("no profile");
			return EMPTY_RESPONSE;
		}
		Individual profileInd = inds.get(0);

		Map<String, String> map = new HashMap<String, String>();
		String imagePath = profileInd.getThumbUrl();
		if ((imagePath != null) && (!imagePath.isEmpty())) {
			map.put("imageUrl", UrlBuilder.getUrl(imagePath));
		}
		map.put("classLabel", getMostSpecificTypeLabel(profileInd.getURI()));

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(map);
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

}
