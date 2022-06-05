package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public class ConditionTest extends ServletContextTest{

    private static final String TEST_DATA = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/conditions/conditions-test-data.n3";

    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException {
        loadOntology();
        loadModels("n3", TEST_DATA);
        Action action = loader.loadInstance("test:action", Action.class);
        //OperationResult result = action.run(null);
        assertTrue(action != null);
    }

}
