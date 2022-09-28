package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.FileView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;

public class ReportGenerator extends Operation {

    private static final Log log = LogFactory.getLog(ReportGenerator.class);
    private static final String JSON_LOADER_TYPE = "json";
    protected Parameters inputParams = new Parameters();
    protected Parameters outputParams = new Parameters();
    protected Parameters dataSources = new Parameters();
    private Parameter templateFile;
    private Parameter reportFile;
    
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#temlateFile", minOccurs = 1, maxOccurs = 1)
    public void addTemplateFile(Parameter templateFile) throws InitializationException {
        if (!FileView.isFile(templateFile)) {
            throw new InitializationException("Only file parameters accepted on setTemplateFile");
        }
        this.templateFile = templateFile;
        inputParams.add(templateFile);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#dataSource", minOccurs = 1, maxOccurs = 1)
    public void addDataSource(Parameter dataSource) throws InitializationException {
        if (!JsonView.isJson(dataSource)) {
            throw new InitializationException("Only json data sources accepted on addDataSource");
        }
        inputParams.add(dataSource);
        dataSources.add(dataSource);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#resultFile", minOccurs = 1, maxOccurs = 1)
    public void addReportFile(Parameter reportFile) throws InitializationException {
        if (!FileView.isFile(reportFile)) {
            throw new InitializationException("Only file parameters accepted on addReportFile");
        }
        this.reportFile = reportFile;
        outputParams.add(reportFile);
    }
        
    @Override
    public OperationResult run(DataStore dataStore) {
        if(!isValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        OperationResult result = OperationResult.ok();

        // get from dataStore
        byte[] template = null;
        ReportBuilder builder = new ReportBuilder();
        ReportTemplateBuilder templateBuilder = new ReportTemplateBuilder();
        templateBuilder.documentContent(template);
        templateBuilder.documentName("name");
        // TODO: Delete if not needed
        templateBuilder.documentPath("/");
        builder.template(templateBuilder.build());

        Reporting reporting = new Reporting();
        final JsonDataLoader jsonDataLoader = new JsonDataLoader();
        final DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory().setJsonDataLoader(jsonDataLoader);
        
        //Json loader
        reporting.setLoaderFactory(loaderFactory);
        //Json formatter
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        final RunParams runParams = new RunParams(builder.build());
        final HashMap<String, Object> reportParams = createReportParams(dataStore, builder);
        runParams.params(reportParams);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        reporting.runReport(runParams, baos);
        FileView.setFileContent(dataStore, reportFile, baos);
        return result;
    }

    private HashMap<String, Object> createReportParams(DataStore dataStore, ReportBuilder builder) {
        HashMap<String, Object> reportParams = new HashMap<String, Object>();
        for (String dataSourceName : dataSources.getNames() ) {
            Parameter dataSourceParam = dataSources.get(dataSourceName);
            String json = JsonView.getJsonString(dataStore, dataSourceParam);
            reportParams.put(dataSourceName, json);
            String parameterPrefix = "parameter=" + dataSourceName;
            addBand(builder, dataSourceName + "_header", parameterPrefix + "_header $.head.vars[*]", JSON_LOADER_TYPE);
            addBand(builder, dataSourceName + "_footer", parameterPrefix + "_footer $.head.vars[*]" , JSON_LOADER_TYPE);
            addBand(builder, dataSourceName, parameterPrefix + " $.results.bindings[*]", JSON_LOADER_TYPE);
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

    private boolean isValid(DataStore dataStore) {
        boolean result = isValid();
        if (!isInputValid(dataStore)) {
            result = false;
        }
        return result;
    }

    private boolean isValid() {
        boolean result = true;
        if (templateFile == null) {
            log.error("Template file param is not provided in the configuration");
            result = false;
        }
        if (reportFile == null) {
            log.error("Report file param is not provided in the configuration");
            result = false;
        }
        if (dataSources.size() == 0) {
            log.error("No data sources provided in the configuration");
            result = false;
        }
        return result;
    }

    @Override
    public Parameters getInputParams() {
        return inputParams;
    }

    @Override
    public Parameters getOutputParams() {
        return outputParams;
    }

    @Override
    public void dereference() {
    }
}
