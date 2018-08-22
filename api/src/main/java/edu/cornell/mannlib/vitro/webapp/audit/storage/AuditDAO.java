/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

import edu.cornell.mannlib.vitro.webapp.audit.AuditChangeSet;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;

/**
 * Interface for storing and retrieving Audit entries
 */
public interface AuditDAO {
    /**
     * Write the dataset to stroage
     *
     * @param dataset
     */
    void write(AuditChangeSet dataset);

    /**
     * Retrieve a set of audit entries for a given user
     *
     * @param userUri
     * @param offset
     * @param limit
     * @return
     */
    AuditResults findForUser(String userUri, long offset, int limit);
}
