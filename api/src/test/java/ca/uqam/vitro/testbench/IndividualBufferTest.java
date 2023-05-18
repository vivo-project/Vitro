/**
 * 
 */
package ca.uqam.vitro.testbench;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
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
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDBBuffered;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;
import edu.cornell.mannlib.vitro.webapp.services.shortview.FakeApplicationOntologyService.FakeVivoPeopleDataGetter;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.startup.StartupManager;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModelBuilder;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

/**
 * @author ubuntu
 *
 */
public class IndividualBufferTest {
    private static final Log log = LogFactory.getLog(IndividualBufferTest.class);

    private static ServletContextStub ctx;
    private static ServletContextEvent sce;
    private static VitroRequest vreq;
    private static HttpSessionStub session;
    private static HttpServletRequestStub hreq;
    private static StartupManager sm;

    private static StartupStatus ss;

    /**
     * @throws java.lang.Exception
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

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.manchester.cs.jfact.kernel.Individual#Individual(org.semanticweb.owlapi.model.IRI)}.
     */
    @Test
    public void testIndividual() {
    String iriString = "http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca";
    WebappDaoFactory daoFact = vreq.getBufferedIndividualWebappDaoFactory();
    IndividualDao iDao = daoFact.getIndividualDao();
    Individual individual = iDao.getIndividualByURI(iriString);
    OntModel _buffOnt = null;
    if (individual instanceof IndividualSDBBuffered) {
        ((IndividualSDBBuffered) individual).populateIndividualBufferModel(iriString);
        _buffOnt=((IndividualSDBBuffered) individual).getBuffOntModel();
        
    }
    WebappDaoFactoryJena jenaDao = new WebappDaoFactoryJena(_buffOnt);
    ShortViewService svs = ShortViewServiceSetup.getService(ctx);
//  LogManager.getRootLogger().setLevel(Level.DEBUG);
//  Instant t1 = Instant.now();
    Map<String, Object> modelMap = new HashMap<String, Object>();
    modelMap.put("individual", IndividualTemplateModelBuilder.build((Individual) individual, vreq));
    modelMap.put("vclass", "Professeur");
    String rsv = svs.renderShortView((Individual) individual, ShortViewContext.BROWSE, modelMap, vreq);
    log.debug(rsv);
    }

}
