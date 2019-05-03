/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;


import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 *
 * @author hjk54
 *
 * There are multiple date time properties in VIVO, all of which have a DateTimeValue individual as an object.
 * I am extending DateTimeValueFormGenerator to enable the predicate to be utilized as the "toDateTimeValue" property, i.e.
 * the property associating the subject with the date time value individual.  The dateTimeValueForm template has been
 * used as the basis for the general form for this generator that can be applied to multiple properties.
 */

public class DateTimePropertyGenerator extends DateTimeValueFormGenerator {

	String predicate = null;
	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
			HttpSession session) {
		predicate = EditConfigurationUtils.getPredicateUri(vreq);
        EditConfigurationVTwo conf = super.getEditConfiguration(vreq, session);
        return conf;
	}

	//isolating the predicate in this fashion allows this class to be subclassed for other date time value
	//properties
	@Override
	protected String getToDateTimeValuePredicate() {
		return predicate;
	}

}
