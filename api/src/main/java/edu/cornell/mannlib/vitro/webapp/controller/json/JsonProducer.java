/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * A base for the classes that produce JSON results. Contains some useful constants and convenience methods.
 */
public abstract class JsonProducer {
	private static final Log log = LogFactory.getLog(JsonProducer.class);

	/**
	 * Process a list of Individuals into a JSON array that holds the Names and URIs.
	 */
    protected ArrayNode individualsToJson(List<Individual> individuals) throws ServletException {
		ArrayNode ja = JsonNodeFactory.instance.arrayNode();
		for (Individual ent: individuals) {
			ObjectNode entJ = JsonNodeFactory.instance.objectNode();
			entJ.put("name", ent.getName());
			entJ.put("URI", ent.getURI());
			ja.add( entJ );
		}
		return ja;
    }

	/**
	 * Get the "vclassId" parameter from the request and instantiate the VClass.
	 *
	 * There must be one, and it must be valid.
	 */
	protected VClass getVclassParameter(VitroRequest vreq) {
		String vclassId = vreq.getParameter("vclassId");
		if (StringUtils.isEmpty(vclassId)) {
			log.error("parameter vclassId expected but not found");
			throw new IllegalStateException("parameter vclassId expected ");
		}
		return instantiateVclass(vclassId, vreq);
	}

	/**
	 * Get one or more "vclassId" parameters from the request. Confirm that
	 * there is at least one, and that all are valid.
	 *
	 * Return value is never null and never empty.
	 */
	protected List<String> getVclassIds(VitroRequest vreq) {
		String[] vclassIds = vreq.getParameterValues("vclassId");
		if ((vclassIds == null) || (vclassIds.length == 0)) {
			log.error("parameter vclassId expected but not found");
			throw new IllegalStateException("parameter vclassId expected ");
		}

		for (String vclassId : vclassIds) {
			instantiateVclass(vclassId, vreq);
		}

		return Arrays.asList(vclassIds);
	}

	private VClass instantiateVclass(String uri, VitroRequest vreq) {
		VClass vclass = vreq.getWebappDaoFactory().getVClassDao()
				.getVClassByURI(uri);
		if (vclass == null) {
			log.error("Couldn't retrieve vclass '" + uri + "'");
			throw new IllegalStateException("Class " + uri + " not found");
		}
		return vclass;
	}

}
