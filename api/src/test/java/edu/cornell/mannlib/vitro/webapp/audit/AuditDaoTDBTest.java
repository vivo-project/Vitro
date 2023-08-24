package edu.cornell.mannlib.vitro.webapp.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAO;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOFactory;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOTDB;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AuditDaoTDBTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private RDFService rdfService;

    @Before
    public void initializeModule() throws IOException {
        AuditDAOTDB.initialize(folder.getRoot().getAbsolutePath());
        AuditDAOFactory.initialize(AuditDAOFactory.Storage.AUDIT_TDB);
        Model model = ModelFactory.createDefaultModel();
        rdfService = new RDFServiceModel(model);
        AuditSetup auditSetup = new AuditSetup();
        auditSetup.registerChangeListener(rdfService);
    }

    @After
    public void closeModule() throws IOException {
        AuditDAOTDB.shutdown();
        AuditDAOFactory.shutdown();
    }

    @Test
    public void testInitialization() {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        assertNotNull(dao);
    }

    @Test
    public void testGetGraphs() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        List<String> graphList = dao.getGraphs();
        assertTrue(graphList.isEmpty());
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        graphList = dao.getGraphs();
        assertEquals(2, graphList.size());
        assertTrue(graphList.contains(ModelNames.ABOX_ASSERTIONS));
        assertTrue(graphList.contains(ModelNames.TBOX_ASSERTIONS));
    }

    @Test
    public void testGetUsers() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        List<String> userList = dao.getUsers();
        assertTrue(userList.isEmpty());
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        userList = dao.getUsers();
        assertEquals(2, userList.size());
        assertTrue(userList.contains("test:bob"));
        assertTrue(userList.contains("test:alice"));
    }

    @Test
    public void testFindWrongDate() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        AuditResults results = dao.find(0, 10, 0, 1, "", "", false);
        List<AuditChangeSet> datasets = results.getDatasets();
        assertEquals(0, datasets.size());
    }

    @Test
    public void testFindSpecificEditor() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        AuditResults results = dao.find(0, 10, 0, System.currentTimeMillis(), "test:bob", "", false);
        List<AuditChangeSet> datasets = results.getDatasets();
        assertEquals(1, datasets.size());
        assertEquals("test:bob", datasets.get(0).getUserId());
    }

    @Test
    public void testFindLimit() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        AuditResults results = dao.find(0, 1, 0, System.currentTimeMillis(), "", "", false);
        List<AuditChangeSet> datasets = results.getDatasets();
        assertEquals(1, datasets.size());
    }

    @Test
    public void testFindDescOrder() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        AuditResults results = dao.find(0, 10, 0, System.currentTimeMillis(), "", "", false);
        List<AuditChangeSet> datasets = results.getDatasets();
        assertEquals(2, datasets.size());
        assertEquals("test:alice", datasets.get(0).getUserId());
    }

    @Test
    public void testFindAscOrder() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        AuditResults results = dao.find(0, 10, 0, System.currentTimeMillis(), "", "", true);
        List<AuditChangeSet> datasets = results.getDatasets();
        assertEquals(2, datasets.size());
        assertEquals("test:bob", datasets.get(0).getUserId());
    }

    @Test
    public void testFindSpecificGraph() throws Exception {
        AuditDAO dao = AuditDAOFactory.getAuditDAO();
        addData("42", "test:bob", ModelNames.ABOX_ASSERTIONS, false);
        addData("77", "test:alice", ModelNames.TBOX_ASSERTIONS, true);
        AuditResults results = dao.find(0, 10, 0, System.currentTimeMillis(), "", ModelNames.TBOX_ASSERTIONS, false);
        List<AuditChangeSet> datasets = results.getDatasets();
        assertEquals(1, datasets.size());
        assertEquals(1, datasets.get(0).getGraphUris().size());
        assertEquals(ModelNames.TBOX_ASSERTIONS, datasets.get(0).getGraphUris().iterator().next());
    }

    public void addData(String propValue, String editorUri, String graphUri, boolean isAddition) throws Exception {
        ChangeSet cs = rdfService.manufactureChangeSet();
        String inputString = "<test:uri> <test:prop> \"" + propValue + "\" . ";
        try (InputStream stream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8))) {
            if (isAddition) {
                cs.addAddition(stream, RDFService.ModelSerializationFormat.N3, graphUri, editorUri);
            } else {
                cs.addRemoval(stream, RDFService.ModelSerializationFormat.N3, graphUri, editorUri);
            }
            rdfService.changeSetUpdate(cs);
        }
    }
}
