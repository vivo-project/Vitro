/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import virtuoso.jena.driver.VirtDataset;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 */
public class RDFServiceVirtuosoJena extends RDFServiceJena {
	private final static Log log = LogFactory.getLog(RDFServiceVirtuosoJena.class);

	private ConnectionPoolDataSource ds;

//	private StaticDatasetFactory staticDatasetFactory;

	public RDFServiceVirtuosoJena(ConnectionPoolDataSource dataSource) {
		this.ds = dataSource;
//		this.staticDatasetFactory = new StaticDatasetFactory(getDataset());
	}

	@Override
	protected DatasetWrapper getDatasetWrapper() {
//		if (staticDatasetFactory != null) {
//			return staticDatasetFactory.getDatasetWrapper();
//		}

		log.info("Getting Virtuoso Dataset Wrapper");
		return new VirtuosoDatasetWrapper(ds);
	}

	@Override
	public boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException {
		if (changeSet.getPreconditionQuery() != null
				&& !isPreconditionSatisfied(
				changeSet.getPreconditionQuery(),
				changeSet.getPreconditionQueryType())) {
			return false;
		}

		VirtDataset dataset = null;
		try {
			insureThatInputStreamsAreResettable(changeSet);

			dataset = getDataset();

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

	private VirtDataset getDataset() {
		VirtDataset dataset = new VirtDataset(ds);
		dataset.setReadFromAllGraphs(true);
		return dataset;
	}

	@Override
	public void close() {
	}

	@Override
	public InputStream sparqlConstructQuery(String query,
											ModelSerializationFormat resultFormat) throws RDFServiceException {
		log.info("Virtuoso Construct query: " + query.replace("\n",""));
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlConstructQuery(query, resultFormat);
		} finally {
			dataset.end();
			dataset.close();
			log.info("Virtuoso Construct end");
		}
	}

	@Override
	public void sparqlConstructQuery(String query, Model model) throws RDFServiceException {
		log.info("Virtuoso Construct query: " + query.replace("\n",""));
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			super.sparqlConstructQuery(query, model);
		} finally {
			dataset.end();
			dataset.close();
			log.info("Virtuoso Construct end");
		}
	}

	@Override
	public InputStream sparqlDescribeQuery(String query,
										   ModelSerializationFormat resultFormat) throws RDFServiceException {
		log.info("Virtuoso Select query: " + query.replace("\n",""));
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlDescribeQuery(query, resultFormat);
		} finally {
			dataset.end();
			dataset.close();
			log.info("Virtuoso Select end");
		}
	}

	@Override
	public InputStream sparqlSelectQuery(String query, ResultFormat resultFormat)
			throws RDFServiceException {
		log.info("Virtuoso Select query: " + query.replace("\n",""));
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlSelectQuery(query, resultFormat);
		} finally {
			dataset.end();
			dataset.close();
			log.info("Virtuoso Select end");
		}
	}

	@Override
	public void sparqlSelectQuery(String query, ResultSetConsumer consumer)
			throws RDFServiceException {
		log.info("Virtuoso Select query: " + query.replace("\n",""));
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			super.sparqlSelectQuery(query, consumer);
		} finally {
			dataset.end();
			dataset.close();
			log.info("Virtuoso Select end");
		}
	}

	@Override
	public boolean sparqlAskQuery(String query) throws RDFServiceException {
		log.info("Virtuoso ASK query: " + query.replace("\n",""));
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			return super.sparqlAskQuery(query);
		} finally {
			dataset.end();
			dataset.close();
			log.info("Virtuoso Ask end");
		}
	}

	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			return super.getGraphURIs();
		} finally {
			dataset.end();
			dataset.close();
		}
	}

	@Override
	public void serializeAll(OutputStream outputStream)
			throws RDFServiceException {
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			super.serializeAll(outputStream);
		} finally {
			dataset.end();
			dataset.close();
		}
	}

	@Override
	public void serializeGraph(String graphURI, OutputStream outputStream)
			throws RDFServiceException {
		Dataset dataset = getDataset();
		dataset.begin(ReadWrite.READ);
		try {
			super.serializeGraph(graphURI, outputStream);
		} finally {
			dataset.end();
			dataset.close();
		}
	}

	private class VirtuosoDatasetWrapper extends DatasetWrapper {
		private final ConnectionPoolDataSource dataSource;
		private ThreadLocal<VirtDataset> datasets = new ThreadLocal<>();

		public VirtuosoDatasetWrapper(ConnectionPoolDataSource dataSource) {
			super(null);
			this.dataSource = dataSource;
		}

		@Override
		public synchronized Dataset getDataset() {
			log.info("Getting Virtuoso Dataset from Wrapper");
			VirtDataset dataset = datasets.get();
			if (dataset == null) {
				dataset = new VirtDataset(dataSource);
				dataset.setReadFromAllGraphs(true);
				datasets.set(dataset);
			}

			return dataset;
		}

		@Override
		public synchronized void close() {
			log.info("Closing Virtuoso Dataset from Wrapper");
			VirtDataset dataset = datasets.get();
			if (dataset != null) {
				dataset.close();
				datasets.remove();
			}
		}
	}
}

