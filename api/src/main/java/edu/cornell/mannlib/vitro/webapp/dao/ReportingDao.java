/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.reporting.ReportGenerator;

/**
 * Interface for retrieving and updating reporting configurations
 */
public interface ReportingDao {
    // URIs for report configuration properties
    public final static String PROPERTY_REPORTNAME = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#reportName";
    public final static String PROPERTY_DATASOURCE = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#dataSource";
    public final static String PROPERTY_TEMPLATE = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#template";

    public final static String PROPERTY_DISTRIBUTORNAME = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#distributorName";
    public final static String PROPERTY_DISTRIBUTORRANK = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#distributorRank";
    public final static String PROPERTY_OUTPUTNAME = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#outputName";

    /**
     * Get a report by the URI
     */
    ReportGenerator getReportByUri(String uri);

    /**
     * Get a report by it's name
     */
    ReportGenerator getReportByName(String name);

    /**
     * Get all configured reports
     */
    List<ReportGenerator> getAllReports();

    /**
     * Update a given report
     */
    boolean updateReport(String uri, ReportGenerator report);

    /**
     * Delete the report configuration for the uri
     */
    boolean deleteReport(String uri);

    /**
     * Is the report configuration part of the persistent triple store
     */
    boolean isPersistent(String uri);
}