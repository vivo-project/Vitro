package ca.uqam.vitro.testbench;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationSetup;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.IndividualFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDBBuffered;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupManager;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.triplesource.impl.BasicCombinedTripleSource;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModelBuilder;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

public class IndividualSDBBufferTest extends AbstractTestClass implements ServletContextListener {
//    public static void main(String[] args) {
//        IndividualSDBBufferTest startupManagerWithNeptuneTest = new IndividualSDBBufferTest();
//        try {
//            startupManagerWithNeptuneTest.run();
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (JsonProcessingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }
    private ServletContextEvent sce;

    private VitroRequest vreq;
    @Before
    public void setupContext()  throws InterruptedException, JsonProcessingException {
        System.setProperty("vitro.home",(new File("src/test/testbench")).getAbsolutePath()+"/vivo/home");
        ctx = new ServletContextStub();
        sce = new ServletContextEvent(ctx);
        ss = StartupStatus.getBean(ctx);
        initLisener("edu.cornell.mannlib.vitro.webapp.application.ApplicationSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl$ComponentsSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.config.RevisionInfoSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.i18n.selection.LocaleSelectionSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.servlet.setup.ConfigurationModelsSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.servlet.setup.ContentModelSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.web.images.PlaceholderUtil$Setup");
        initLisener("edu.cornell.mannlib.vitro.webapp.servlet.setup.FileGraphSetup");
//        initLisener("edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl$ReasonersSetup");
//        initLisener("edu.cornell.mannlib.vitro.webapp.servlet.setup.SimpleReasonerSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.servlet.setup.ThemeInfoSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionRegistry$Setup");
//        initLisener("edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsSmokeTest");
        initLisener("edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionBean$Setup");
        initLisener("edu.cornell.mannlib.vitro.webapp.auth.policy.setup.CommonPolicyFamilySetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.auth.policy.RootUserPolicy$Setup");
        initLisener("edu.cornell.mannlib.vitro.webapp.auth.policy.RestrictHomeMenuItemEditingPolicy$Setup");
        initLisener("edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewServiceSetup");
//        initLisener("edu.ucsf.vitro.opensocial.OpenSocialSmokeTests");
        initLisener("edu.cornell.mannlib.vitro.webapp.i18n.I18nContextListener");
//        initLisener("edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerSetup");
        initLisener("edu.cornell.mannlib.vitro.webapp.freemarker.config.FreemarkerConfiguration$Setup");
        initLisener("org.apache.commons.fileupload.servlet.FileCleanerCleanup");
//        initLisener("edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache$Setup");
//        initLisener("edu.cornell.mannlib.vitro.webapp.servlet.setup.SearchEngineSmokeTest");
        dumpCtxAttrib(ctx);
        //        try {
        //            findAndInstantiateListeners();
        //
        //            for (ServletContextListener listener : initializeList) {
        //                if (ss.isStartupAborted()) {
        //                    ss.listenerNotExecuted(listener);
        //                } else {
        //                    initialize(listener, sce);
        //                }
        //            }
        //            log.info("Called 'contextInitialized' on all listeners.");
        //        } catch (Exception e) {
        //            ss.fatal(this, "Startup threw an unexpected exception.", e);
        //            log.error("Startup threw an unexpected exception.", e);
        //        } catch (Throwable t) {
        //            log.fatal("Startup threw an unexpected error.", t);
        //            throw t;
        //        } 
        //        Application app = ApplicationUtils.instance();
        //        ComponentStartupStatus css = new ComponentStartupStatusImpl(this,ss);
        //        configureJena();
        //        ContentTripleSource contentSource = app.getContentTripleSource();
        //        contentSource.startup(app, css);
        //        RDFServiceUtils.setRDFServiceFactory(ctx,
        //                contentSource.getRDFServiceFactory());
        //        RDFService srv = contentSource.getRDFService();
        //        try {
        //            List<String> rep = srv.getGraphURIs();
        //        } catch (RDFServiceException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        System.out.println("Done");
        //        WebappDaoFactoryJena wadf = new WebappDaoFactoryJena(new SimpleOntModelSelector());

        HttpSessionStub session = new HttpSessionStub();
        session = new HttpSessionStub();
        session.setServletContext(ctx);
        HttpServletRequestStub hreq = new HttpServletRequestStub();
        hreq.setSession(session);

        vreq = new VitroRequest(hreq);
        dumpReqParam(vreq);
    }
    private void evaluate(String individualUri) {
        IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
        Individual individual = iDao.getIndividualByURI(individualUri);
//        WebappDaoFactory fac = vreq.getWebappDaoFactory();
//        TimeUnit.SECONDS.sleep(10);
//        renderShortView("http://localhost:8080/vivo_i18n/individual/n6870", "Professeur");
//        String individualUri = "http://localhost:8080/vivo_i18n/individual/n6870";
        
//        String individualUri = "http://vivo-demo.uqam.ca/individual/01b1f948-0f03-3058-952b-97f188540b8d";
        if (individual instanceof IndividualFiltering) {
            try {
              IndividualSDBBuffered _indv = (IndividualSDBBuffered) ((IndividualFiltering)individual).get_innerIndividual();
              Instant t1 = Instant.now();
              Model model = _indv.populateIndividualBufferModel(individualUri);
              Instant t2 = Instant.now();
//              RDFDataMgr.write(System.out, model, Lang.TURTLE);
             log.info("ANALYSER: The treatment at (" + t2 +") for (" + individual.getURI()+") took "+ ChronoUnit.MILLIS.between(t1, t2)/1000.0 + " seconds");
             
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Map<String, Object> modelMap = new HashMap<String, Object>();
        modelMap.put("individual", IndividualTemplateModelBuilder.build(individual, vreq));
        modelMap.put("vclass", "Professeur");
        ShortViewService svs = ShortViewServiceSetup.getService(ctx);
//        LogManager.getRootLogger().setLevel(Level.DEBUG);
        Instant t1 = Instant.now();
        String rsv = svs.renderShortView(individual, ShortViewContext.BROWSE, modelMap, vreq);
        Instant t2 = Instant.now();
        log.info("ANALYSER: renderShortView at (" + t2 +") for (" + individual.getURI()+") took "+ ChronoUnit.MILLIS.between(t1, t2)/1000.0 + " seconds");
//        LogManager.getRootLogger().setLevel(Level.INFO);
        log.info("toString "+ individual);

//        ShortViewService svs = ShortViewServiceSetup.getService(ctx);
//        
//        log.info("IndividualDao");
//        IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
//        log.info("Individual");
//        log.info("===========================================================================");
//        Instant t1 = Instant.now();
//        LogManager.getRootLogger().setLevel(Level.DEBUG);
//        Individual individual = iDao.getIndividualByURI("http://localhost:8080/vivo_i18n/individual/n6870");
//        Instant t2 = Instant.now();
//        long totalTime = ChronoUnit.MILLIS.between(t1, t2);
//        log.info("ANALYSER: The treatment at (" + t2 +") for (" + individual.getURI()+") took "+ totalTime/1000.0 + " seconds");
//        log.info("===========================================================================");
//
//        
//        log.info("=======================>" +individual.getURI());
//        log.info("=======================>" +individual.toString());
//        log.info("=======================>" +individual.toJSON());
//        dumpCtxAttrib(ctx);
        log.info("DONE");
        System.exit(0);

    }

    @Test
    public void testDemoProf() {
        String individualUri = "http://vivo-demo.uqam.ca/individual/agbobli_christian_uqam_ca";
        evaluate(individualUri);
    }
    private String renderShortView(String individualUri, String vclassName) {
        Instant t1 = Instant.now();

        IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
        Instant t2 = Instant.now();
        Individual individual = iDao.getIndividualByURI(individualUri);
        Instant t3 = Instant.now();

        Map<String, Object> modelMap = new HashMap<String, Object>();
        modelMap.put("individual",
                IndividualTemplateModelBuilder.build(individual, vreq));
        modelMap.put("vclass", vclassName);
        Instant t4 = Instant.now();

        ShortViewService svs = ShortViewServiceSetup.getService(ctx);
        
        Instant t5 = Instant.now();
        String rsv = svs.renderShortView(individual, ShortViewContext.BROWSE,
                modelMap, vreq);
        Instant t6 = Instant.now();
        log.info("ANALYSER: "+
        " total-renderShortView="+ChronoUnit.MILLIS.between(t5,t6)+ 
        " total-iDao="+ChronoUnit.MILLIS.between(t1,t2)+ 
        " total-modelMap="+ChronoUnit.MILLIS.between(t3,t4)+ 
        " total-ShortViewService="+ChronoUnit.MILLIS.between(t4,t5)+ 
        " total="+ChronoUnit.MILLIS.between(t1,t6) 
        );
        return rsv;
    }
    public static void dumpReqParam(VitroRequest vreq) {
        Enumeration<String> paramNames = vreq.getParameterNames();
        for (Iterator iterator =  paramNames.asIterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            log.info("ctx name= "+ name + ", value= "+vreq.getParameter(name));
        }
        
    }
    private void initLisener(String _listener) {
        log.info("==============Called listeners "+_listener+"===============START");
//        this.dumpCtxAttrib(ctx);
        ServletContextListener _srv_listener = instantiateListener(_listener);
        initialize(_srv_listener, sce);
        log.info("==============Called listeners "+_listener+"===============END");
        
    }
    private void configureJena() {
        // we do not want to fetch imports when we wrap Models in OntModels
        OntDocumentManager.getInstance().setProcessImports(false);
    }

    private void prepareCombinedTripleSource(Application app,
            ServletContext ctx) {
        ContentTripleSource contentSource = app.getContentTripleSource();
        ConfigurationTripleSource configurationSource = app
                .getConfigurationTripleSource();
        BasicCombinedTripleSource source = new BasicCombinedTripleSource(
                contentSource, configurationSource);

        RDFServiceUtils.setRDFServiceFactory(ctx,
                contentSource.getRDFServiceFactory());
        RDFServiceUtils.setRDFServiceFactory(ctx,
                configurationSource.getRDFServiceFactory(), CONFIGURATION);

        ModelAccess.setCombinedTripleSource(source);
    }
    private static final Log log = LogFactory.getLog(StartupManager.class);

    public static final String FILE_OF_STARTUP_LISTENERS = "/WEB-INF/resources/startup_listeners.txt";

    private final List<ServletContextListener> initializeList = new ArrayList<ServletContextListener>();

    /**
     * These can be instance variables without risk, since contextInitialized()
     * will only be called once per instance.
     */
    private ServletContext ctx;
    private StartupStatus ss;

    /**
     * Build a list of the listeners, and run contextInitialized() on each of
     * them, at least until we get a fatal error.
     *
     * Each step of this should handle its own exceptions, but we'll wrap the
     * whole thing in a try/catch just in case.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ctx = sce.getServletContext();
        dumpCtxAttrib(ctx);
        ss = StartupStatus.getBean(ctx);
        try {
            findAndInstantiateListeners();

            for (ServletContextListener listener : initializeList) {
                if (ss.isStartupAborted()) {
                    ss.listenerNotExecuted(listener);
                } else {
                    initialize(listener, sce);
                }
            }
            log.info("Called 'contextInitialized' on all listeners.");
        } catch (Exception e) {
            ss.fatal(this, "Startup threw an unexpected exception.", e);
            log.error("Startup threw an unexpected exception.", e);
        } catch (Throwable t) {
            log.fatal("Startup threw an unexpected error.", t);
            throw t;
        }
    }

    public static void dumpCtxAttrib(ServletContext ctx2) {
        Enumeration<String> attribNames = ctx2.getAttributeNames();
        for (Iterator iterator =  attribNames.asIterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
//            log.info("ctx name= "+ name + ", value= "+ctx2.getAttribute(name));
        }
        
    }
    /**
     * Read the file and instantiate build a list of listener instances.
     *
     * If there is a problem, it will occur and be handled in a sub-method.
     */
    private void findAndInstantiateListeners() {
        List<String> classNames = readFileOfListeners();

        for (String className : classNames) {
            ServletContextListener listener = instantiateListener(className);
            if (listener != null) {
                initializeList.add(listener);
            }
        }

        checkForDuplicateListeners();
    }

    /**
     * Read the names of the listener classes.
     *
     * If there is a problem, set a fatal error, and return an empty list.
     */
    private List<String> readFileOfListeners() {
        List<String> list = new ArrayList<String>();

        InputStream is = null;
        BufferedReader br = null;
        try {
            //            is = ctx.getResourceAsStream(FILE_OF_STARTUP_LISTENERS);
            is = this.getClass().getResourceAsStream(FILE_OF_STARTUP_LISTENERS);
            br = new BufferedReader(new InputStreamReader(is));

            String line;
            while (null != (line = br.readLine())) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    list.add(trimmed);
                }
            }
        } catch (NullPointerException e) {
            ss.fatal(this, "Unable to locate the list of startup listeners: "
                    + FILE_OF_STARTUP_LISTENERS);
        } catch (IOException e) {
            ss.fatal(this,
                    "Failed while processing the list of startup listeners:  "
                            + FILE_OF_STARTUP_LISTENERS, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        log.debug("Classnames of listeners = " + list);
        return list;
    }

    /**
     * Instantiate a context listener from this class name.
     *
     * If there is a problem, set a fatal error, and return null.
     */
    private ServletContextListener instantiateListener(String className) {
        try {
            Class<?> c = Class.forName(className);
            Object o = c.newInstance();
            return (ServletContextListener) o;
        } catch (ClassCastException e) {
            ss.fatal(this, "Instance of '" + className
                    + "' is not a ServletContextListener", e);
            return null;
        } catch (Exception | ExceptionInInitializerError e) {
            ss.fatal(this, "Failed to instantiate listener: '" + className
                    + "'", e);
            return null;
        }
    }

    /**
     * Call contextInitialized() on the listener.
     *
     * If there is an unexpected exception, set a fatal error.
     */
    private void initialize(ServletContextListener listener,
            ServletContextEvent sce) {
        try {
            log.debug("Initializing '" + listener.getClass().getName() + "'");
            listener.contextInitialized(sce);
            ss.listenerExecuted(listener);
        } catch (Exception e) {
            ss.fatal(listener, "Threw unexpected exception", e);
        } catch (Throwable t) {
            log.fatal(listener + " Threw unexpected error", t);
            throw t;
        }
    }

    /**
     * If we have more than one listener from the same class, set a fatal error.
     */
    private void checkForDuplicateListeners() {
        for (int i = 0; i < initializeList.size(); i++) {
            for (int j = i + 1; j < initializeList.size(); j++) {
                ServletContextListener iListener = initializeList.get(i);
                ServletContextListener jListener = initializeList.get(j);
                if (iListener.getClass().equals(jListener.getClass())) {
                    ss.fatal(this,
                            ("File contains duplicate listener classes: '"
                                    + iListener.getClass().getName() + "'"));
                }
            }
        }
    }

    /**
     * Notify the listeners that the context is being destroyed, in the reverse
     * order from how they were notified at initialization.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        List<ServletContextListener> destroyList = new ArrayList<ServletContextListener>(
                initializeList);
        Collections.reverse(destroyList);

        for (ServletContextListener listener : destroyList) {
            try {
                log.debug("Destroying '" + listener.getClass().getName() + "'");
                listener.contextDestroyed(sce);
            } catch (Exception e) {
                log.error("Unexpected exception from contextDestroyed() on '"
                        + listener.getClass().getName() + "'", e);
            } catch (Throwable t) {
                log.fatal("Unexpected error from contextDestroyed() on '"
                        + listener.getClass().getName() + "'", t);
                throw t;
            }
        }
        log.info("Called 'contextDestroyed' on all listeners.");
    }

}
