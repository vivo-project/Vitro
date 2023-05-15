/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

import edu.cornell.mannlib.vitro.webapp.audit.AuditChangeSet;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Audit storage using a plain file system
 */
public class AuditDAOFS implements AuditDAO {
    private static final Log log = LogFactory.getLog(AuditDAOFS.class.getName());

    // Directory
    private static File directory = null;

    // Formatting for dates
    private final SimpleDateFormat DATEFORMATTER = new SimpleDateFormat("yyyyMMddHHmmSS");

    /**
     * Initialize the directory to be used
     *
     * @param path
     */
    public static void initialize(String path) {
        directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void write(AuditChangeSet dataset) {
        // Must have been initialized
        if (directory == null) {
            throw new IllegalStateException("Directory for storage has not been configured");
        }

        // Create a directory to log this request into, based on the date and UUID
        File logTo = new File(new File(directory, DATEFORMATTER.format(dataset.getRequestTime())), dataset.getUUID().toString());
        logTo.mkdirs();

        // Get the current user
        //UserAccount acc = LoginStatusBean.getCurrentUser(request);

        // Record the user URI for this set of changes
        File userLog = new File(logTo, "user");
        try (Writer writer = new FileWriter(userLog)) {
                writer.write(dataset.getUserId());
        } catch (Exception e) {
            log.error(e, e);
        }

        // Record any statements that have been added
        if (dataset.getAddedDataset() != null && !dataset.getAddedDataset().asDatasetGraph().isEmpty()) {
            File addedLog   = new File(logTo, "added");
            try (OutputStream os = new FileOutputStream(addedLog)) {
                RDFDataMgr.write(os, dataset.getAddedDataset(),   RDFFormat.TRIG_BLOCKS);
            } catch (Exception e) {
                log.error(e, e);
            }
        }

        // Record any statements that have been removed
        if (dataset.getRemovedDataset() != null && !dataset.getRemovedDataset().asDatasetGraph().isEmpty()) {
            File removedLog = new File(logTo, "removed");
            try (OutputStream os = new FileOutputStream(removedLog)) {
                RDFDataMgr.write(os, dataset.getRemovedDataset(), RDFFormat.TRIG_BLOCKS);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

    @Override
    public AuditResults find(long offset, int limit, long startDate, long endDate, String userUri, String graphUri, boolean order) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getGraphs() {
        throw new UnsupportedOperationException();
    }
}
