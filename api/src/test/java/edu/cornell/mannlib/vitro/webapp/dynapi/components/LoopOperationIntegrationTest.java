package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ResourceAPIPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

@RunWith(Parameterized.class)
public class LoopOperationIntegrationTest extends ServletContextTest {

	private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
	private static final String TEST_ACTION = RESOURCES_PATH + "loop-operation-test-action.n3";

	Model storeModel;

	@org.junit.runners.Parameterized.Parameter(0)
	public String uri;

	@org.junit.runners.Parameterized.Parameter(1)
	public String value;

	@Before
	public void beforeEach() {
		storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
	}
	
    @After
    public void reset() {
        setup();

        ActionPool rpcPool = ActionPool.getInstance();
        rpcPool.init(servletContext);
        rpcPool.reload();
        assertEquals(0, rpcPool.count());
    }

    private ActionPool initWithDefaultModel() throws IOException {
        loadOntology(ontModel);
        ActionPool rpcPool = ActionPool.getInstance();
        rpcPool.init(servletContext);
        return rpcPool;
    }
    
    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException {
        Logger.getLogger(ResourceAPIPool.class).setLevel(Level.INFO);
        Logger.getLogger(ActionPool.class).setLevel(Level.INFO);
        loadModel(ontModel, TEST_ACTION);
        ActionPool rpcPool = initWithDefaultModel();
        Action action = null;
        try { 
            action = rpcPool.getByUri("test:action");
            assertFalse(action instanceof NullAction);
            assertTrue(action.isValid());
            Parameters inputParameters = action.getInputParams();
            DataStore store = new DataStore();
        } finally {
            if (action != null) {
                action.removeClient();    
            }
        }
    }

	@Parameterized.Parameters
	public static Collection<Object[]> requests() {
		return Arrays.asList(new Object[][] {
		    {"test", "test"}
		});
	}

	protected void loadModel(Model model, String... files) throws IOException {
		for (String file : files) {
		    System.out.println(file);
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
