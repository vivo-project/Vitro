/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import java.util.List;

/**
 * Results object for retrieving entries from the audit store
 */
public class AuditResults {
    private long total;
    private long offset;
    private long limit;
    private List<AuditChangeSet> datasets;

    public AuditResults(long total, long offset, long limit, List<AuditChangeSet> datasets) {
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.datasets = datasets;
    }

    public long getTotal() {
        return total;
    }

    public long getOffset() {
        return offset;
    }

    public List<AuditChangeSet> getDatasets() {
        return datasets;
    }
}
