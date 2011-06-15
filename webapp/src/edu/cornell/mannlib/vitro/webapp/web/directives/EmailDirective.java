/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailMessage;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Process the inputs for a FreemarkerEmailMessage.
 * 
 * @see FreemarkerEmailMessage
 */
public class EmailDirective extends BaseTemplateDirectiveModel {

	private static final Log log = LogFactory.getLog(EmailDirective.class);

	private final FreemarkerEmailMessage message;

	public EmailDirective(FreemarkerEmailMessage message) {
		this.message = message;
	}

	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {

		String subject = getOptionalSimpleScalarParameter(params, "subject");
		if (subject != null) {
			message.setSubject(subject);
		}

		String htmlContent = getOptionalSimpleScalarParameter(params, "html");
		if (htmlContent != null) {
			message.setHtmlContent(htmlContent);
		}

		String textContent = getOptionalSimpleScalarParameter(params, "text");
		if (textContent != null) {
			message.setTextContent(textContent);
		}

		if ((htmlContent == null) && (textContent == null)) {
			throw new TemplateModelException("The email directive must have "
					+ "either a 'html' parameter or a 'text' parameter.");
		}
	}

	private String getOptionalSimpleScalarParameter(Map<?, ?> params,
			String name) throws TemplateModelException {
		Object o = params.get(name);
		if (o == null) {
			return null;
		}

		if (!(o instanceof SimpleScalar)) {
			throw new TemplateModelException("The '" + name + "' parameter "
					+ "for the email directive must be a string value.");
		}

		return o.toString();
	}

	@Override
	public Map<String, Object> help(String name) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		map.put("effect",
				"Create an email message from the parameters set in the invoking template.");

		Map<String, String> params = new HashMap<String, String>();
		params.put("subject", "email subject (optional)");
		params.put("html", "HTML version of email message (optional)");
		params.put("text", "Plain text version of email message (optional)");
		map.put("parameters", params);

		List<String> examples = new ArrayList<String>();
		examples.add("&lt;email subject=\"Password reset confirmation\" html=html text=text&gt;");
        examples.add("&lt;email html=html text=text&gt;");
		map.put("examples", examples);

		return map;
	}
}
