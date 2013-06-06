/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Some static methods for the GUI aspects of selecting a Locale.
 */
public class LocaleSelectorUtilities {
	private static final Log log = LogFactory
			.getLog(LocaleSelectorUtilities.class);

	/**
	 * Look in the current theme directory to find a selection image for this
	 * Locale.
	 * 
	 * Images are expected at a resource path like
	 * /[themeDir]/i18n/images/select_locale_[locale_code].*
	 * 
	 * For example, /themes/wilma/i18n/images/select_locale_en.png
	 * /themes/wilma/i18n/images/select_locale_en.JPEG
	 * /themes/wilma/i18n/images/select_locale_en.gif
	 * 
	 * To create a proper URL, prepend the context path.
	 */
	public static String getImageUrl(VitroRequest vreq, Locale locale)
			throws FileNotFoundException {
		String filename = "select_locale_" + locale + ".";

		String themeDir = vreq.getAppBean().getThemeDir();
		String imageDirPath = "/" + themeDir + "i18n/images/";

		ServletContext ctx = vreq.getSession().getServletContext();
		@SuppressWarnings("unchecked")
		Set<String> resourcePaths = ctx.getResourcePaths(imageDirPath);
		if (resourcePaths != null) {
			for (String resourcePath : resourcePaths) {
				if (resourcePath.contains(filename)) {
					String fullPath = vreq.getContextPath() + resourcePath;
					log.debug("Found image for " + locale + " at '" + fullPath
							+ "'");
					return fullPath;
				}
			}
		}
		throw new FileNotFoundException("Can't find an image for " + locale);
	}
}
