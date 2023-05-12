/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.audit.AuditChangeSet;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;

/**
 * Audit storage using a plain file system
 */
public class AuditDAOFS implements AuditDAO {
    // Directory
    private static File directory = null;

    // Formatting for dates
    private final SimpleDateFormat DATEFORMATTER = new SimpleDateFormat("yyyyMMddHHmmSS");

    // The current servlet request
    private HttpServletRequest request = null;

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

    /**
     * Create an AuditDAO for the given request
     *
     * @param req
     */
    public AuditDAOFS(ServletRequest req) {
        if (req instanceof HttpServletRequest) {
            request = (HttpServletRequest)req;
        }
    }

    @Override
    public void write(AuditChangeSet dataset) {
        // Must have a current request
        if (request == null) {
            return;
        }

        // Must have been initialized
        if (directory == null) {
            throw new IllegalStateException("Directory for storage has not been configured");
        }

        // Create a directory to log this request into, based on the date and UUID
        File logTo = new File(new File(directory, DATEFORMATTER.format(dataset.getRequestTime())), dataset.getUUID().toString());
        logTo.mkdirs();

        // Get the current user
        UserAccount acc = LoginStatusBean.getCurrentUser(request);

        // Record the user URI for this set of changes
        File userLog = new File(logTo, "user");
        try (Writer writer = new FileWriter(userLog)) {
            if (acc != null) {
                writer.write(acc.getUri());
            } else {
                writer.write("Unknown");
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        // Record any statements that have been added
        if (dataset.getAddedDataset() != null && !dataset.getAddedDataset().asDatasetGraph().isEmpty()) {
            File addedLog   = new File(logTo, "added");
            try (OutputStream os = new FileOutputStream(addedLog)) {
                RDFDataMgr.write(os, dataset.getAddedDataset(),   RDFFormat.TRIG_BLOCKS);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }

        // Record any statements that have been removed
        if (dataset.getRemovedDataset() != null && !dataset.getRemovedDataset().asDatasetGraph().isEmpty()) {
            File removedLog = new File(logTo, "removed");
            try (OutputStream os = new FileOutputStream(removedLog)) {
                RDFDataMgr.write(os, dataset.getRemovedDataset(), RDFFormat.TRIG_BLOCKS);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    }

    @Override
    public AuditResults findForUser(String userUri, long offset, int limit) {
        // Plain file system audit log is read only at this time
        throw new UnsupportedOperationException();
    }
}
