/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Check all of the FileByteStream objects. If there are any that don't have
 * alias URLs, fix them.
 */
public class FileStorageAliasAdder {
	private static final Log log = LogFactory
			.getLog(FileStorageAliasAdder.class);

	private static final String FILE_PATH = "/file/";

	/**
	 * Query: get all bytestream resources that do not have alias URLs.
	 */
	private static final String QUERY_BYTESTREAMS_WITHOUT_ALIASES = ""
			+ "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n"
			+ "SELECT ?bs\n" + "WHERE {\n"
			+ "  ?bs rdf:type public:FileByteStream\n"
			+ "  OPTIONAL { ?bs public:directDownloadUrl ?alias }\n"
			+ "  FILTER ( !BOUND(?alias) )\n" + "}\n";

	/**
	 * Query: get the filenames for all bytestream resources that do not have
	 * alias URLs.
	 */
	private static final String QUERY_FILENAMES_FOR_BYTESTREAMS = ""
			+ "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n"
			+ "SELECT ?bs ?fn\n" + "WHERE {\n"
			+ "  ?bs rdf:type public:FileByteStream . \n"
			+ "  ?f public:downloadLocation ?bs . \n"
			+ "  ?f public:filename ?fn . \n"
			+ "  OPTIONAL { ?bs public:directDownloadUrl ?alias . }\n"
			+ "  FILTER ( !BOUND(?alias) )\n" + "}\n";

	private final Model model;
	private final File upgradeDirectory;
	private final String vivoDefaultNamespace;

	private FSULog updateLog;

	private Set<String> bytestreamUrisWithoutAliases;
	private Map<String, String> bytestreamUrisAndFilenames;

	public FileStorageAliasAdder(Model model, File uploadDirectory,
			String vivoDefaultNamespace) {
		this.model = model;
		this.upgradeDirectory = new File(uploadDirectory, "upgrade");
		this.vivoDefaultNamespace = vivoDefaultNamespace;
	}

	/**
	 * Go through all of the FileByteStream objects in the model, creating Alias
	 * URLs for any objects that don't have them.
	 * 
	 * If there is nothing to do, don't even create a log file, just exit.
	 * 
	 * If there is something to do, go through the whole process.
	 * 
	 * At the end, there should be nothing to do.
	 */
	public void update() {
		// If there is nothing to do, we're done: don't even create a log file.
		if (!isThereAnythingToDo()) {
			log.debug("Found no FileByteStreams without alias URLs.");
			return;
		}

		setup();

		try {
			findAndAddMissingAliasUrls();

			if (isThereAnythingToDo()) {
				throw new IllegalStateException("FileStorageAliasAdder "
						+ "was unsuccessful -- model still contains "
						+ "FileByteStreams without alias URLs.");
			}

			updateLog.section("Finished adding alias URLs to FileByteStreams.");
		} finally {
			updateLog.close();
		}

		log.info("Finished adding alias URLs to FileByteStreams.");
	}

	/**
	 * Query the model. If there are any FileByteStream objects with no Alias
	 * URL, we have work to do.
	 */
	private boolean isThereAnythingToDo() {
		String queryString = QUERY_BYTESTREAMS_WITHOUT_ALIASES;
		log.debug("query: " + queryString);

		QueryExecution qexec = null;
		try {
			qexec = createQueryExecutor(queryString);
			ResultSet results = qexec.execSelect();

			boolean foundSome = results.hasNext();
			log.debug("any work to do? " + foundSome);

			return foundSome;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
	}

	/**
	 * Create the upgrade directory. Create the log file. If we fail, drop dead.
	 */
	private void setup() {
		try {
			this.upgradeDirectory.mkdirs();
			updateLog = new FSULog(this.upgradeDirectory,
					"FileStorageAliasAdder-log");
			log.info("Updating pre-1.1 file references. Log file is "
					+ updateLog.getFilename());
		} catch (IOException e) {
			if (updateLog != null) {
				updateLog.close();
			}
			throw new IllegalStateException("can't create log file: '"
					+ updateLog.getFilename() + "'", e);
		}
	}

	/**
	 * Add an alias URL to any FileByteStream object that doesn't have one.
	 */
	private void findAndAddMissingAliasUrls() {
		findBytestreamsWithoutAliasUrls();
		findFilenamesForBytestreams();
		addAliasUrlsToModel();
	}

	/**
	 * Find every bytestream that doesn't have an alias URL.
	 */
	private void findBytestreamsWithoutAliasUrls() {
		BytestreamUriUnpacker unpacker = new BytestreamUriUnpacker();

		runQuery(QUERY_BYTESTREAMS_WITHOUT_ALIASES, unpacker);
		this.bytestreamUrisWithoutAliases = unpacker.getUris();

		log.debug("Found " + unpacker.getUris().size()
				+ " bytestreams without alias URLs");
	}

	/**
	 * Find the filename for every bytestream that doesn't have an alias URL.
	 */
	private void findFilenamesForBytestreams() {
		FilenameUnpacker unpacker = new FilenameUnpacker();

		runQuery(QUERY_FILENAMES_FOR_BYTESTREAMS, unpacker);
		this.bytestreamUrisAndFilenames = unpacker.getFilenameMap();

		log.debug("Found " + unpacker.getFilenameMap().size()
				+ " bytestreams with filenames but no alias URLs");
	}

	/** Add an alias URL to each resource in the list. */
	private void addAliasUrlsToModel() {
		if (this.bytestreamUrisWithoutAliases.isEmpty()) {
			updateLog.warn("Found no bytestreams without aliases. "
					+ "Why am I here?");
			return;
		}

		Property aliasProperty = model
				.createProperty(VitroVocabulary.FS_ALIAS_URL);

		for (String bytestreamUri : this.bytestreamUrisWithoutAliases) {
			String aliasUrl = figureAliasUrl(bytestreamUri);
			Resource resource = model.getResource(bytestreamUri);

			ModelWrapper.add(model, resource, aliasProperty, aliasUrl);
			updateLog.log(resource, "added alias URL: '" + aliasUrl + "'");
		}
	}

	/**
	 * Convert the bytestream URI and the filename into an alias URL.
	 * 
	 * If they aren't in our default namespace, or they don't have a filename,
	 * then their URI is the best we can do for an alias URL.
	 */
	private String figureAliasUrl(String bytestreamUri) {
		if (!bytestreamUri.startsWith(vivoDefaultNamespace)) {
			updateLog.warn("bytestream uri does not start "
					+ "with the default namespace: '" + bytestreamUri + "'");
			return bytestreamUri;
		}

		String filename = this.bytestreamUrisAndFilenames.get(bytestreamUri);
		if (filename == null) {
			updateLog.warn("bytestream has no surrogate or no filename: '"
					+ bytestreamUri + "'");
			return "filename_not_found";
		}

		try {
			String remainder = bytestreamUri.substring(vivoDefaultNamespace
					.length());
			String encodedFilename = URLEncoder.encode(filename, "UTF-8");
			String separator = remainder.endsWith("/") ? "" : "/";

			return FILE_PATH + remainder + separator + encodedFilename;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // No UTF-8? Can't happen.
		}
	}

	private void runQuery(String queryString, QueryResultUnpacker unpacker) {
		log.debug("query: " + queryString);

		QueryExecution qexec = null;
		try {
			qexec = createQueryExecutor(queryString);

			Iterator<QuerySolution> results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution result = results.next();
				if (log.isDebugEnabled()) {
					log.debug("Query result variables: "
							+ listVariables(result));
				}
				unpacker.unpack(result);
			}
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
	}

	private QueryExecution createQueryExecutor(String queryString) {
		Query query = QueryFactory.create(queryString);
		return QueryExecutionFactory.create(query, model);
	}

	/** For debug logging. */
	private List<String> listVariables(QuerySolution result) {
		List<String> list = new ArrayList<String>();
		for (Iterator<String> names = result.varNames(); names.hasNext();) {
			String name = names.next();
			RDFNode value = result.get(name);
			list.add(name + "=" + value);
		}
		return list;
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private interface QueryResultUnpacker {
		public abstract void unpack(QuerySolution result);
	}

	private class BytestreamUriUnpacker implements QueryResultUnpacker {
		private final Set<String> uris = new HashSet<String>();

		@Override
		public void unpack(QuerySolution result) {
			Resource bytestream = result.getResource("bs");
			if (bytestream == null) {
				updateLog.error("Query result contains no "
						+ "bytestream resource: " + result);
				return;
			}

			uris.add(bytestream.getURI());
		}

		public Set<String> getUris() {
			return uris;
		}
	}

	private class FilenameUnpacker implements QueryResultUnpacker {
		private final Map<String, String> filenameMap = new HashMap<String, String>();

		@Override
		public void unpack(QuerySolution result) {
			Resource bytestream = result.getResource("bs");
			if (bytestream == null) {
				updateLog.error("Query result contains no "
						+ "bytestream resource: " + result);
				return;
			}
			String bytestreamUri = bytestream.getURI();

			Literal filenameLiteral = result.getLiteral("fn");
			if (filenameLiteral == null) {
				updateLog.error("Query result for '" + bytestreamUri
						+ "' contains no filename.");
				return;
			}
			String filename = filenameLiteral.getString();

			filenameMap.put(bytestreamUri, filename);
		}

		public Map<String, String> getFilenameMap() {
			return filenameMap;
		}
	}

}
