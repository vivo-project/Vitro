/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

import javax.servlet.ServletRequest;
import java.io.File;

/**
 * Implementation of Audit store that uses Jena TDB
 */
public class AuditDAOTDB extends AuditDAOJena {
    // The TDB instance
    private static Dataset dataset = null;

    public AuditDAOTDB(ServletRequest req) {
        super(req);
    }

    /**
     * Initialize the Jena TFB storage
     *
     * @param tdbPath
     */
    public static void initialize(String tdbPath) {
        // If we've already initialized, throw an exception
        if (dataset != null) {
            throw new IllegalStateException("Already initialised AuditDAOTDB");
        }

        // Create the directories if necessary
        File dir = new File(tdbPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // If the path is pointing to a file rather than a directory, something has gone wrong!!
        if (dir.isFile()) {
            throw new IllegalStateException("Path for the Audit TDB models must be a directory, not a file");
        }

        // Create the TDB dataset
        dataset = TDBFactory.createDataset(tdbPath);
    }

    /**
     * Shutdown the dataset
     */
    public static void shutdown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    /**
     * Return the store
     *
     * @return
     */
    protected Dataset getDataset() {
        return dataset;
    }
}
