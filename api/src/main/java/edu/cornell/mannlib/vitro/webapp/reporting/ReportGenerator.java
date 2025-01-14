/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.io.OutputStream;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * Interface for all reports
 */
public interface ReportGenerator {
    void addDatasource(DataSource dataSource);

    void setReportName(String name);

    /**
     * States the MIME type of the output from this instance. The MIME type may be
     * hardcoded, derived from the configuration, or derived from the request
     * parameters.
     * <p>
     * Called exactly once, after init().
     *
     * @return the MIME type of the (expected) output.
     * @throws ReportGeneratorException
     */
    String getContentType() throws ReportGeneratorException;

    /**
     * Generates the report directly into the specified output stream
     *
     * @param outputStream Stream to write report into
     * @throws ReportGeneratorException
     */
    void generateReport(OutputStream outputStream, RequestModelAccess request, UserAccount account) throws ReportGeneratorException;

    void setIsPersistent(boolean isPersistent);

    void setUri(String uri);

    String getClassName();

    List<DataSource> getDataSources();

    String getReportName();

    String getUri();

    boolean getImplementsTemplate();

    boolean getImplementsXml();

    boolean isPersistent();

    boolean isRunnable();
}
