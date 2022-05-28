/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;

import javax.servlet.ServletContext;

/**
 * Help to load RDF files on first time and on every startup.
 */
public class RDFFilesLoader {
	private static final Log log = LogFactory.getLog(RDFFilesLoader.class);

	private static final String DEFAULT_RDF_FORMAT = "RDF/XML";
	private static final String RDF = "rdf";
	private static final String I18N = "i18n";
	private static final String FIRST_TIME = "firsttime";
	private static final String EVERY_TIME = "everytime";

	/**
	 * Path filter that ignores sub-directories, hidden files and markdown
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
	public static void loadFirstTimeFiles(ServletContext ctx, String modelPath, Model model,
			boolean firstTime) {
		if (firstTime) {
			String home = locateHomeDirectory();

			// Load common files
			Set<Path> paths = getPaths(home, RDF, modelPath, FIRST_TIME);

			// Load enabled languages
			Set<String> enabledLocales = getEnabledLocales(ctx);
			for (String locale : enabledLocales) {
				paths.addAll(getPaths(home, RDF, I18N, locale, modelPath, FIRST_TIME));
			}

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

		// Load common files
		Set<Path> paths = getPaths(home, RDF, modelPath, EVERY_TIME);

		// Load enabled languages
		Set<String> enabledLocales = getEnabledLocales(ctx);
		for (String locale : enabledLocales) {
			paths.addAll(getPaths(home, RDF, I18N, locale, modelPath, EVERY_TIME));
		}

		for (Path p : paths) {
			log.info("Loading " + relativePath(p, home));
			readOntologyFileIntoModel(p, everytimeModel);
		}
		model.addSubModel(everytimeModel);
	}

	public static Set<String> getEnabledLocales(ServletContext ctx) {
		Set<String> enabledLocales = new HashSet<>();

		// Which locales are enabled in runtime.properties?
		List<Locale> locales = SelectedLocale.getSelectableLocales(ctx);
		for (Locale locale : locales) {
			enabledLocales.add(locale.toLanguageTag().replace('-', '_'));
		}

		// If no languages were enabled in runtime.properties, add a fallback as the default
		if (enabledLocales.isEmpty()) {
			enabledLocales.add(SelectedLocale.getFallbackLocale().toString());
		}

		return enabledLocales;
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
	
	/**
	 * Check if the user model (UI) changes conflict with the changes made to
	 * the firsttime.  If there is conflict, the user model UI value will be
	 * left unchanged.
	 * 
	 * @param baseModel firsttime backup model
	 * @param userModel current state in the system (user/UI-model)
	 * @param changesModel the changes between firsttime-files and firsttime-backup
	 */
	public static void removeChangesThatConflictWithUIEdits(Model baseModel,
	        Model userModel, Model changesModel) {
	    log.debug("Check if subtractions from backup-firsttime model to"
	            + " current state of firsttime-files were changed in user-model"
	            + " (via UI)");        
	    // We don't want to diff against the entire user model, which may be 
	    // huge.  We only care about subject/predicate pairs that exist in the
	    // changesModel.  So extract these first from userModel into a 
	    // scopedUserModel that we can use for diffing.
	    Model scopedUserModel = ModelFactory.createDefaultModel();
	    StmtIterator scopeIt = changesModel.listStatements();
	    while(scopeIt.hasNext()) {
	        Statement scopingStmt = scopeIt.next();
	        scopedUserModel.add(userModel.listStatements(
	                scopingStmt.getSubject(), scopingStmt.getPredicate(), (RDFNode) null));
	    }
	    log.debug("Scoped user model has " + scopedUserModel.size());
	    Model changesUserModel = scopedUserModel.difference(baseModel);         
	    log.debug("Diff of scoped user model against firsttime backup has "
	            + changesUserModel.size() + " triples");
	    List<Statement> changedInUIandFileStatements = new ArrayList<Statement>();
	    if(changesUserModel.isEmpty()) {
	        log.debug("There were no changes in the user-model via UI"
                    + " compared to the backup-firsttime-model");
	        return;
	    }
        removeBlankTriples(changesUserModel);
        if(log.isDebugEnabled()) {
            StringWriter out3 = new StringWriter();
            changesUserModel.write(out3, "TTL");
            log.debug("changesUserModel:\n" + out3);
        }
        log.debug("There were changes in the user-model via UI which have"
                + " also changed in the firsttime files. The following"
                + " triples will not be updated.");
        // Iterate over all statements and check if the ones which should be
        // removed were not changed via the UI
        StmtIterator userChanges = changesUserModel.listStatements();
        while (userChanges.hasNext()) {
            Statement stmt      = userChanges.nextStatement();
            Resource  subject   = stmt.getSubject();
            Property predicate  = stmt.getPredicate();
            RDFNode   object    = stmt.getObject();
            StmtIterator firsttimeChanges = changesModel.listStatements(
                    subject, predicate, (RDFNode) null);
            while (firsttimeChanges.hasNext()) {
                Statement stmt2      = firsttimeChanges.nextStatement();
                RDFNode   object2    = stmt2.getObject();
                // If subject and predicate are equal but the object differs
                // and the language tag is the same, do not update these triples.
                // This case indicates an change in the UI, which should not
                // be overwritten from the firsttime files.
                if(!object.equals(object2) ) {
                    // if object is an literal, check the language tag
                    if (object.isLiteral() && object2.isLiteral()) {
                        // if the language tag is the same, remove this
                        // triple from the update list
                        if(object.asLiteral().getLanguage().equals(
                                object2.asLiteral().getLanguage())) {
                            log.debug("This two triples changed UI and"
                                    + " files: \n UI: " + stmt
                                    + " \n file: " +stmt2);
                            changedInUIandFileStatements.add(stmt2);
                        }
                    } else {
                        log.debug("This two triples changed UI and"
                                + " files: \n UI: " + stmt
                                + " \n file: " +stmt2);
                        changedInUIandFileStatements.add(stmt2);
                    }
                }
            }
        }
        // remove triples which were changed in the user model (UI) from the list
        changesModel.remove(changedInUIandFileStatements);
	}

    /**
     * Remove all triples where subject or object is blank (Anon)
     */
    public static void removeBlankTriples(Model model) {
        List<Statement> removeStatement = new ArrayList<Statement>();
        StmtIterator stmts = model.listStatements();
        while (stmts.hasNext()) {
            Statement stmt      = stmts.nextStatement();
            if(stmt.getSubject().isAnon() || stmt.getObject().isAnon())
            {
                removeStatement.add(stmt);
            }
        }
        model.remove(removeStatement);
    }
    
    /**
     * Check 'isomorphism' for purposes of propagating firsttime changes.
     * Run Jena's isomorphism check, but if it fails only due to blank nodes,
     * ignore and treat as isomorphic anyway.  (Auto-updating firsttime
     * changes should occur only with named nodes.)
     * @param m1
     * @param m2
     * @return true if models are isomorphic or any lack of isomorphism exists
     *  only in blank nodes
     */
    public static boolean areIsomporphic(Model m1, Model m2) {
        boolean isIsomorphic = m1.isIsomorphicWith(m2);
        if(isIsomorphic) {
            return true;
        } else {
            Model diff1 = m1.difference(m2);
            Model diff2 = m2.difference(m1);
            removeBlankTriples(diff1);
            removeBlankTriples(diff2);
            return (diff1.isEmpty() && diff2.isEmpty());
        }
    }
    
}