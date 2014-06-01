/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;

/**
 * TODO
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
			
			applyChangeSetToModel(changeSet, dataset);
			TDB.sync(dataset);
			
			notifyListenersOfChanges(changeSet);
			notifyListenersOfPostChangeEvents(changeSet);

			return true;
		} catch (Exception e) {
			log.error(e, e);
			throw new RDFServiceException(e);
		}
	}

}
