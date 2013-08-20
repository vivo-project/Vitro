/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

/**
 * Help to load RDF files on first time and on every startup.
 */
public class RDFFilesLoader {
	private static final Log log = LogFactory.getLog(RDFFilesLoader.class);

	private static final String PROPERTY_VITRO_HOME = "vitro.home";
	private static final String DEFAULT_RDF_FORMAT = "RDF/XML";
	private static final String FIRST_TIME = "firsttime";

	/** Directory filter that ignores sub-directories and hidden files. */
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
			return true;
		}
	};

	private final String homeDirProperty;

	public RDFFilesLoader(ServletContext ctx) {
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		this.homeDirProperty = props.getProperty(PROPERTY_VITRO_HOME);
	}

	/**
	 * Load the "first time" files if the model is empty.
	 */
	public void loadFirstTimeFiles(String modelPath, Model model) {
		loadFirstTimeFiles(modelPath, model, model.isEmpty());
	}
	
	/**
	 * Load the "first time" files if we say it is the first time.
	 */
	public void loadFirstTimeFiles(String modelPath, Model model, boolean firstTime) {
		if (firstTime) {
			Set<Path> paths = getPaths(homeDirProperty, modelPath, FIRST_TIME);
			for (Path p : paths) {
				readOntologyFileIntoModel(p, model);
			}
		}
	}

	private Set<Path> getPaths(String parentDir, String... strings) {
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
			log.debug("Filegraph directory '" + dir + "' doesn't exist.");
		}
		log.debug("Paths from '" + dir + "': " + paths);
		return paths;
	}

	private static void readOntologyFileIntoModel(Path p, Model model) {
		String format = getRdfFormat(p);
		log.info("Loading ontology file at " + p + " as format " + format);
		try (InputStream stream = new FileInputStream(p.toFile())) {
			model.read(stream, null, format);
			log.debug("...successful");
		} catch (IOException e) {
			log.error("Failed to load ontology file at '" + p + "' as format "
					+ format, e);
		}
	}

	private static String getRdfFormat(Path p) {
		String filename = p.getFileName().toString().toLowerCase();
		if (filename.endsWith("n3"))
			return "N3";
		else if (filename.endsWith("ttl"))
			return "TURTLE";
		else
			return DEFAULT_RDF_FORMAT;
	}

}
