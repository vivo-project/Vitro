/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

public class DatasetWrapper {

    private SDBConnection conn;
    private Dataset dataset;
    private boolean closed = false;
    
    public DatasetWrapper(Dataset dataset) {
        this.dataset = dataset;
    }
    
    public DatasetWrapper(Dataset dataset, SDBConnection conn) {
        this.dataset = dataset;
        this.conn = conn;
    }
    
    public Dataset getDataset() {
        if (!closed) {
            return dataset;
        } else throw new RuntimeException("No operations on a closed dataset");
    }
    
    public void close() {
        if (!closed) {
            closed = true;
            if (conn != null) {
                dataset.close();
                conn.close();
            }
        }
    }
    
}
