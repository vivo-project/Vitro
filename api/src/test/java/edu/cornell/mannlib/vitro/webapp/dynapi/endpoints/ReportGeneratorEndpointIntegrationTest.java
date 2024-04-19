/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;

import com.haulmont.yarg.formatters.impl.XlsxFormatter;
import com.haulmont.yarg.reporting.Reporting;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.AbstractTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

@RunWith(Parameterized.class)
public class ReportGeneratorEndpointIntegrationTest extends AbstractTest {

    private static final String REPORT_GENERATOR_URI = "resource_id";
    private static final String RESOURCES_PATH =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
    private static final String CREATE_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_create_report_generator.n3";
    private static final String EXECUTE_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_execute_report_generator.n3";
    private static final String DELETE_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_delete_report_generator.n3";
    private static final String LIST_REPORT_GENERATORS_PROCEDURE = "endpoint_procedure_list_report_generators.n3";
    private static final String GET_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_get_report_generator.n3";
    private static final String EXPORT_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_export_report_generator.n3";
    private static final String IMPORT_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_import_report_generator.n3";
    private static final String REPORT_ENDPOINT_INPUT = RESOURCES_PATH +
            "endpoint_procedure_create_report_generator_input1.n3";
    private static final String REPORT_ENDPOINT_INPUT2 = RESOURCES_PATH +
            "endpoint_procedure_create_report_generator_input2.n3";

    private static final String REPORT_ENDPOINT_DATA = RESOURCES_PATH +
            "endpoint_procedure_create_report_generator_demo_data.n3";

    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

    OntModel storeModel = ModelFactory.createOntologyModel();

    @org.junit.runners.Parameterized.Parameter(0)
    public String inputFileName;

    @org.junit.runners.Parameterized.Parameter(1)
    public String dataFileName;

    @AfterClass
    public static void after() {
        dynapiModelFactory.close();
    }

    @BeforeClass
    public static void before() {
        dynapiModelFactory = mockStatic(DynapiModelFactory.class);
    }

    @Before
    public void beforeEach() {
        LoggingControl.offLog(Reporting.class);
        LoggingControl.offLog(XlsxFormatter.class);
        LoggingControl.offLog(SpreadsheetMLPackage.class);
        LoggingControl.offLog(DocPropsCustomPart.class);
        LoggingControl.offLog(SaveToZipFile.class);
        LoggingControl.offLogs();
        storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq(
                "http://vitro.mannlib.cornell.edu/default/dynamic-api-abox"))).thenReturn(ontModel);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq("vitro:jenaOntModel"))).thenReturn(storeModel);

    }

    @After
    public void reset() {
        setup();
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        procedurePool.reload();
        assertEquals(0, procedurePool.count());
        LoggingControl.restoreLogs();
        LoggingControl.restoreLog(Reporting.class);
        LoggingControl.restoreLog(XlsxFormatter.class);
        LoggingControl.restoreLog(SpreadsheetMLPackage.class);
        LoggingControl.restoreLog(DocPropsCustomPart.class);
        LoggingControl.restoreLog(SaveToZipFile.class);
    }

    private ProcedurePool initWithDefaultModel() throws IOException {
        loadOntology(ontModel);
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        return procedurePool;
    }

    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException,
            InitializationException {
        ProcedurePool procedurePool = initWithDefaultModel();
        DataStore store = null;
        long initialModelSize;
        long initialProcedureCount;
        Model generatorConfiguration;
        UserAccount user = new UserAccount();
        user.setRootUser(true);
        boolean manualDebugging = false;

        long modelSizeWithReportGenerator;
        long procedureCountWithReportGenerator;
        try (Procedure procedure = procedurePool.getByUri("https://vivoweb.org/procedure/create_report_generator")) {
            assertFalse(procedure instanceof NullProcedure);
            assertTrue(procedure.isValid());
            initialModelSize = ontModel.size();
            initialProcedureCount = procedurePool.count();
            ;
            Parameters internal = procedure.getInternalParams();
            store = new DataStore();
            store.setUser(user);
            Converter.convertInternalParams(internal, store);
            Endpoint.collectDependencies(procedure, store, procedurePool);
            assertTrue(OperationResult.ok().equals(procedure.run(store)));
            modelSizeWithReportGenerator = ontModel.size();
            procedureCountWithReportGenerator = procedurePool.count();
            assertTrue(modelSizeWithReportGenerator > initialModelSize);
            assertTrue(procedureCountWithReportGenerator > initialProcedureCount);

            Data modelData = store.getData("report_generator_configuration_graph");
            generatorConfiguration = (Model) TestView.getObject(modelData);
            assertFalse(generatorConfiguration.isEmpty());
            if (manualDebugging) {
                File file = new File(RESOURCES_PATH + "create-report-generator-integration-test-report-generator.n3");
                FileWriter fw = new FileWriter(file);
                generatorConfiguration.write(fw, "n3");
            }
        } finally {
            if (store != null) {
                store.removeDependencies();
            }
        }

        DataStore reportStore = new DataStore();
        reportStore.setUser(user);
        Data uriData = store.getData(REPORT_GENERATOR_URI);
        assertTrue(uriData != null);
        reportStore.addData(uriData.getParam().getName(), uriData);
        try (Procedure reportGenerator = procedurePool.getByUri(
                "https://vivoweb.org/procedure/execute_report_generator");) {
            Parameters reportInternalParams = reportGenerator.getInternalParams();
            Converter.convertInternalParams(reportInternalParams, reportStore);
            assertTrue(OperationResult.ok().equals(reportGenerator.run(reportStore)));
            Data reportData = reportStore.getData("report");
            String base64EncodedReport = reportData.getSerializedValue();
            assertFalse(base64EncodedReport.isEmpty());
            if (manualDebugging) {
                byte[] reportBytes = Base64.getDecoder().decode(base64EncodedReport);
                File file = new File(RESOURCES_PATH + "create-report-generator-integration-test-report.xlsx");
                try (OutputStream os = new FileOutputStream(file)) {
                    os.write(reportBytes);
                }
            }
        }
        DataStore listReportStore = new DataStore();
        listReportStore.setUser(user);
        listReportStore.addData(uriData.getParam().getName(), uriData);

        try (Procedure listReportGenerators = procedurePool.getByUri(
                "https://vivoweb.org/procedure/list_report_generators");) {
            Parameters internalParams = listReportGenerators.getInternalParams();
            Converter.convertInternalParams(internalParams, listReportStore);
            assertTrue(OperationResult.ok().equals(listReportGenerators.run(listReportStore)));
            Data reportsData = listReportStore.getData("reports");
            String reports = reportsData.getSerializedValue();
            assertTrue(reports.contains(uriData.getSerializedValue()));
            if (manualDebugging) {
                System.out.println(reports);
            }
        }

        DataStore getReportStore = new DataStore();
        getReportStore.setUser(user);
        getReportStore.addData(uriData.getParam().getName(), uriData);

        try (Procedure getReportGenerator = procedurePool.getByUri(
                "https://vivoweb.org/procedure/get_report_generator");) {
            Parameters internalParams = getReportGenerator.getInternalParams();
            Converter.convertInternalParams(internalParams, getReportStore);
            assertTrue(OperationResult.ok().equals(getReportGenerator.run(getReportStore)));
            Data reportData = getReportStore.getData("result");
            String report = reportData.getSerializedValue();
            assertTrue(report.contains(uriData.getSerializedValue()));
            assertTrue(report.contains("selectQuery"));
            assertTrue(report.contains("constructQuery"));
            assertTrue(report.contains("template"));
            if (manualDebugging) {
                System.out.println(report);
            }
        }

        DataStore exportReportStore = new DataStore();
        exportReportStore.setUser(user);
        Data exportedData;
        exportReportStore.addData(uriData.getParam().getName(), uriData);
        try (Procedure exportReportGenerator = procedurePool.getByUri(
                "https://vivoweb.org/procedure/export_report_generator");) {
            Parameters internalParams = exportReportGenerator.getInternalParams();
            Converter.convertInternalParams(internalParams, exportReportStore);
            assertTrue(OperationResult.ok().equals(exportReportGenerator.run(exportReportStore)));
            exportedData = exportReportStore.getData("report_generator_configuration_graph");
            Model exportedModel = (Model) TestView.getObject(exportedData);
            Model notAddedData = generatorConfiguration.difference(exportedModel);
            Model excessivelyAddedData = exportedModel.difference(generatorConfiguration);
            if (manualDebugging) {
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" +
                        excessivelyAddedData.size());
                excessivelyAddedData.write(System.out, "n3");
                System.out.println("------------------------------------------------------------" +
                        notAddedData.size());
                notAddedData.write(System.out, "n3");
            }
            assertTrue(notAddedData.isEmpty());
            assertTrue(excessivelyAddedData.isEmpty());
        }

        DataStore deleteReportStore = new DataStore();
        deleteReportStore.setUser(user);
        deleteReportStore.addData(uriData.getParam().getName(), uriData);
        try (Procedure deleteReportGenerator = procedurePool.getByUri(
                "https://vivoweb.org/procedure/delete_report_generator");) {
            Parameters internalParams = deleteReportGenerator.getInternalParams();
            Converter.convertInternalParams(internalParams, deleteReportStore);
            assertTrue(OperationResult.ok().equals(deleteReportGenerator.run(deleteReportStore)));
            Data removeData = deleteReportStore.getData("report_generator_configuration_graph");
            Model removeModel = (Model) TestView.getObject(removeData);
            Model notRemoved = generatorConfiguration.difference(removeModel);
            Model excessivelyRemoved = removeModel.difference(generatorConfiguration);
            if (manualDebugging) {
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" +
                        excessivelyRemoved.size());
                excessivelyRemoved.write(System.out, "n3");
                System.out.println("------------------------------------------------------------" + notRemoved.size());
                notRemoved.write(System.out, "n3");
            }
            assertTrue(notRemoved.isEmpty());
            assertTrue(excessivelyRemoved.isEmpty());
            assertTrue(ontModel.size() == initialModelSize);
            assertTrue(procedurePool.count() == initialProcedureCount);
        }

        reportStore = new DataStore();
        reportStore.setUser(user);
        assertTrue(uriData != null);
        reportStore.addData(uriData.getParam().getName(), uriData);
        try (Procedure reportGenerator = procedurePool.getByUri(
                "https://vivoweb.org/procedure/execute_report_generator");) {
            Parameters reportInternalParams = reportGenerator.getInternalParams();
            Converter.convertInternalParams(reportInternalParams, reportStore);
            OperationResult result = reportGenerator.run(reportStore);
            assertTrue((new OperationResult(404)).equals(result));
        }

        DataStore importReportStore = new DataStore();
        importReportStore.addData(exportedData.getParam().getName(), exportedData);
        try (Procedure importReportGenerator = procedurePool.getByUri(
                "https://vivoweb.org/procedure/import_report_generator");) {
            Parameters internalParams = importReportGenerator.getInternalParams();
            Converter.convertInternalParams(internalParams, importReportStore);
            assertTrue(OperationResult.ok().equals(importReportGenerator.run(importReportStore)));
            Data importedUriData = importReportStore.getData(REPORT_GENERATOR_URI);
            assertTrue(uriData.getSerializedValue().equals(importedUriData.getSerializedValue()));
            assertTrue(ontModel.size() == modelSizeWithReportGenerator);
            assertTrue(procedurePool.count() == procedureCountWithReportGenerator);
        }
    }

    protected void loadModel(Model model, String... files) throws IOException {
        for (String file : files) {
            String rdf = readFile(file);
            model.read(new StringReader(rdf), null, "n3");
        }
    }

    public void loadOntology(OntModel ontModel) throws IOException {
        loadModel(ontModel, IMPLEMENTATION_FILE_PATH);
        loadModel(ontModel, ONTOLOGY_FILE_PATH);
        loadModel(ontModel, getFileList(ABOX_PREFIX));
        loadModel(ontModel, ABOX_PREFIX + CREATE_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + EXECUTE_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + DELETE_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + LIST_REPORT_GENERATORS_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + GET_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + EXPORT_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + IMPORT_REPORT_GENERATOR_PROCEDURE);

        loadModel(ontModel, inputFileName);
        loadModel(storeModel, dataFileName);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { REPORT_ENDPOINT_INPUT, REPORT_ENDPOINT_DATA },
                { REPORT_ENDPOINT_INPUT2, REPORT_ENDPOINT_DATA }, });
    }
}
