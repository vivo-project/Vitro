/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.query.Dataset;
import org.apache.jena.shared.ClosedException;

public class DatasetWrapper {

    private Dataset dataset;
    private boolean closed = false;

    /**
     * Initialize the data set.
     *
     * @param dataset The data set.
     */
    public DatasetWrapper(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Get the data set.
     *
     * @return The data set
     */
    public Dataset getDataset() {
        if (!closed) {
            return dataset;
        } else throw new RuntimeException("No operations on a closed dataset");
    }

    /**
     * Set the data set closed boolean.
     *
     * This is normally handled internally.
     * This should only be used for exceptional cases.
     *
     * Setting this to true or false neither opens nor closes the data set.
     *
     * @param closed The data set closed state.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Close the data.
     *
     * This does nothing if already closed according to the closed boolean.
     *
     * This sets the closed state to true.
     */
    public void close() {
        // FIXME: This is causing NPEs and connection already closed errors in tests.
        //        Not closing the dataset can result in excessive memory or other resource usage.
        /*if (!closed) {
            closed = true;
            try {
                dataset.close();
            } catch (ClosedException e) {
                // Do nothing.
            }
        }*/
    }

}
