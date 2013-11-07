/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.reasoner.ReasonerPlugin;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasonerTBoxListener;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

public class SimpleReasonerSetup implements ServletContextListener {

    private static final Log log = LogFactory.getLog(SimpleReasonerSetup.class.getName());
    
    public static final String FILE_OF_PLUGINS = "/WEB-INF/resources/reasoner_plugins.txt";
    
    // Models used during a full recompute of the ABox
    public static final String JENA_INF_MODEL_REBUILD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-rebuild";
    public static final String JENA_INF_MODEL_SCRATCHPAD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-scratchpad";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	ServletContext ctx = sce.getServletContext();
        
        try {    
            // set up Pellet reasoning for the TBox    
            OntModelSelector assertionsOms = ModelAccess.on(ctx).getBaseOntModelSelector();
            OntModelSelector inferencesOms = ModelAccess.on(ctx).getInferenceOntModelSelector();
            OntModelSelector unionOms = ModelAccess.on(ctx).getUnionOntModelSelector();

			WebappDaoFactory wadf = ModelAccess.on(ctx).getWebappDaoFactory();
            
            if (!assertionsOms.getTBoxModel().getProfile().NAMESPACE().equals(OWL.NAMESPACE.getNameSpace())) {        
                log.error("Not connecting Pellet reasoner - the TBox assertions model is not an OWL model");
                return;
            }
            
            // Set various Pellet options for incremental consistency checking, etc.
            //PelletOptions.DL_SAFE_RULES = true;
            //PelletOptions.USE_COMPLETION_QUEUE = true;
            //PelletOptions.USE_TRACING = true;
            //PelletOptions.TRACK_BRANCH_EFFECTS = true;
            //PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
            //PelletOptions.USE_INCREMENTAL_DELETION = true;
             
            PelletListener pelletListener = new PelletListener(unionOms.getTBoxModel(),assertionsOms.getTBoxModel(),inferencesOms.getTBoxModel(),ReasonerConfiguration.DEFAULT);
            sce.getServletContext().setAttribute("pelletListener",pelletListener);
            sce.getServletContext().setAttribute("pelletOntModel", pelletListener.getPelletModel());
            
            if (wadf instanceof WebappDaoFactoryJena) {
                ((WebappDaoFactoryJena) wadf).setPelletListener(pelletListener);
            }
            
            log.info("Pellet reasoner connected for the TBox");
     
            waitForTBoxReasoning(sce); 
            
            // set up simple reasoning for the ABox
                                
            RDFService rdfService = RDFServiceUtils.getRDFServiceFactory(ctx).getRDFService();            
            Dataset dataset = new RDFServiceDataset(rdfService);
            
            Model rebuildModel = dataset.getNamedModel(JENA_INF_MODEL_REBUILD); 
            Model scratchModel = dataset.getNamedModel(JENA_INF_MODEL_SCRATCHPAD);
            Model inferenceModel = dataset.getNamedModel(JenaDataSourceSetupBase.JENA_INF_MODEL);

            // the simple reasoner will register itself as a listener to the ABox assertions
            SimpleReasoner simpleReasoner = new SimpleReasoner(
                    unionOms.getTBoxModel(), rdfService, inferenceModel, rebuildModel, scratchModel);
            sce.getServletContext().setAttribute(SimpleReasoner.class.getName(),simpleReasoner);
            
            StartupStatus ss = StartupStatus.getBean(ctx);
            List<ReasonerPlugin> pluginList = new ArrayList<ReasonerPlugin>();
            List<String> pluginClassnameList = this.readFileOfListeners(ctx);
            for (String classname : pluginClassnameList) {
                try {
                    ReasonerPlugin plugin = (ReasonerPlugin) Class.forName(
                            classname).getConstructors()[0].newInstance();
                    plugin.setSimpleReasoner(simpleReasoner);
                    pluginList.add(plugin);
                } catch(Throwable t) {              
                    ss.info(this, "Could not instantiate reasoner plugin " + classname);
                }
            }
            simpleReasoner.setPluginList(pluginList);
            
            SimpleReasonerTBoxListener simpleReasonerTBoxListener = new SimpleReasonerTBoxListener(simpleReasoner);
            sce.getServletContext().setAttribute(SimpleReasonerTBoxListener.class.getName(),simpleReasonerTBoxListener);
            assertionsOms.getTBoxModel().register(simpleReasonerTBoxListener);
            inferencesOms.getTBoxModel().register(simpleReasonerTBoxListener);
            
            RecomputeMode mode = getRecomputeRequired(ctx);
            if (RecomputeMode.FOREGROUND.equals(mode)) {
                log.info("ABox inference recompute required.");
                simpleReasoner.recompute();
            } else if (RecomputeMode.BACKGROUND.equals(mode)) {
                log.info("starting ABox inference recompute in a separate thread.");
                new VitroBackgroundThread(
                        new ABoxRecomputer(
                                simpleReasoner),"ABoxRecomputer").start();
            }    
            
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    public static void waitForTBoxReasoning(ServletContextEvent sce) 
        throws InterruptedException {
        PelletListener pelletListener = (PelletListener) sce.getServletContext().getAttribute("pelletListener");
        if (pelletListener == null) {
            return ;
        }
        int sleeps = 0;
        // sleep at least once to make sure the TBox reasoning gets started
        while ((0 == sleeps) || ((sleeps < 1000) && pelletListener.isReasoning())) {
            if (((sleeps - 1) % 10) == 0) { // print message at 10 second intervals
                log.info("Waiting for initial TBox reasoning to complete");
            }
            Thread.sleep(1000);   
            sleeps++;
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("received contextDestroyed notification");
   
        SimpleReasoner simpleReasoner = getSimpleReasonerFromServletContext(sce.getServletContext());   
        if (simpleReasoner != null) {
            log.info("sending stop request to SimpleReasoner");
            simpleReasoner.setStopRequested();
        } 
    
        SimpleReasonerTBoxListener simpleReasonerTBoxListener = getSimpleReasonerTBoxListenerFromContext(sce.getServletContext());   
        if (simpleReasonerTBoxListener != null) {
            log.info("sending stop request to simpleReasonerTBoxListener");
            simpleReasonerTBoxListener.setStopRequested();
        } 
        
    }
    
    public static SimpleReasoner getSimpleReasonerFromServletContext(ServletContext ctx) {
        Object simpleReasoner = ctx.getAttribute(SimpleReasoner.class.getName());
        
        if (simpleReasoner instanceof SimpleReasoner) {
            return (SimpleReasoner) simpleReasoner;
        } else {
            return null;
        }
    }
    
    public static SimpleReasonerTBoxListener getSimpleReasonerTBoxListenerFromContext(ServletContext ctx) {
        Object simpleReasonerTBoxListener = ctx.getAttribute(SimpleReasonerTBoxListener.class.getName());
        
        if (simpleReasonerTBoxListener instanceof SimpleReasonerTBoxListener) {
            return (SimpleReasonerTBoxListener) simpleReasonerTBoxListener;
        } else {
            return null;
        }
    }
    
    public enum RecomputeMode {
        FOREGROUND, BACKGROUND
    }
    
    private static final String RECOMPUTE_REQUIRED_ATTR = 
            SimpleReasonerSetup.class.getName() + ".recomputeRequired";
    
    public static void setRecomputeRequired(ServletContext ctx, RecomputeMode mode) {
        ctx.setAttribute(RECOMPUTE_REQUIRED_ATTR, mode);
    }
    
    public static RecomputeMode getRecomputeRequired(ServletContext ctx) {
        return (RecomputeMode) ctx.getAttribute(RECOMPUTE_REQUIRED_ATTR);
    }
  
    private static final String MSTCOMPUTE_REQUIRED_ATTR = 
        SimpleReasonerSetup.class.getName() + ".MSTComputeRequired";

    public static void setMSTComputeRequired(ServletContext ctx) {
        ctx.setAttribute(MSTCOMPUTE_REQUIRED_ATTR, true);
    }
    
    private static boolean isMSTComputeRequired(ServletContext ctx) {
        return (ctx.getAttribute(MSTCOMPUTE_REQUIRED_ATTR) != null);
    }
        
    /**
     * Read the names of the plugin classes classes.
     * 
     * If there is a problem, set a fatal error, and return an empty list.
     */
    private List<String> readFileOfListeners(ServletContext ctx) {
        List<String> list = new ArrayList<String>();

        StartupStatus ss = StartupStatus.getBean(ctx);
        
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = ctx.getResourceAsStream(FILE_OF_PLUGINS);
            br = new BufferedReader(new InputStreamReader(is));

            String line;
            while (null != (line = br.readLine())) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    list.add(trimmed);
                }
            }
        } catch (NullPointerException e) {
            // ignore the lack of file
        } catch (IOException e) {
            ss.fatal(this,
                    "Failed while processing the list of startup listeners:  "
                            + FILE_OF_PLUGINS, e);
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
        
        log.debug("Classnames of reasoner plugins = " + list);
        return list;
    }
    
    private class ABoxRecomputer implements Runnable {

        private SimpleReasoner simpleReasoner;

        public ABoxRecomputer(SimpleReasoner simpleReasoner) {
            this.simpleReasoner = simpleReasoner;
        }

        public void run() {
            simpleReasoner.recompute();
        }
    }
}
