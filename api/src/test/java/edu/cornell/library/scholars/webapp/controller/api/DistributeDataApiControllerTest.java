/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccessStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;

/**
 * TODO
 */
public class DistributeDataApiControllerTest extends AbstractTestClass {
    private static final String DDIST_URI_1 = "http://test/distributor1";
    private static final String DDIST_URI_2 = "http://test/distributor2";
    private static final String ACTION_NAME_PROPERTY = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#actionName";
    private static final String FAIL_METHOD_PROPERTY = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#failMethod";

    private HttpServletRequestStub req;
    private HttpServletResponseStub resp;
    private RequestModelAccessStub requestModels;
    private DistributeDataApiController controller;

    @Before
    public void setup() {
        req = new HttpServletRequestStub();
        resp = new HttpServletResponseStub();
        requestModels = new ModelAccessFactoryStub().get(req);

        controller = new DistributeDataApiController();
    }

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------

    @Test
    public void emptyAction_400_badRequest()
            throws ServletException, IOException {
        setActionPath("");
        populateDisplayModel(
                dd(DDIST_URI_1, DDTestDistributor.class, "simpleSuccess"));
        runIt(400, "'action' path was not provided.");
    }

    @Test
    public void unrecognizedAction_400_badRequest()
            throws ServletException, IOException {
        setActionPath("unknown");
        populateDisplayModel(
                dd(DDIST_URI_1, DDTestDistributor.class, "simpleSuccess"));
        runIt(400, "Did not find a DataDistributor for 'unknown'");
    }

    @Test
    public void multipleActions_warning() throws ServletException, IOException {
        StringWriter w = new StringWriter();
        captureLogOutput(DistributeDataApiController.class, w, true);

        setActionPath("multiples");
        populateDisplayModel(
                dd(DDIST_URI_1, DDTestDistributor.class, "multiples"),
                dd(DDIST_URI_2, DDTestDistributor.class, "multiples"));
        runIt(200, "success");

        assertTrue(w.toString()
                .contains("more than one DataDistributor for 'multiples'"));
    }

    @Test
    public void failToInstantiate_500_serverError()
            throws ServletException, IOException {
        setLoggerLevel(DistributeDataApiController.class, Level.ERROR);
        setActionPath("simpleSuccess");
        populateDisplayModel(
                dd(DDIST_URI_1, TestFailure.class, "simpleSuccess"));
        runIt(500, "Failed to instantiate");
    }

    @Test
    public void initThrowsException_500_serverError()
            throws ServletException, IOException {
        setLoggerLevel(DistributeDataApiController.class, Level.OFF);
        setActionPath("initFails");
        populateDisplayModel(
                dd(DDIST_URI_1, DDTestDistributor.class, "initFails", "init"));
        runIt(500, "forced error in init()");
    }

    @Test
    public void getContextTypeThrowsException_500_serverError()
            throws ServletException, IOException {
        setLoggerLevel(DistributeDataApiController.class, Level.OFF);
        setActionPath("getContentTypeFails");
        populateDisplayModel(dd(DDIST_URI_1, DDTestDistributor.class,
                "getContentTypeFails", "getContentType"));
        runIt(500, "forced error in getContentType()");
    }

    @Test
    public void writeOutputThrowsException_500_serverError()
            throws ServletException, IOException {
        setLoggerLevel(DistributeDataApiController.class, Level.OFF);
        setActionPath("writeOutputFails");
        populateDisplayModel(dd(DDIST_URI_1, DDTestDistributor.class,
                "writeOutputFails", "writeOutput"));
        runIt(500, "forced error in writeOutput()");
    }

    @Test
    public void simpleSuccess() throws ServletException, IOException {
        setActionPath("simpleSuccess");
        populateDisplayModel(
                dd(DDIST_URI_1, DDTestDistributor.class, "simpleSuccess"));
        runIt(200, "success");
    }

    @Test
    public void actionPrefixedBySlash_success()
            throws ServletException, IOException {
        setActionPath("/simpleSuccess");
        populateDisplayModel(
                dd(DDIST_URI_1, DDTestDistributor.class, "simpleSuccess"));
        runIt(200, "success");
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private void setActionPath(String actionPath) {
        req.setRequestUrlByParts("http://test", "", "/dataRequest", actionPath);
    }

    private void populateDisplayModel(Model... models) {
        OntModel om = ModelFactory.createOntologyModel();
        for (Model model : models) {
            om.add(model);
        }
        requestModels.setOntModel(om, ModelNames.DISPLAY);
    }

    private Model dd(String uri, Class<?> class1, String actionName,
            String... failMethods) {
        Model m = model(typeStatement(uri, toJavaUri(class1)),
                typeStatement(uri, toJavaUri(DataDistributor.class)),
                dataProperty(uri, ACTION_NAME_PROPERTY, actionName));

        for (String failMethod : failMethods) {
            m.add(dataProperty(uri, FAIL_METHOD_PROPERTY, failMethod));
        }

        return m;
    }

    private void runIt(int expectedStatus, String expectedOutput)
            throws ServletException, IOException {
        controller.doGet(req, resp);
        assertEquals(expectedStatus, resp.getStatus());
        String actualOutput = resp.getOutput();
        if (!actualOutput.contains(expectedOutput)) {
            fail("expect output to contain>" + expectedOutput + "<but was>"
                    + actualOutput + "<");
        }
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    public static class DDTestDistributor implements DataDistributor {
        private Set<String> failureMethods = new HashSet<>();

        @SuppressWarnings("unused")
        @Property(uri = ACTION_NAME_PROPERTY)
        public void setActionName(String actionName) {
            // Nothing to do
        }

        @Property(uri = FAIL_METHOD_PROPERTY)
        public void addFailureMethod(String methodName) {
            failureMethods.add(methodName);
        }

        @Override
        public void init(DataDistributorContext ddContext)
                throws DataDistributorException {
            if (failureMethods.contains("init")) {
                throw new DataDistributorException("forced error in init()");
            }
        }

        @Override
        public String getContentType() throws DataDistributorException {
            if (failureMethods.contains("getContentType")) {
                throw new DataDistributorException(
                        "forced error in getContentType()");
            }
            return "text/plain";
        }

        @Override
        public void writeOutput(OutputStream output)
                throws DataDistributorException {
            if (failureMethods.contains("writeOutput")) {
                throw new DataDistributorException(
                        "forced error in writeOutput()");
            }
            try {
                output.write("success".getBytes());
            } catch (IOException e) {
                throw new ActionFailedException(e);
            }
        }

        @Override
        public void close() throws DataDistributorException {
            // Nothing to do
        }
    }

    public static class TestFailure {
        // Won't instantiate.
    }
}
