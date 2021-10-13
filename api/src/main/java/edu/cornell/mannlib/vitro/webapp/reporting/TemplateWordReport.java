/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.io.OutputStream;

import com.haulmont.yarg.structure.ReportOutputType;

/**
 * Generate a Word report using the YARG template processor
 */
public class TemplateWordReport extends AbstractYARGTemplateReport {
    @Override
    public String getContentType() throws ReportGeneratorException {
        // Return the docx mime type, including the UTF-8 character set
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=UTF-8";
    }

    @Override
    public void generateReport(OutputStream outputStream) throws ReportGeneratorException {
        generateReport(outputStream, "report.docx", ReportOutputType.docx);
    }
}
