/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BinaryView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class ReportGenerator extends AbstractOperation {

    private static final String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final Log log = LogFactory.getLog(ReportGenerator.class);
    private static final String JSON_LOADER_TYPE = "json";
    private static final Set<String> supportedTypes = new HashSet<>(Arrays.asList(MIME_XLSX, MIME_DOCX));
    private static final Map<String, ReportOutputType> outputTypes = new HashMap<>();
    private static final Map<String, String> fileNames = new HashMap<>();
    static {
        fileNames.put(MIME_XLSX, "report.xlsx");
        fileNames.put(MIME_DOCX, "report.docx");
        outputTypes.put(MIME_XLSX, ReportOutputType.xlsx);
        outputTypes.put(MIME_DOCX, ReportOutputType.docx);
    }
    protected Parameters dataSources = new Parameters();
    private Parameter templateParam;
    private Parameter reportParam;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#template", minOccurs = 1, maxOccurs = 1)
    public void addTemplate(Parameter templateParam) throws InitializationException {
        if (!BinaryView.isByteArray(templateParam)) {
            throw new InitializationException("Only file parameters accepted on setTemplateFile");
        }
        this.templateParam = templateParam;
        inputParams.add(templateParam);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#dataSource", minOccurs = 1)
    public void addDataSource(Parameter dataSource) throws InitializationException {
        if (!JsonView.isJsonNode(dataSource)) {
            throw new InitializationException("Only json data sources accepted on addDataSource");
        }
        inputParams.add(dataSource);
        dataSources.add(dataSource);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#report", minOccurs = 1, maxOccurs = 1)
    public void addReport(Parameter reportParam) throws InitializationException {
        if (!BinaryView.isByteArray(reportParam)) {
            throw new InitializationException("Only file parameters accepted on addReportFile");
        }
        this.reportParam = reportParam;
        outputParams.add(reportParam);
    }

    @Override
    public OperationResult runOperation(DataStore dataStore) {
        // get from dataStore
        byte[] template = BinaryView.getByteArray(dataStore, templateParam);
        String type = detectType(template);
        if (!supportedTypes.contains(type)) {
            log.error("Template type '" + type + "' is not supported by " + ReportGenerator.class.getSimpleName());
            return OperationResult.internalServerError();
        }
        ReportBuilder builder = new ReportBuilder();
        ReportTemplateBuilder templateBuilder = new ReportTemplateBuilder();
        templateBuilder.documentContent(template);
        templateBuilder.documentName(fileNames.get(type));
        templateBuilder.outputType(outputTypes.get(type));

        // TODO: Delete if not needed
        templateBuilder.documentPath("/");
        builder.template(templateBuilder.build());

        Reporting reporting = new Reporting();
        JsonDataLoader jsonDataLoader = new JsonDataLoader();
        DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory().setJsonDataLoader(jsonDataLoader);

        // Json loader
        reporting.setLoaderFactory(loaderFactory);
        // Json formatter
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        RunParams runParams = new RunParams(builder.build());
        HashMap<String, Object> reportParams = createReportParams(dataStore, builder);
        runParams.params(reportParams);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        reporting.runReport(runParams, baos);
        BinaryView.setByteArray(dataStore, reportParam, baos);
        return OperationResult.ok();
    }

    private String detectType(byte[] template) {
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        try {
            parser.parse(new ByteArrayInputStream(template), handler, metadata, context);
        } catch (Exception e) {
            log.error(e, e);
        }
        return metadata.get("Content-Type");
    }

    private HashMap<String, Object> createReportParams(DataStore dataStore, ReportBuilder builder) {
        HashMap<String, Object> reportParams = new HashMap<String, Object>();
        for (String dataSourceName : dataSources.getNames()) {
            Parameter dataSourceParam = dataSources.get(dataSourceName);
            String json = JsonView.getJsonString(dataStore, dataSourceParam);
            reportParams.put(dataSourceName, json);
            String parameterPrefix = "parameter=" + dataSourceName;
            addBand(builder, dataSourceName + "_header", parameterPrefix + "_header $.head.vars[*]", JSON_LOADER_TYPE);
            addBand(builder, dataSourceName + "_footer", parameterPrefix + "_footer $.head.vars[*]", JSON_LOADER_TYPE);
            final String mainScript = parameterPrefix + "$.results.bindings[*]";
            addBand(builder, dataSourceName, mainScript, JSON_LOADER_TYPE);
        }
        return reportParams;
    }

    private void addBand(ReportBuilder builder, final String name, final String script, final String loaderType) {
        BandBuilder band = new BandBuilder();
        band.name(name);
        band.query(name, script, loaderType);
        band.orientation(BandOrientation.HORIZONTAL);
        builder.band(band.build());
    }

    public boolean isValid() {
        boolean result = true;
        if (templateParam == null) {
            log.error("Template file param is not provided in the configuration");
            result = false;
        }
        if (reportParam == null) {
            log.error("Report file param is not provided in the configuration");
            result = false;
        }
        if (dataSources.size() == 0) {
            log.error("No data sources provided in the configuration");
            result = false;
        }
        return result;
    }
}
