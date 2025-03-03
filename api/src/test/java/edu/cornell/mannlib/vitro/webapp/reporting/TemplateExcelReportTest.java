package edu.cornell.mannlib.vitro.webapp.reporting;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Base64;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.ontology.OntModel;
import org.junit.Test;
import org.mockito.Mockito;

public class TemplateExcelReportTest {

    private static final String RESOURCES = "src/test/resources";
    private static final String REPORTING_DIR = RESOURCES + "/edu/cornell/mannlib/vitro/webapp/reporting";
    private static final String CONFIGURATION_PATH = REPORTING_DIR + "/display.n3";
    private static final String TEMPLATE_PATH = REPORTING_DIR + "/reportTemplate.xlsx";
    private static final String CONTENT_PATH = REPORTING_DIR + "/content.n3";

    private static final boolean debug = false;

    @Test
    public void testExcelReport() throws Exception {
        UserAccount account = new UserAccount();
        OntModel displayModel = VitroModelFactory.createOntologyModel();
        OntModel content = VitroModelFactory.createOntologyModel();
        content.read(CONTENT_PATH);
        RDFServiceModel contentService = new RDFServiceModel(content);

        RequestModelAccess rma = Mockito.mock(RequestModelAccess.class);
        when(rma.getOntModel(DISPLAY)).thenReturn(displayModel);
        when(rma.getRDFService()).thenReturn(contentService);
        when(rma.getOntModel(ModelNames.FULL_UNION)).thenReturn(content);

        displayModel.read(CONFIGURATION_PATH);
        byte[] template = Files.readAllBytes(new File(TEMPLATE_PATH).toPath());
        String string = Base64.getEncoder().encodeToString(template);
        TemplateExcelReport reportGenerator = new TemplateExcelReport();
        reportGenerator.addDatasource(getDataSource());
        reportGenerator.setTemplateBase64(string);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        reportGenerator.generateReport(baos, rma, account);
        assertFalse(baos.size() == 0);
        if (debug) {
            File file = new File(REPORTING_DIR + "/report.xlsx");
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(baos.toByteArray());
            }
        }
    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setOutputName("dataSource");
        dataSource.setDistributorName("dataSource");
        return dataSource;
    }
}
