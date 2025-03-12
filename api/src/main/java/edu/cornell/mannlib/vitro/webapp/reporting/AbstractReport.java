/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_DATASOURCE;
import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_REPORTNAME;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Base implementation for reports
 */
public abstract class AbstractReport implements ReportGenerator {
    protected String uri;
    protected String reportName;
    protected List<DataSource> dataSources = new ArrayList<>();
    protected boolean isPersistent;

    @Property(uri = PROPERTY_DATASOURCE)
    public void addDatasource(DataSource dataSource) {
        dataSources.add(dataSource);
    }

    @Property(uri = PROPERTY_REPORTNAME)
    public void setReportName(String name) {
        this.reportName = name;
    }

    public void setIsPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public List<DataSource> getDataSources() {
        dataSources.sort(new DatasourceComparator());
        return dataSources;
    }

    public String getReportName() {
        return reportName;
    }

    public String getUri() {
        return uri;
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    /**
     * Helper method for the UI
     */
    public boolean getImplementsTemplate() {
        return false;
    }

    /**
     * Helper method for the UI
     */
    public boolean getImplementsXml() {
        return false;
    }

    /**
     * Report is runnable if there is a datasource set
     */
    public boolean isRunnable() {
        return dataSources.size() > 0;
    }

    private class DatasourceComparator implements Comparator<DataSource> {
        @Override
        public int compare(DataSource ds1, DataSource ds2) {
            if (ds1 == null || ds2 == null) {
                return 0;
            }

            return ds1.getRank() - ds2.getRank();
        }
    }
}
