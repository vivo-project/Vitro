/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import java.util.List;

/**
 * Results object for retrieving entries from the audit store
 */
public class AuditResults {
    private final long total;
    private final long offset;
    private final long limit;

    private final List<AuditChangeSet> datasets;

    public AuditResults(long total, long offset, long limit, List<AuditChangeSet> datasets) {
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.datasets = datasets;
    }

    public long getLimit() {
        return limit;
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
