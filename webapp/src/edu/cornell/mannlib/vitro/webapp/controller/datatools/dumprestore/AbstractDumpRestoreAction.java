/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.BadRequestException;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

/**
 * Some utility methods that are common to the Action classes.
 */
abstract class AbstractDumpRestoreAction {
	protected final HttpServletRequest req;

	public AbstractDumpRestoreAction(HttpServletRequest req) {
		this.req = req;
	}

	protected RDFService getRdfService(WhichService which) {
		return RDFServiceUtils.getRDFService(new VitroRequest(req), which);
	}

	protected <T extends Enum<T>> T getEnumFromParameter(Class<T> enumClass,
			String key) throws BadRequestException {
		String valueString = req.getParameter(key);
		if (valueString == null) {
			throw new BadRequestException("Request has no '" + key
					+ "' parameter. ");
		}

		try {
			return Enum.valueOf(enumClass, valueString);
		} catch (Exception e) {
			throw new BadRequestException("Request has invalid '" + key
					+ "' parameter: '" + valueString
					+ "'; acceptable values are " + EnumSet.allOf(enumClass));
		}
	}

}
