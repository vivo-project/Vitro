/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.freemarker;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.DeepUnwrap;

/**
 * A Freemarker representation of a text string. Because it implements
 * TemplateScalarModel, you can use it as a string value. And because it
 * implements TemplateMethodModel, you can pass arguments to it for formatting.
 * 
 * So if the string is "His name is {0}!", then these references could be used:
 * {@code
 * ${string} ==> "His name is {0}!"
 * 
 * ${string("Bozo")} ==> "His name is Bozo!"
 * }
 * Note that the format of the message is determined by java.text.MessageFormat,
 * so argument indices start at 0 and you can escape a substring by wrapping it
 * in apostrophes.
 */
public class I18nStringTemplateModel implements TemplateMethodModelEx,
		TemplateScalarModel {
	private static final Log log = LogFactory
			.getLog(I18nStringTemplateModel.class);

	private final String bundleName;
	private final String key;
	private final String textString;

	public I18nStringTemplateModel(String bundleName, String key,
			String textString) {
		this.bundleName = bundleName;
		this.key = key;
		this.textString = textString;
	}

	@Override
	public String getAsString() throws TemplateModelException {
		return textString;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object exec(List args) throws TemplateModelException {
		log.debug("Formatting string '" + key + "' from bundle '" + bundleName
				+ "' with these arguments: " + args);

		if (args.isEmpty()) {
			return textString;
		} else {
			Object[] unwrappedArgs = new Object[args.size()];
			for (int i = 0; i < args.size(); i++) {
				unwrappedArgs[i] = DeepUnwrap.unwrap((TemplateModel) args
						.get(i));
			}
			try {
				return MessageFormat.format(textString, unwrappedArgs);
			} catch (Exception e) {
				String message = "Can't format '" + key + "' from bundle '"
						+ bundleName + "', wrong argument types: " + args
						+ " for message format'" + textString + "'";
				log.warn(message);
				return message;
			}
		}
	}

}
