/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.io.OutputStream;

import com.haulmont.yarg.structure.ReportOutputType;

/**
 * Generate an Excel report using the YARG template processor
 */
public class TemplateExcelReport extends AbstractYARGTemplateReport {
    @Override
    public String getContentType() throws ReportGeneratorException {
        // Return the xlsx mime type include the UTF-8 character set
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8";
    }

    @Override
    public void generateReport(OutputStream outputStream) throws ReportGeneratorException {
        generateReport(outputStream, "report.xlsx", ReportOutputType.xlsx);
    }
}
