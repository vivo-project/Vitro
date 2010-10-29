/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private static final String QUERY_WORK_TO_DO = "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n"
			+ "SELECT ?bs\n"
			+ "WHERE {\n"
			+ "  ?bs rdf:type public:FileByteStream\n"
			+ "  OPTIONAL { ?bs public:directDownloadUrl ?alias }\n"
			+ "  FILTER ( !BOUND(?alias) )\n" + "}\n";

	/**
	 * Query: get all bytestream resources that do not have alias URLs. Get
	 * their filenames from their surrogates also.
	 */
	private static final String QUERY_MORE_INFO = "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n"
			+ "SELECT ?bs ?f ?fn\n"
			+ "WHERE {\n"
			+ "  ?bs rdf:type public:FileByteStream . \n"
			+ "  ?f public:downloadLocation ?bs . \n"
			+ "  OPTIONAL { \n"
			+ "    ?f public:filename ?fn . \n"
			+ "  }\n"
			+ "  OPTIONAL { \n"
			+ "    ?bs public:directDownloadUrl ?alias . \n"
			+ "  }\n"
			+ "  FILTER ( !BOUND(?alias) )\n" + "}\n";

	private final Model model;
	private final File upgradeDirectory;
	private final String vivoDefaultNamespace;

	private FSULog updateLog;

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
		String queryString = QUERY_WORK_TO_DO;
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
		List<BytestreamInfo> list;
		list = findBytestreamsWithMissingValues();
		addMissingValuesToModel(list);
	}

	/**
	 * Find every bytestream that doesn't have an alias URL. Find the filename
	 * for the bytestream also.
	 */
	private List<BytestreamInfo> findBytestreamsWithMissingValues() {
		List<BytestreamInfo> list = new ArrayList<BytestreamInfo>();
		String queryString = QUERY_MORE_INFO;
		log.debug("query: " + queryString);

		QueryExecution qexec = null;
		try {
			qexec = createQueryExecutor(queryString);

			Iterator<?> results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution result = (QuerySolution) results.next();
				BytestreamInfo bs = captureQueryResult(result);
				if (bs != null) {
					list.add(bs);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return list;
	}

	/**
	 * Capture the data from each valid query result. If this data isn't
	 * perfectly valid, complain and return null instead.
	 */
	private BytestreamInfo captureQueryResult(QuerySolution result) {
		if (log.isDebugEnabled()) {
			log.debug("Query result variables: " + listVariables(result));
		}

		Resource bytestream = result.getResource("bs");
		if (bytestream == null) {
			updateLog.error("Query result contains no bytestream resource: "
					+ result);
			return null;
		}

		String uri = bytestream.getURI();
		if (!uri.startsWith(vivoDefaultNamespace)) {
			updateLog.warn("uri does not start with the default namespace: '"
					+ uri + "'");
			return null;
		}

		Literal filenameLiteral = result.getLiteral("fn");
		if (filenameLiteral == null) {
			updateLog.error("Query result for '" + uri
					+ "' contains no filename.");
			return null;
		}
		String filename = filenameLiteral.getString();

		return new BytestreamInfo(uri, filename);
	}

	/** Add an alias URL to each resource in the list. */
	private void addMissingValuesToModel(List<BytestreamInfo> list) {
		if (list.isEmpty()) {
			updateLog.warn("Query found no valid results.");
			return;
		}

		Property aliasProperty = model
				.createProperty(VitroVocabulary.FS_ALIAS_URL);

		for (BytestreamInfo bytestreamInfo : list) {
			String value = figureAliasUrl(bytestreamInfo);
			Resource resource = model.getResource(bytestreamInfo.uri);

			ModelWrapper.add(model, resource, aliasProperty, value);
			updateLog.log(resource, "added alias URL: '" + value + "'");
		}
	}

	/**
	 * Convert the bytestream URI and the filename into an alias URL. If
	 * problems, return null.
	 */
	String figureAliasUrl(BytestreamInfo bsi) {
		try {
			String remainder = bsi.uri.substring(vivoDefaultNamespace.length());
			String filename = URLEncoder.encode(bsi.filename, "UTF-8");
			String separator = remainder.endsWith("/") ? "" : "/";

			return FILE_PATH + remainder + separator + filename;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // No UTF-8? Can't happen.
		}
	}

	private QueryExecution createQueryExecutor(String queryString) {
		Query query = QueryFactory.create(queryString);
		return QueryExecutionFactory.create(query, model);
	}

	/** For debug logging. */
	@SuppressWarnings("unchecked")
	private List<String> listVariables(QuerySolution result) {
		List<String> list = new ArrayList<String>();
		for (Iterator<String> names = result.varNames(); names.hasNext();) {
			String name = names.next();
			RDFNode value = result.get(name);
			list.add(name + "=" + value);
		}
		return list;
	}

	private static class BytestreamInfo {
		final String uri;
		final String filename;

		BytestreamInfo(String uri, String filename) {
			this.uri = uri;
			this.filename = filename;
		}
	}
}
