/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

@RunWith(Parameterized.class)
public class ParameterSubstitutionIntegrationTest extends ServletContextTest {
    private static final String RESULT = "result";
    private static final String RESOURCES_PATH =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/ParameterSubstitution/";
    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

    @org.junit.runners.Parameterized.Parameter(0)
    public String procedureConfiguration;

    @org.junit.runners.Parameterized.Parameter(1)
    public String expectedValue;

    @BeforeClass
    public static void before() {
        dynapiModelFactory = mockStatic(DynapiModelFactory.class);
    }

    @AfterClass
    public static void after() {
        dynapiModelFactory.close();
    }

    @Before
    public void beforeEach() {
        // LoggingControl.offLogs();
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq(
                "http://vitro.mannlib.cornell.edu/default/dynamic-api-abox"))).thenReturn(ontModel);
    }

    @After
    public void reset() {
        // LoggingControl.restoreLogs();
        setup();
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        procedurePool.reload();
        assertEquals(0, procedurePool.count());
    }

    private ProcedurePool initWithDefaultModel() throws IOException {
        loadOntology(ontModel);
        loadModel(ontModel, RESOURCES_PATH + procedureConfiguration);
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init();
        return procedurePool;
    }

    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException,
            InitializationException {
        ProcedurePool procedurePool = initWithDefaultModel();
        DataStore store = new DataStore();
        UserAccount user = new UserAccount();
        user.setRootUser(true);
        store.setUser(user);
        try (Procedure procedure = procedurePool.getByUri("test:parameter-substitution-procedure")) {
            assertTrue(procedure.isValid());
            Converter.convertInternalParams(procedure.getInternalParams(), store);
            Endpoint.collectDependencies(procedure, store, procedurePool);
            OperationResult opResult = procedure.run(store);
            assertFalse(opResult.hasError());
            assertTrue(store.contains(RESULT));
            final Data data = store.getData(RESULT);
            assertTrue(TestView.getObject(data) != null);
            final String testResultValue = data.getSerializedValue();
            assertEquals(testResultValue, expectedValue);
        } finally {
            if (store != null) {
                store.removeDependencies();
            }
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "parameter-substitution-procedure.n3", "test string" },
                { "parameter-substitution-call-exteranal-procedure.n3", "test string" },
                { "parameter-substitution-call-exteranal-procedure-mirror-params.n3", "1" }, });
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
