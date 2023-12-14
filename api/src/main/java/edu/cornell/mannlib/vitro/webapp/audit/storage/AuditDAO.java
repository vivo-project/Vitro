/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.audit.AuditChangeSet;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;

/**
 * Interface for storing and retrieving Audit entries
 */
public interface AuditDAO {

    /**
     * Write the dataset to storage
     *
     * @param dataset
     */
    void write(AuditChangeSet dataset);

    /**
     * Get list of users
     */
    List<String> getUsers();

    /**
     * Get list of graphs
     */
    List<String> getGraphs();

    /**
     * Retrieve a set of audit entries for a given user
     *
     * @param offset
     * @param limit
     * @param startDate
     * @param endDate
     * @param userUri
     * @param graphUri
     * @param order true = ASC, false = DESC
     * @return
     */
    AuditResults find(long offset, int limit, long startDate, long endDate, String userUri, String graphUri,
            boolean order);

}
