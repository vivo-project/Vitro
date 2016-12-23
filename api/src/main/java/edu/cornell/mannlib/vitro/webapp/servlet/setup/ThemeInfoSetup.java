/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean.ThemeInfo;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class ThemeInfoSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(ThemeInfoSetup.class);

	// Set default theme based on themes present on the file system
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		String themeDirPath = ctx.getRealPath("/themes");
		if (themeDirPath == null) {
			throw new IllegalStateException(
					"Application does not have a /themes directory.");
		}
		File themesBaseDir = new File(themeDirPath);

		List<String> themeNames = getThemeNames(themesBaseDir);
		log.debug("themeNames: " + themeNames);
		if (themeNames.isEmpty()) {
			ss.fatal(this, "The application contains no themes. '"
					+ themesBaseDir.getAbsolutePath()
					+ "' has no child directories.");
		}

		String defaultThemeName = "vitro";
		if (!themeNames.contains(defaultThemeName)) {
			defaultThemeName = themeNames.get(0);
		}
		log.debug("defaultThemeName: " + defaultThemeName);

		String currentThemeName = getCurrentThemeName(ctx);
		log.debug("currentThemeName: " + currentThemeName);
		if ((currentThemeName != null) && (!currentThemeName.isEmpty())
				&& (!themeNames.contains(currentThemeName))) {
			ss.warning(this, "The current theme selection is '"
					+ currentThemeName
					+ "', but that theme is not available. The '"
					+ defaultThemeName + "' theme will be used instead. "
					+ "Go to the Site Admin page and choose "
					+ "\"Site Information\" to select a theme.");
		}

		ApplicationBean.themeInfo = new ThemeInfo(themesBaseDir,
				defaultThemeName, themeNames);
		ss.info(this, "current theme: " + currentThemeName
				+ ", default theme: " + defaultThemeName + ", available themes: "
				+ themeNames);
	}

	/** Get a list of the names of available themes, sorted alphabetically. */
	private List<String> getThemeNames(File themesBaseDir) {
		ArrayList<String> themeNames = new ArrayList<String>();

		for (File child : themesBaseDir.listFiles()) {
			if (child.isDirectory()) {
				themeNames.add(child.getName());
			}
		}

		Collections.sort(themeNames, String.CASE_INSENSITIVE_ORDER);
		return themeNames;
	}

	private String getCurrentThemeName(ServletContext ctx) {
		OntModel ontModel = ModelAccess.on(ctx).getOntModel(APPLICATION_METADATA);

		ontModel.enterCriticalSection(Lock.READ);
		try {
			Property property = ontModel
					.getProperty(VitroVocabulary.PORTAL_THEMEDIR);
			ClosableIterator<RDFNode> nodes = ontModel
					.listObjectsOfProperty(property);
			try {
				if (nodes.hasNext()) {
					String themeDir = ((Literal) nodes.next()).getString();
					return ThemeInfo.themeNameFromDir(themeDir);
				} else {
					return null;
				}
			} finally {
				nodes.close();
			}
		} catch (Exception e) {
			log.error(e, e);
			return null;
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}

}
