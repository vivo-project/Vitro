package edu.cornell.mannlib.vitro.webapp.controller.json;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.startup.StartupManager;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class GetRenderedSearchIndividualsByVClassTest {

    private static ServletContextStub ctx;
    private static ServletContextEvent sce;
    private static VitroRequest vreq;
    private static HttpSessionStub session;
    private static HttpServletRequestStub hreq;
    private static StartupManager sm;
    private static StartupStatus ss;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("vitro.home", (new File("src/test/testbench")).getAbsolutePath() + "/vivo/home");
        session = new HttpSessionStub();
        hreq = new HttpServletRequestStub();
        ctx = new ServletContextStub();
        sm = new StartupManager();

        sce = new ServletContextEvent(ctx);
        ss = StartupStatus.getBean(ctx);
        session.setServletContext(ctx);
        hreq.setSession(session);
        vreq = new VitroRequest(hreq);
        sm.contextInitialized(sce, false);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testProcess() throws IOException {
        HttpServletResponse resp = new HttpServletResponseStub();
        new GetRenderedSearchIndividualsByVClass(vreq).process(resp );
    }

}
