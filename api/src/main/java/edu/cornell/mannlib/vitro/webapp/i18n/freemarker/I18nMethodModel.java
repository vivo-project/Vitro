/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n.freemarker;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import freemarker.core.Environment;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * This Freemarker method will produce a bundle of text strings. It is simply a
 * wrapper around I18n that produces a wrapped I18nBundle.
 *
 * If the bundle name is not provided, the default bundle is assumed.
 */
public class I18nMethodModel implements TemplateMethodModelEx  {
	private static final Log log = LogFactory.getLog(I18nMethodModel.class);

	@Override
	public Object exec(List args) throws TemplateModelException {
		Environment env = Environment.getCurrentEnvironment();
		HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
		I18nBundle tb = I18n.bundle(request);
		return new I18nBundleTemplateModel(tb);
	}

}
