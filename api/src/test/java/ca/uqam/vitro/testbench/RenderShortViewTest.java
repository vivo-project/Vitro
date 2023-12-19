package ca.uqam.vitro.testbench;

import static org.junit.Assert.fail;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetRenderedSearchIndividualsByVClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.IndividualFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDBBuffered;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WebappDaoFactoryOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.WebappDaoFactoryKey;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.startup.StartupManager;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModelBuilder;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class RenderShortViewTest {
    private static final Log log = LogFactory.getLog(RenderShortViewTest.class);

    private static ServletContextStub ctx;
    private static ServletContextEvent sce;
    private static VitroRequest vreq;
    private static HttpSessionStub session;
    private static HttpServletRequestStub hreq;
    private static StartupManager sm;
    private static StartupStatus ss;

    /**
     * @throws Exception
     * @see src/test/testbench/webapp/WEB-INF/resources/startup_listeners.txt to
     *      configure the listeners to load by StartupManager
     */
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
    public void testMultipleEntriesWithBufferedWDF() {
//        WebappDaoFactoryOption options;
        // LogManager.getRootLogger().setLevel(Level.DEBUG);
        WebappDaoFactory daoFact = vreq.getBufferedIndividualWebappDaoFactory();
        doTest(daoFact);

    }
    
//    @Test
//    public void testMultipleEntriesWithWDF() {
////        WebappDaoFactoryOption options;
//        // LogManager.getRootLogger().setLevel(Level.DEBUG);
//        WebappDaoFactory daoFact = vreq.getWebappDaoFactory();
//        doTest(daoFact);
//    }

private void doTest(WebappDaoFactory daoFact) {
    List<String> irisList = buildIrisList();
    Instant d1 = Instant.now();
    for (Iterator iterator = irisList.iterator(); iterator.hasNext();) {
        String individualUri = (String) iterator.next();
        IndividualDao iDao = daoFact.getIndividualDao();
        Individual individual = iDao.getIndividualByURI(individualUri);
        LogManager.getRootLogger().setLevel(Level.DEBUG);
        evaluate(individual);
    }
    Instant d2 = Instant.now();
    int totalIndv = irisList.size();
    long totalTime = ChronoUnit.MILLIS.between(d1, d2);
//  LogManager.getRootLogger().setLevel(Level.INFO);
    log.info("ANALYSER: total indv:(" + totalIndv + ") "
          + "total time :(" + totalTime / 1000.0 + ") sec. "
          + "- avrg time :(" + (totalTime / totalIndv) / 1000.0+") sec.");    }

private List<String> buildIrisList() {
    List<String> irisList = new ArrayList<>();
//    irisList.add("http://vivo-demo.uqam.ca/individual/abergel_elisabeth_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/adjiwanou_visseho_uqam_ca");
    irisList.add("http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/ajib_wessam_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/alhaji_ahmad_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/alandry_aymeric_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/alessandra_amandine_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/cadieux_alexandre_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/alexeeva_olga_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/allard_s_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/allen_marie_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/alloing_camille_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/amamou_salem_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/amiot_catherine_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/amireault_valerie_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/ananian_priscilla_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/anaya_arenas_ana_maria_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/ancelovici_marcos_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/angenot_valerie_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/annabi_borhane_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/Charrette_anne_marie_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/belanger_anouk_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/apostolov_vestislav_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/araujo_oliveira_anderson_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/arcand_manon_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/archambault_denis_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/armony_victor_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/arroyo_pardo_paulina_uqam_ca");
//    irisList.add("http://vivo-demo.uqam.ca/individual/arseneault_paul_uqam_ca");
//    irisList.add("http://localhost:8080/vivo_i18n/individual/n6870");
    return irisList;
    }

//    @Test
//    public void testRemoteProcess() {
//        String individualUri = "http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca";
//        WebappDaoFactoryOption options;
//        // LogManager.getRootLogger().setLevel(Level.DEBUG);
//        WebappDaoFactory daoFact = vreq.getBufferedIndividualWebappDaoFactory();
//        IndividualDao iDao = daoFact.getIndividualDao();
//        Individual individual = iDao.getIndividualByURI(individualUri);
//        evaluate(individual);
//        log.info("DONE");
//    }
//
//    @Test
//    public void testLocalProcess() {
//        String individualUri = "http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca";
//        WebappDaoFactoryOption options;
////      LogManager.getRootLogger().setLevel(Level.DEBUG);
//        WebappDaoFactory daoFact = vreq.getWebappDaoFactory();
//        IndividualDao iDao = daoFact.getIndividualDao();
//        Individual individual = iDao.getIndividualByURI(individualUri);
//        evaluate(individual);
//        log.info("DONE");
//    }

//    @Test
//    public void testGetRenderedSearchIndividualsByVClass() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetSearchIndividualsByVClasses() {
//        fail("Not yet implemented");
//    }
    private void evaluate(Individual anIndividual) {
        Map<String, Object> modelMap = new HashMap<String, Object>();
        modelMap.put("individual", IndividualTemplateModelBuilder.build(anIndividual, vreq));
        modelMap.put("vclass", "Professeur");
        ShortViewService svs = ShortViewServiceSetup.getService(ctx);
//        LogManager.getRootLogger().setLevel(Level.DEBUG);
//        Instant t1 = Instant.now();
        String rsv = svs.renderShortView(anIndividual, ShortViewContext.BROWSE, modelMap, vreq);
//        Instant t2 = Instant.now();
//        log.info("ANALYSER: renderShortView at (" + t2 + ") for (" + anIndividual.getURI() + ") took "
//                + ChronoUnit.MILLIS.between(t1, t2) / 1000.0 + " seconds");
//        log.info("toString " + anIndividual);
    }
}
