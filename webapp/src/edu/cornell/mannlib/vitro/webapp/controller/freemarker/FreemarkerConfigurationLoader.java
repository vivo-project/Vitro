/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class FreemarkerConfigurationLoader {
	private static final Log log = LogFactory
			.getLog(FreemarkerConfigurationLoader.class);

	private static final Map<String, FreemarkerConfiguration> themeToConfigMap = new HashMap<String, FreemarkerConfiguration>();

	public static FreemarkerConfiguration getConfig(VitroRequest vreq,
			ServletContext context) {
		String themeDir = getThemeDir(vreq.getAppBean());
		return getConfigForTheme(themeDir, vreq, context);
	}

	private static String getThemeDir(ApplicationBean appBean) {
		if (appBean == null) {
			log.error("Cannot get themeDir from null application bean");
			return null;
		}

		String themeDir = appBean.getThemeDir();
		if (themeDir == null) {
			log.error("themeDir is null");
			return null;
		}

		return themeDir.replaceAll("/$", "");
	}

	/**
	 * The Configuration is theme-specific because:
	 * 
	 * 1. The template loader is theme-specific, since it specifies a theme
	 * directory to load templates from.
	 * 
	 * 2. Shared variables like stylesheets are theme-specific.
	 */
	private static FreemarkerConfiguration getConfigForTheme(String themeDir,
			VitroRequest vreq, ServletContext context) {
		synchronized (themeToConfigMap) {
			if (themeToConfigMap.containsKey(themeDir)) {
				return themeToConfigMap.get(themeDir);
			} else {
				FreemarkerConfiguration config = new FreemarkerConfiguration(
						themeDir, vreq, context);
				themeToConfigMap.put(themeDir, config);
				return config;
			}
		}
	}

}