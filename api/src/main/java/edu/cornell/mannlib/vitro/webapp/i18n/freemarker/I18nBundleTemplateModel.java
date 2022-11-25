/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n.freemarker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * For Freemarker, this acts like a bundle of text strings. It is simply a
 * wrapper around an I18nBundle.
 */
public class I18nBundleTemplateModel implements TemplateHashModel {
	private static final Log log = LogFactory.getLog(I18nBundleTemplateModel.class);

	private final I18nBundle textBundle;

	public I18nBundleTemplateModel( I18nBundle textBundle) {
		this.textBundle = textBundle;
	}

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		return new I18nStringTemplateModel(key,	textBundle.text(key));
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		return false;
	}

}
