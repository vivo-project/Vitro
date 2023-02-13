/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n.freemarker;

import java.text.MessageFormat;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.i18n.TranslationProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
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

	private final String key;
	private final String textString;

	public I18nStringTemplateModel( String key,	String textString) {
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
		log.debug("Formatting string '" + key + "' with these arguments: " + args);

		if (args.isEmpty()) {
			return textString;
		} else {
			Object[] unwrappedArgs = new Object[args.size()];
			for (int i = 0; i < args.size(); i++) {
				unwrappedArgs[i] = DeepUnwrap.unwrap((TemplateModel) args
						.get(i));
			}
			try {
				if(isOnlineTranslationsEnabled()) {
					return getOnlineTranslationsFormattedMessage(textString, unwrappedArgs);
				} else {
					return MessageFormat.format(TranslationProvider.preprocessForFormating(textString), unwrappedArgs);
				}
			} catch (Exception e) {
				String message = "Can't format '" + key + "', wrong argument types: " + args
						+ " for message format'" + textString + "'";
				log.warn(message);
				return message;
			}
		}
	}
	
	/**
	 * Splits preProcessed string, formats message with arguments, lists arguments before message 
	 * and combines preProcessed string back to be used with online translations.
	 * Substitutes arguments in message which is a part of preProcessed string  
	 * @param preProcessed String "startSep + key + intSep + textString + intSep + message + endSep"
	 * @param args that should be listed before message and substituted in the message itself
	 * @return
	 */
	private String getOnlineTranslationsFormattedMessage(String preProcessed, Object[] args) {
		String[] parts = preProcessed.split(I18nBundle.INT_SEP);
		final int messageIndex = parts.length -1;
		String message = MessageFormat.format(TranslationProvider.preprocessForFormating(parts[messageIndex]), args);
		String[] arguments = convertToArrayOfStrings(args);
		parts[messageIndex] = "";
		String result = String.join(I18nBundle.INT_SEP, parts) + 
				            String.join(I18nBundle.INT_SEP, arguments) + 
				            I18nBundle.INT_SEP + message ;
		return result;
	}

	private String[] convertToArrayOfStrings(Object[] args) {
		String[] result = new String[args.length];
		for (int i = 0; i < result.length; i++)
			if (args[i] != null) {
				result[i] = args[i].toString();	
			} else {
				result[i] = "";
			}
		return result;
	}

	private static boolean isOnlineTranslationsEnabled() {
		return DeveloperSettings.getInstance().getBoolean(Key.I18N_ONLINE_TRANSLATION);
	}

}
