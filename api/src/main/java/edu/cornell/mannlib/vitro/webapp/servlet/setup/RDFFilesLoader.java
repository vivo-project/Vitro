/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;

/**
 * Help to load RDF files on first time and on every startup.
 */
public class RDFFilesLoader {
	private static final Log log = LogFactory.getLog(RDFFilesLoader.class);

	private static final String DEFAULT_RDF_FORMAT = "RDF/XML";
	private static final String RDF = "rdf";
	private static final String FIRST_TIME = "firsttime";
	private static final String EVERY_TIME = "everytime";

	/**
	 * Path filter that ignores sub-directories, hidden files, and markdown
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
	public static void loadFirstTimeFiles(String modelPath, Model model,
			boolean firstTime) {
		if (firstTime) {
			String home = locateHomeDirectory();
			Set<Path> paths = getPaths(home, RDF, modelPath, FIRST_TIME);
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
	public static void loadEveryTimeFiles(String modelPath, OntModel model) {
		OntModel everytimeModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);
		String home = locateHomeDirectory();
		Set<Path> paths = getPaths(home, RDF, modelPath, EVERY_TIME);
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
	 * Create a model from all the RDF files in the specified directory.
	 */
	public static OntModel getModelFromDir(File dir) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		if (dir == null) {
			log.warn("Must pass a File to getModelFromDir()");
			return model;
		}
		if (!dir.isDirectory()) {
			log.warn("Directory must be a File object for a directory");
			return model;
		}
		if (!dir.canRead()) {
			log.warn("getModelFromDir(): Directory "
					+ " must be readable, check permissions on "
					+ dir.getAbsolutePath());
			return model;
		}

		Set<Path> paths = getPaths(dir.getPath());
		for (Path p : paths) {
			readOntologyFileIntoModel(p, model);
		}
		return model;
	}

	/**
	 * Find the paths to RDF files in this directory. Sub-directories, hidden
	 * files, and markdown files are ignored.
	 */
	private static Set<Path> getPaths(String parentDir, String... strings) {
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
