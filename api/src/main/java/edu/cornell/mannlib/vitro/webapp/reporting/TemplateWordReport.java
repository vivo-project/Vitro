/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.io.OutputStream;

import com.haulmont.yarg.structure.ReportOutputType;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

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
    public void generateReport(OutputStream outputStream, RequestModelAccess request, UserAccount account) throws ReportGeneratorException {
        generateReport(outputStream, "report.docx", ReportOutputType.docx, request, account);
    }
}
