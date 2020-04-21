/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.i18n.VitroResourceBundle;
import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;

import javax.servlet.ServletContext;

/**
 * Help to load RDF files on first time and on every startup.
 */
public class RDFFilesLoader {
	private static final Log log = LogFactory.getLog(RDFFilesLoader.class);

	private static final String DEFAULT_RDF_FORMAT = "RDF/XML";
	private static final String RDF = "rdf";
	private static final String FIRST_TIME = "firsttime";
	private static final String EVERY_TIME = "everytime";

	private static List<String> omittedLocales;

	/**
	 * Path filter that ignores sub-directories, hidden files, markdown and non-enabled language
	 * files.
	 */
	private static final DirectoryStream.Filter<Path> RDF_FILE_FILTER = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path p) throws IOException {
			if (Files.isHidden(p)) {
				return false;
			}
			if (Files.isDirectory(p)) {
				log.warn("RDF files in subdirectories are not loaded. Directory '"
						+ p + "' ignored.");
				return false;
			}
			if (p.toString().endsWith(".md")) {
				return false;
			}
			// Skip language files that are not enabled
			// ..Assumes all language files take the form: path/to/file/name-<locale>.extension
			String basename =  FilenameUtils.getBaseName(p.toString());
			for (String omit : omittedLocales) {
				if (basename.endsWith(omit)) {
					log.info("Ignoring file not enabled in i18n configuration: " + p);
					return false;
				}
			}
			return true;
		}
	};

	/**
	 * Load the "first time" files if we say it is the first time.
	 *
	 * The location is based on the home directory and the model path: "abox",
	 * "display", etc.
	 *
	 * The files from the directory are added to the model.
	 */
	public static void loadFirstTimeFiles(ServletContext ctx, String modelPath, Model model,
			boolean firstTime) {
		if (firstTime) {
			String home = locateHomeDirectory();
			Set<Path> paths = getPaths(ctx, home, RDF, modelPath, FIRST_TIME);
			for (Path p : paths) {
				log.info("Loading " + relativePath(p, home));
				readOntologyFileIntoModel(p, model);
			}
		} else {
			log.debug("Not loading first time files on '" + modelPath
					+ "', firstTime=false");
		}
	}

	/**
	 * Load the "every time" files.
	 *
	 * The location is based on the home directory and the model path: "abox",
	 * "display", etc.
	 *
	 * The files from the directory become a sub-model of the model.
	 */
	public static void loadEveryTimeFiles(ServletContext ctx, String modelPath, OntModel model) {
		OntModel everytimeModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);
		String home = locateHomeDirectory();
		Set<Path> paths = getPaths(ctx, home, RDF, modelPath, EVERY_TIME);
		for (Path p : paths) {
			log.info("Loading " + relativePath(p, home));
			readOntologyFileIntoModel(p, everytimeModel);
		}
		model.addSubModel(everytimeModel);
	}

	private static Path relativePath(Path p, String home) {
		try {
			return Paths.get(home).relativize(p);
		} catch (Exception e) {
			return p;
		}
	}

	/**
	 * Find the paths to RDF files in this directory. Sub-directories, hidden
	 * files, markdown, and non-enabled language files are ignored.
	 */
	private static Set<Path> getPaths(ServletContext ctx, String parentDir, String... strings) {
		// Ensure that omitted locales are loaded and available
		loadOmittedLocales(ctx);

		Path dir = Paths.get(parentDir, strings);

		Set<Path> paths = new TreeSet<>();
		if (Files.isDirectory(dir)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
					RDF_FILE_FILTER)) {
				for (Path p : stream) {
					paths.add(p);
				}
			} catch (IOException e) {
				log.warn("Failed to read directory '" + dir + "'", e);
			}
		} else {
			log.debug("Directory '" + dir + "' doesn't exist.");
		}
		log.debug("Paths from '" + dir + "': " + paths);
		return paths;
	}

    private static void loadOmittedLocales(ServletContext ctx) {
        // Only load if 'omittedLocales' has not been initialized
        if (omittedLocales == null) {
            omittedLocales = new LinkedList<>();
            List<String> enabledLocales = new LinkedList<>();

            // Which locales are enabled in runtime.properties?
            List<Locale> locales = SelectedLocale.getSelectableLocales(ctx);
            for (Locale locale : locales) {
                enabledLocales.add(locale.toLanguageTag().replace('-', '_'));
            }

            // Get ResourceBundle that contains listing of all available i18n languages
            WebappDaoFactory wadf = ModelAccess.on(ctx).getWebappDaoFactory();
            ApplicationBean app = wadf.getApplicationDao().getApplicationBean();
            String availableLangsFilename = app.getAvailableLangsFile();
            String themeI18nPath = "/not-used";
            String appI18nPath = "/i18n";
            VitroResourceBundle bundle = VitroResourceBundle.getBundle(
                    availableLangsFilename, ctx, appI18nPath, themeI18nPath,
                    ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));

            // This should not happen
            if (bundle == null) {
                throw new RuntimeException("Unable to find bundle: " + availableLangsFilename);
            }

            // If no languages were enabled in runtime.properties, add 'en_US' as the default
            if (enabledLocales.isEmpty()) {
                enabledLocales.add("en_US");
            }

            // Omitted locales are the available locales minus the enabled locales
            Enumeration langs = bundle.getKeys();
            while (langs.hasMoreElements()) {
                String available = (String) langs.nextElement();
                if (!enabledLocales.contains(available)) {
                    omittedLocales.add(available);
                }
            }
        }
    }

	private static void readOntologyFileIntoModel(Path p, Model model) {
		String format = getRdfFormat(p);
		log.debug("Loading " + p);
		try (InputStream stream = new FileInputStream(p.toFile())) {
			model.read(stream, null, format);
			log.debug("...successful");
		} catch (Exception e) {
			log.warn("Could not load file '" + p + "' as " + format
					+ ". Check that it contains valid data.", e);
		}
	}

	private static String getRdfFormat(Path p) {
		String filename = p.getFileName().toString().toLowerCase();
		if (filename.endsWith("n3"))
			return "N3";
		else if (filename.endsWith("nt"))
			return "N-TRIPLES";
		else if (filename.endsWith("ttl"))
			return "TURTLE";
		else
			return DEFAULT_RDF_FORMAT;
	}

	private static String locateHomeDirectory() {
		return ApplicationUtils.instance().getHomeDirectory().getPath()
				.toString();
	}

	/**
	 * No need to create an instance -- all methods are static.
	 */
	private RDFFilesLoader() {
		// Nothing to initialize.
	}

}
