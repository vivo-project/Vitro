/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import static edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.PARAMETER_PURGE;
import static edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.PARAMETER_SOURCE_FILE;
import static edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.PARAMETER_WHICH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.BadRequestException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

/**
 * Load from a dump file of NQuads, or something equivalent.
 * 
 * We could process it all a line at a time, except for the blank nodes. If
 * there are two references to the same blank node, they must be processed in
 * the same method call to the RDFService.
 * 
 * So, process each line as it comes in, unless it contains a blank node. Lines
 * with blank nodes get put into buckets, with one bucket for each model (and
 * one for the default model). At the end, we'll empty each of the buckets.
 * 
 * And if they ask to purge the models before restoring, do that.
 */
public class RestoreModelsAction extends AbstractDumpRestoreAction {
	private static final Log log = LogFactory.getLog(RestoreModelsAction.class);

	private static final String DEFAULT_GRAPH_URI = "__default__";

	private final FileItem sourceFile;
	private final WhichService which;
	private final boolean purge;
	private final SelfLimitingTripleBuckets bnodeBuckets;
	private final SelfLimitingTripleBuckets easyBuckets;

	private long tripleCount;

	RestoreModelsAction(HttpServletRequest req, HttpServletResponse resp)
			throws BadRequestException {
		super(req);
		this.sourceFile = getFileItem(PARAMETER_SOURCE_FILE);
		this.which = getEnumFromParameter(WhichService.class, PARAMETER_WHICH);
		this.purge = null != req.getParameter(PARAMETER_PURGE);

		this.bnodeBuckets = new SelfLimitingTripleBuckets(this, 1000000);
		this.easyBuckets = new SelfLimitingTripleBuckets(this, 10000);
	}

	private FileItem getFileItem(String key) throws BadRequestException {
		FileItem fileItem = new VitroRequest(req).getFileItem(key);
		if (fileItem == null) {
			throw new BadRequestException("Request has no file item named '"
					+ key + "'");
		}
		return fileItem;
	}

	long restoreModels() throws IOException, RDFServiceException {
		purgeIfRequested();
		return doTheRestore();
	}

	private void purgeIfRequested() throws RDFServiceException {
		if (!purge) {
			return;
		}

		log.info("Purging the " + which + " models.");
		RDFService rdfService = getRdfService(which);
		RDFServiceDataset dataset = new RDFServiceDataset(rdfService);
		for (String graphUri : rdfService.getGraphURIs()) {
			Model m = dataset.getNamedModel(graphUri);
			log.info("Remove " + m.size() + " triples from " + graphUri);
			m.removeAll();
		}
		log.info("Purge is complete.");
	}

	private long doTheRestore() throws IOException, RDFServiceException {
		log.info("Restoring the " + which + " models.");
		long lineCount = 0;
		try (InputStream is = sourceFile.getInputStream();
				DumpParser p = new NquadsParser(is)) {
			for (DumpQuad line : p) {
				bucketize(line);
				lineCount++;
				if (lineCount % 10000 == 0) {
					log.info("read " + lineCount + " lines.");
				}
			}
			emptyBuckets();
		}
		log.info("Restore is complete.");
		return lineCount;
	}

	private void bucketize(DumpQuad quad) throws IOException,
			RDFServiceException {
		DumpTriple triple = quad.getTriple();
		if (triple.getS().isBlank() || triple.getO().isBlank()) {
			bnodeBuckets.add(quad.getG().getValue(), triple);
		} else {
			easyBuckets.add(quad.getG().getValue(), triple);
		}
	}

	private void emptyBuckets() throws IOException, RDFServiceException {
		for (String key : easyBuckets.getKeys()) {
			processTriples(key, easyBuckets.getTriples(key));
		}
		for (String key : bnodeBuckets.getKeys()) {
			processTriples(key, bnodeBuckets.getTriples(key));
		}
	}

	private void processTriples(String graphUri, Collection<DumpTriple> triples)
			throws IOException, RDFServiceException {
		if (graphUri.equals(DEFAULT_GRAPH_URI)) {
			graphUri = null;
		}
		RDFService rdfService = getRdfService(which);
		ChangeSet change = rdfService.manufactureChangeSet();
		change.addAddition(serialize(triples),
				ModelSerializationFormat.NTRIPLE, graphUri);

		rdfService.changeSetUpdate(change);

		tripleCount += triples.size();
		log.info("processed " + tripleCount + " triples.");
	}

	private InputStream serialize(Collection<DumpTriple> triples)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer w = new OutputStreamWriter(out, "UTF-8");
		for (DumpTriple triple : triples) {
			w.write(triple.toNtriples());
		}
		w.close();
		return new ByteArrayInputStream(out.toByteArray());
	}

	private static class SelfLimitingTripleBuckets {
		private final RestoreModelsAction parent;
		private final int sizeLimit;
		private final Map<String, List<DumpTriple>> map = new HashMap<>();

		public SelfLimitingTripleBuckets(RestoreModelsAction parent,
				int sizeLimit) {
			this.parent = parent;
			this.sizeLimit = sizeLimit;
		}

		public void add(String key, DumpTriple triple) throws IOException,
				RDFServiceException {
			key = nonNull(key, DEFAULT_GRAPH_URI);
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<DumpTriple>());
			}
			map.get(key).add(triple);

			if (map.get(key).size() > sizeLimit) {
				parent.processTriples(key, map.remove(key));
			}
		}

		public Set<String> getKeys() {
			return map.keySet();
		}

		public List<DumpTriple> getTriples(String key) {
			key = nonNull(key, DEFAULT_GRAPH_URI);
			if (map.containsKey(key)) {
				return map.get(key);
			} else {
				return Collections.emptyList();
			}
		}

		private String nonNull(String value, String defaultValue) {
			return (value == null) ? defaultValue : value;
		}
	}

}
