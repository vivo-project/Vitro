package ca.uqam.vitro.testbench;

import static org.junit.Assert.*;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

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
        log.info("DONE!");
    }

    private WebappDaoFactory daoFact;

    private IndividualDao iDao;

    private Individual individual;

    private String iriString;

    private Instant d1;

    private Instant d2;

    private Instant d3;

    private String message;

    private void analyse() {
        String calleMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        d3 = Instant.now();
        message ="\tANALYSER: " + 
        " total time :(" + ChronoUnit.MILLIS.between(d1, d3) / 1000.0 + ")" +
        " functionCall time :(" + ChronoUnit.MILLIS.between(d2, d3) / 1000.0 + ")";
        printMessage(calleMethodName);
    }


    private void printMessage() {
        String calleMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    private void printMessage(String calleMethodName) {
        log.info(calleMethodName+ ": " + message);
    }

    @Before
    public void setUp() throws Exception {
        iriString = "http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca";
    }

    private void setupBufferedIndividualContext() {
        d1 = Instant.now();
        daoFact = vreq.getBufferedIndividualWebappDaoFactory();
        iDao = daoFact.getIndividualDao();
        individual = iDao.getIndividualByURI(iriString);
        OntModel _buffOnt = null;
        if (individual instanceof IndividualBufferedSDB) {
            ((IndividualBufferedSDB) individual).populateIndividualBufferModel(iriString);
            _buffOnt = ((IndividualBufferedSDB) individual).getBuffOntModel();
        }
        d2 = Instant.now();
    }

    private void setupIndividualContext() {
        d1 = Instant.now();
        daoFact = vreq.getWebappDaoFactory();
        iDao = daoFact.getIndividualDao();
        individual = iDao.getIndividualByURI(iriString);
        d2 = Instant.now();
    }

    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testGetMainImageUri() {
        setupIndividualContext();
        String mainImgUri = individual.getMainImageUri();
        analyse();
        message="mainImgUri " + mainImgUri;
        printMessage();

    }

    @Test
    public void testGetMainImageUriBuffered() {
        setupBufferedIndividualContext();
        String mainImgUri = individual.getMainImageUri();
        analyse();
        message="mainImgUri " + mainImgUri;
        printMessage();
    }
    @Test
    public void testGetMostSpecificTypeURIs() {
        /*
         * get information from IndividualSDB
         */
        setupIndividualContext();
        List<String> uris = individual.getMostSpecificTypeURIs();
        for (Iterator iterator = uris.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            log.info("IndividualSDB " + string);
        }
        analyse();
    }
    @Test
    public void testGetMostSpecificTypeURIsBuffered() {
        setupBufferedIndividualContext();
        /*
         * get information from IndividualBufferedSDB
         */
        List<String> buffUris = individual.getMostSpecificTypeURIs();
        for (Iterator iterator = buffUris.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            log.info("IndividualBufferedSDB " + string);
        }
        analyse();
    }
    @Test
    public void testManyFunctions() {
        setupIndividualContext();
        individual.getMostSpecificTypeURIs();
        individual.getMainImageUri();
        individual.getImageUrl();
        individual.getName();
        individual.getNamespace();
        individual.getThumbUrl();
        analyse();
    }

    @Test
    public void testManyFunctionsBuffered() {
        setupBufferedIndividualContext();
        individual.getMostSpecificTypeURIs();
        individual.getMainImageUri();
        individual.getImageUrl();
        individual.getName();
        individual.getNamespace();
        individual.getThumbUrl();
        analyse();
    }
}
