/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import org.apache.commons.lang3.StringUtils;

/**
 * Base implementation for reports that use the Yet Another Report Generator
 * library
 */
public abstract class AbstractYARGTemplateReport extends AbstractTemplateReport {
    /**
     * Generate the report
     */
    protected void generateReport(OutputStream outputStream, String name, ReportOutputType type,
            RequestModelAccess request, UserAccount account) {
        // Create a new report builder and template
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportTemplateBuilder reportTemplateBuilder = new ReportTemplateBuilder();

        // Document path and name is required but unused
        reportTemplateBuilder.documentPath("/");
        reportTemplateBuilder.documentName(name);

        // Add the template as the document content
        reportTemplateBuilder.documentContent(template);

        // Set the output type according to the implementation
        reportTemplateBuilder.outputType(type);

        // Build the template and add it to the report builder
        reportBuilder.template(reportTemplateBuilder.build());

        // Add the data from the data sources
        Map<String, Object> params = new HashMap<String, Object>();
        for (DataSource dataSource : getDataSources()) {
            // Get the output of the datasource
            String body = dataSource.getBody(new HashMap<>(), request, account);
            if (!StringUtils.isEmpty(body)) {
                // Bind the output to the name given in the datasource configuration
                params.put(dataSource.getOutputName(), body);

                // Create a bandbuilder for the header and bind it to the output name configured
                BandBuilder headBand = new BandBuilder();
                headBand.name(dataSource.getOutputName() + "_header");
                headBand.query(dataSource.getOutputName() + "_header",
                        "parameter=" + dataSource.getOutputName() + "_header $.head.vars[*]", "json");
                headBand.orientation(BandOrientation.HORIZONTAL);
                reportBuilder.band(headBand.build());

                // Create a bandbuilder for the results and bind it to the output name
                // configured
                BandBuilder resultsBand = new BandBuilder();
                resultsBand.name(dataSource.getOutputName());
                resultsBand.query(dataSource.getOutputName(),
                        "parameter=" + dataSource.getOutputName() + " $.results.bindings[*]", "json");
                resultsBand.orientation(BandOrientation.HORIZONTAL);
                reportBuilder.band(resultsBand.build());

                // Create a bandbuilder for the footer and bind it to the output name configured
                BandBuilder footerBand = new BandBuilder();
                footerBand.name(dataSource.getOutputName() + "_footer");
                footerBand.query(dataSource.getOutputName() + "_footer",
                        "parameter=" + dataSource.getOutputName() + "_footer $.head.vars[*]", "json");
                footerBand.orientation(BandOrientation.HORIZONTAL);
                reportBuilder.band(footerBand.build());

            }
        }

        // Create a new reporting object
        Reporting reporting = new Reporting();

        // Attach a JSON loader and formatter
        reporting.setLoaderFactory(new DefaultLoaderFactory().setJsonDataLoader(new JsonDataLoader()));
        reporting.setFormatterFactory(new DefaultFormatterFactory());

        // Run the report writing to the output stream
        reporting.runReport((new RunParams(reportBuilder.build())).params(params), outputStream);
    }
}
