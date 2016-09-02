/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;

/**
 * An implementation that is based on Jena TDB.
 */
public class RDFServiceTDB extends RDFServiceJena {
	private static final Log log = LogFactory.getLog(RDFServiceTDB.class);

	private final Dataset dataset;

	public RDFServiceTDB(String directoryPath) throws IOException {
		Path tdbDir = Paths.get(directoryPath);

		if (!Files.exists(tdbDir)) {
			Path parentDir = tdbDir.getParent();
			if (!Files.exists(parentDir)) {
				throw new IllegalArgumentException(
						"Cannot create TDB directory '" + tdbDir
								+ "': parent directory does not exist.");
			}
			Files.createDirectory(tdbDir);
		}

		this.dataset = TDBFactory.createDataset(directoryPath);
	}

	@Override
	protected DatasetWrapper getDatasetWrapper() {
		return new DatasetWrapper(dataset);
	}

	@Override
	public boolean changeSetUpdate(ChangeSet changeSet)
			throws RDFServiceException {

		if (changeSet.getPreconditionQuery() != null
				&& !isPreconditionSatisfied(changeSet.getPreconditionQuery(),
						changeSet.getPreconditionQueryType())) {
			return false;
		}

		try {
			insureThatInputStreamsAreResettable(changeSet);

			if (log.isDebugEnabled()) {
				log.debug("Change Set: " + changeSet);
			}
			notifyListenersOfPreChangeEvents(changeSet);

			dataset.begin(ReadWrite.WRITE);
			try {
				applyChangeSetToModel(changeSet, dataset);
				dataset.commit();
			} finally {
				dataset.end();
			}
			
			notifyListenersOfChanges(changeSet);
			notifyListenersOfPostChangeEvents(changeSet);
			return true;
		} catch (Exception e) {
			log.error(e, e);
			throw new RDFServiceException(e);
		}
	}

	
	@Override
	public void close() {
		if (this.dataset != null) {
			dataset.close();
		}
	}

	@Override
	public InputStream sparqlConstructQuery(String query,
			ModelSerializationFormat resultFormat) throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlConstructQuery(query, resultFormat);
		} finally {
			dataset.end();
		}
	}

	@Override
	public void sparqlConstructQuery(String query, Model model) throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			super.sparqlConstructQuery(query, model);
		} finally {
			dataset.end();
		}
	}

	@Override
	public InputStream sparqlDescribeQuery(String query,
			ModelSerializationFormat resultFormat) throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlDescribeQuery(query, resultFormat);
		} finally {
			dataset.end();
		}
	}

	@Override
	public InputStream sparqlSelectQuery(String query, ResultFormat resultFormat)
			throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlSelectQuery(query, resultFormat);
		} finally {
			dataset.end();
		}
	}

	@Override
	public void sparqlSelectQuery(String query, ResultSetConsumer consumer)
			throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			super.sparqlSelectQuery(query, consumer);
		} finally {
			dataset.end();
		}
	}

	@Override
	public boolean sparqlAskQuery(String query) throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlAskQuery(query);
		} finally {
			dataset.end();
		}
	}

	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			return super.getGraphURIs();
		} finally {
			dataset.end();
		}
	}

	@Override
	public void serializeAll(OutputStream outputStream)
			throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			super.serializeAll(outputStream);
		} finally {
			dataset.end();
		}
	}

	@Override
	public void serializeGraph(String graphURI, OutputStream outputStream)
			throws RDFServiceException {
		dataset.begin(ReadWrite.READ);
		try {
			super.serializeGraph(graphURI, outputStream);
		} finally {
			dataset.end();
		}
	}

	/**
	 * TDB has a bug: if given a literal of type xsd:nonNegativeInteger, it
	 * stores a literal of type xsd:integer.
	 * 
	 * To determine whether this serialized graph is equivalent to what's in
	 * TDB, we need to do the same.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI,
			InputStream serializedGraph,
			ModelSerializationFormat serializationFormat)
			throws RDFServiceException {
		return super.isEquivalentGraph(graphURI,
				adjustForNonNegativeIntegers(serializedGraph),
				serializationFormat);
	}

	/**
	 * TDB has a bug: if given a literal of type xsd:nonNegativeInteger, it
	 * stores a literal of type xsd:integer.
	 *
	 * To determine whether this serialized graph is equivalent to what's in
	 * TDB, we need to do the same.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI,
									 Model graph)
			throws RDFServiceException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		graph.write(buffer, "N-TRIPLE");
		InputStream inStream = new ByteArrayInputStream(buffer.toByteArray());
		return isEquivalentGraph(graphURI, inStream, ModelSerializationFormat.NTRIPLE);
	}

	/**
	 * Convert all of the references to "nonNegativeInteger" to "integer" in
	 * this serialized graph.
	 * 
	 * This isn't rigorous: it could fail if another property contained the text
	 * "nonNegativeInteger" in its name, or if that text were used as part of a
	 * string literal. If that happens before this TDB bug is fixed, we'll need
	 * to improve this method.
	 * 
	 * It also isn't scalable: if we wanted real scalability, we would write to
	 * a temporary file as we converted.
	 */
	private InputStream adjustForNonNegativeIntegers(InputStream serializedGraph)
			throws RDFServiceException {
		try {
			String raw = IOUtils.toString(serializedGraph, "UTF-8");
			String modified = raw.replace("nonNegativeInteger", "integer");
			return new ByteArrayInputStream(modified.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new RDFServiceException(e);
		}
	}
}
