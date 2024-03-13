/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;

@RunWith(Parameterized.class)
public class ResourceGeneratorTest {

    private static final String NAMESPACE = "http://namespace/";

    private WebappDaoFactory wadf = new WebappDaoFactoryStub();

    @org.junit.runners.Parameterized.Parameter(0)
    public String format;

    @org.junit.runners.Parameterized.Parameter(1)
    public String expectedUri;

    private ResourceGenerator generator;

    @Before
    public void beforeEach() {
        generator = new ResourceGenerator();
        generator.init(wadf);
    }

    @Test
    public void createNewResource() throws InitializationException {
        Resource result = generator.getUriFromFormat(format);
        assertTrue(result != null);
        assertTrue(result.toString().matches(expectedUri));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                {
                NAMESPACE + "," + ResourceGenerator.JAVA_UUID,
                NAMESPACE + "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
                },
                { NAMESPACE + "," + ResourceGenerator.JAVA_UUID_NO_DASH, NAMESPACE + "[0-9a-f]{32}" },
                { NAMESPACE + "," + ResourceGenerator.JAVA_UUID_BASE62, NAMESPACE + "[0-9a-zA-Z]{1,23}" },
                { NAMESPACE + "n," + ResourceGenerator.JAVA_UUID_NUMBER, NAMESPACE + "n[0-9]{40}" },
                { NAMESPACE + "n," + ResourceGenerator.OLD_NUMBER, NAMESPACE + "n[0-9]{1,10}" }, });
    }
}
