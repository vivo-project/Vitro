package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.FileView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;

public class ReportGenerator extends Operation {

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
        Map<String, Object> params = new HashMap<String, Object>();

        
        Reporting reporting = new Reporting();
        final JsonDataLoader jsonDataLoader = new JsonDataLoader();
        final DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory().setJsonDataLoader(jsonDataLoader);
        
        //Json loader
        reporting.setLoaderFactory(loaderFactory);
        //Json formatter
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        final RunParams runParams = new RunParams(builder.build());
        runParams.params(params);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        reporting.runReport(runParams, baos);
        
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
        // TODO Auto-generated method stub
    }
}
