package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.poi.openxml4j.opc.internal.ContentTypeManager;
import org.apache.solr.common.StringUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.utils.ResourceUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

@RunWith(Parameterized.class)
public class ReportEndpointIntegrationTest extends ServletContextTest {
	private static final String REPORT = "report";
	private static final String QUERY_MODEL = "querymodel";
	private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/ReportGenerator/";

	Model storeModel;

	@org.junit.runners.Parameterized.Parameter(0)
	public String actionPath;

	@org.junit.runners.Parameterized.Parameter(1)
	public String storePath;

	@org.junit.runners.Parameterized.Parameter(2)
	public String extension;

	@BeforeClass
	public static void before() {
	    offLogs();
        offLog(Docx4jProperties.class);
        offLog(ResourceUtils.class);
        offLog(XmlUtils.class);
        offLog(TraversalUtil.class);
        offLog(SaveToZipFile.class);
        offLog(ContentTypeManager.class);
	}
	
	@Before
	public void beforeEach() {
		storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
	}
	
	@AfterClass
	public static void after() {
	    restoreLogs();
        restoreLog(Docx4jProperties.class);
        restoreLog(ResourceUtils.class);
        restoreLog(XmlUtils.class);
        restoreLog(TraversalUtil.class);
        restoreLog(SaveToZipFile.class);
        restoreLog(ContentTypeManager.class);
	}
	
	@After
	public void reset() {
	   
	}
	@Test
	public void testNull() {
	    
	}
	
	public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException {
		loadOntology(ontModel);
		loadModel(ontModel, ABOX_PREFIX + actionPath);
		loadModel(storeModel, RESOURCES_PATH + storePath);
		DataStore store = new DataStore();
		Procedure procedure = loader.loadInstance("https://vivoweb.org/ontology/vitro-dynamic-api/procedure/create_report_generator", Procedure.class);
		assertTrue(procedure.isValid());
		Parameters inputParameters = procedure.getInputParams();
		Parameter paramQueryModel = inputParameters.get(QUERY_MODEL);
		assertNotNull(paramQueryModel);
		Data queryModelData = new Data(paramQueryModel);
		TestView.setObject(queryModelData, storeModel);
		store.addData(QUERY_MODEL, queryModelData);
		Converter.convertInternalParams(procedure.getInternalParams(), store);
		OperationResult opResult = procedure.run(store);
		assertFalse(opResult.hasError());
		assertTrue(store.contains(REPORT));
		final Data data = store.getData(REPORT);
		assertTrue(TestView.getObject(data) != null);
		final String base64EncodedReport = data.getSerializedValue();
		assertTrue(!StringUtils.isEmpty(base64EncodedReport));

		boolean manualDebugging = false;
		if (manualDebugging) {
			byte[] reportBytes = Base64.getDecoder().decode(base64EncodedReport);
			File file = new File(RESOURCES_PATH + "integration-test-report" + extension);
			try (OutputStream os = new FileOutputStream(file)) {
				os.write(reportBytes);
			}	
		}
	}

	@Parameterized.Parameters
	public static Collection<Object[]> requests() {
		return Arrays.asList(new Object[][] { 
			{ "endpoint_procedure_create_report.n3", "report-generator-store.n3", ".xlsx" },
		});
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
    }
}
