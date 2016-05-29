/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.freemarker;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import freemarker.core.Environment;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * This Freemarker method will produce a bundle of text strings. It is simply a
 * wrapper around I18n that produces a wrapped I18nBundle.
 * 
 * If the bundle name is not provided, the default bundle is assumed.
 */
public class I18nMethodModel implements TemplateMethodModel {
	private static final Log log = LogFactory.getLog(I18nMethodModel.class);

	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List args) throws TemplateModelException {
		if (args.size() > 1) {
			throw new TemplateModelException("Too many arguments: "
					+ "displayText method only requires a bundle name.");
		}
		Object arg = args.isEmpty() ? I18n.DEFAULT_BUNDLE_NAME : args.get(0);
		if (!(arg instanceof String)) {
			throw new IllegalArgumentException(
					"Arguments to a TemplateMethodModel are supposed to be Strings!");
		}

		log.debug("Asking for this bundle: " + arg);
		String bundleName = (String) arg;

		Environment env = Environment.getCurrentEnvironment();
		HttpServletRequest request = (HttpServletRequest) env
				.getCustomAttribute("request");
		I18nBundle tb = I18n.bundle(bundleName, request);
		return new I18nBundleTemplateModel(bundleName, tb);
	}

}
