/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.Task;

/**
 * TODO
 */
public class RebuildIndexTask implements Task {

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		throw new RuntimeException("RebuildIndexTask.run() not implemented.");

	}

	/* (non-Javadoc)
	 * @see edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.Task#getStatus()
	 */
	@Override
	public SearchIndexerStatus getStatus() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"RebuildIndexTask.getStatus() not implemented.");

	}

	@Override
	public void notifyWorkUnitCompletion(Runnable workUnit) {
		// TODO Auto-generated method stub
		throw new RuntimeException("RebuildIndexTask.notifyWorkUnitCompletion() not implemented.");
		
	}

}
