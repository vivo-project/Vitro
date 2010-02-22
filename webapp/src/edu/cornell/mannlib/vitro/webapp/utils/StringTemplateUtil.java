/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import org.antlr.stringtemplate.StringTemplate;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class StringTemplateUtil {

	// StringTemplate templates handle null values correctly through an if test, but
	// an empty string is still treated as a value, so we use this method to prevent
	// setting the attribute if the value is an empty string. Otherwise we'll end up
	// outputting empty HTML tags, for example. EL has the advantage here with the empty
	// operator, which treats empty strings like null values.
	public static void setTemplateStringAttribute(StringTemplate template, String attribute, String value) {
		if ( !StringUtils.isBlank(value)) {
			template.setAttribute(attribute, value);
		}
	}
	
}
