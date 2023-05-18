package ca.uqam.vitro.testbench;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualBufferedSDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.startup.StartupManager;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModelBuilder;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class IndividualBufferedSDBTest {
    private static final Log log = LogFactory.getLog(IndividualBufferedSDBTest.class);

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
    public void testGetMostSpecificTypeURIs() {
        String iriString = "http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca";
        /*
         * get information from IndividualBufferedSDB
         */
        WebappDaoFactory daoFact = vreq.getBufferedIndividualWebappDaoFactory();
        IndividualDao iDao = daoFact.getIndividualDao();
        Individual individual = iDao.getIndividualByURI(iriString);
        OntModel _buffOnt = null;
        if (individual instanceof IndividualBufferedSDB) {
            ((IndividualBufferedSDB) individual).populateIndividualBufferModel(iriString);
            _buffOnt=((IndividualBufferedSDB) individual).getBuffOntModel();

        }
        List<String> uris = individual.getMostSpecificTypeURIs();
        for (Iterator iterator = uris.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            log.info(string);
        }
        /*
         * get information from IndividualSDB
         */
        log.info("DONE!");
    }

}
