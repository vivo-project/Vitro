package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BinaryView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ByteArray;

public class ReportGeneratorTest {

	private static final String DATA_SOURCE = "dataSource";
	private static final String TEMPLATE = "template";
	private static final String REPORT = "report";
	private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/ReportGenerator/";
	private static final String XLSX_TEMPLATE = "test-report-template.xlsx";
	private static final String DOCX_TEMPLATE = "test-report-template.docx";

	private static final String testJsonString = "{\"head\":{\"vars\":[\"o\"]},\"results\":{\"bindings\":[{\"o\":{\"type\":\"literal\",\"value\":\"alice\"}}]}}";
	ReportGenerator generator;
	DataStore dataStore;

	@Before
	public void init() throws Exception {
		generator = new ReportGenerator();
		dataStore = new DataStore();
	}

	@Test
	public void testDocX() throws Exception {
		testReport(DOCX_TEMPLATE);
	}

	@Test
	public void testXlsX() throws Exception {
		testReport(XLSX_TEMPLATE);
	}

	public void testReport(String templatePath) throws Exception {
		Path p = new File(RESOURCES_PATH + templatePath).toPath();
		byte[] templateByteArray = Files.readAllBytes(p);
		System.out.println(templateByteArray.length);

		Parameter reportParam = ParameterUtils.createByteArrayParameter(REPORT);
		generator.addReport(reportParam);

		Parameter sourceParam = ParameterUtils.createJsonParameter(DATA_SOURCE);
		generator.addDataSource(sourceParam);
		Data sourceData = new Data(sourceParam);
		sourceData.setRawString(testJsonString);
		sourceData.initialization();
		dataStore.addData(DATA_SOURCE, sourceData);

		Parameter templateParam = ParameterUtils.createByteArrayParameter(TEMPLATE);
		generator.addTemplate(templateParam);
		Data templateData = new Data(templateParam);
		TestView.setObject(templateData, new ByteArray(templateByteArray));
		dataStore.addData(TEMPLATE, templateData);

		assertEquals(OperationResult.ok(), generator.run(dataStore));

		assertTrue(dataStore.contains(REPORT));
		byte[] reportBytes = BinaryView.getByteArray(dataStore, reportParam);
		assertTrue(reportBytes.length > templateByteArray.length);
		
		boolean manualDebugging = false;
		if (manualDebugging) {
			File file = new File(RESOURCES_PATH + "result-" + templatePath);
			try (OutputStream os = new FileOutputStream(file)) {
				os.write(reportBytes);
			}
		}
	}

}
