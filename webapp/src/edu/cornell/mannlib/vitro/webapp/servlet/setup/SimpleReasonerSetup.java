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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.PelletOptions;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.reasoner.ReasonerPlugin;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasonerTBoxListener;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.TripleStoreType;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class SimpleReasonerSetup implements ServletContextListener {

    private static final Log log = LogFactory.getLog(SimpleReasonerSetup.class.getName());
    
    public static final String FILE_OF_PLUGINS = "/WEB-INF/resources/reasoner_plugins.txt";
    
    // Models used during a full recompute of the ABox
    public static final String JENA_INF_MODEL_REBUILD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-rebuild";
    public static final String JENA_INF_MODEL_SCRATCHPAD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-scratchpad";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {    
            // set up Pellet reasoning for the TBox    
            
            OntModelSelector assertionsOms = ModelContext.getBaseOntModelSelector(sce.getServletContext());
            OntModelSelector inferencesOms = ModelContext.getInferenceOntModelSelector(sce.getServletContext());
            OntModelSelector unionOms = ModelContext.getUnionOntModelSelector(sce.getServletContext());

            WebappDaoFactoryJena wadf = (WebappDaoFactoryJena) sce.getServletContext().getAttribute("webappDaoFactory");
            
            if (!assertionsOms.getTBoxModel().getProfile().NAMESPACE().equals(OWL.NAMESPACE.getNameSpace())) {        
                log.error("Not connecting Pellet reasoner - the TBox assertions model is not an OWL model");
                return;
            }
            
            // Set various Pellet options for incremental consistency checking, etc.
            PelletOptions.DL_SAFE_RULES = true;
            PelletOptions.USE_COMPLETION_QUEUE = true;
            PelletOptions.USE_TRACING = true;
            PelletOptions.TRACK_BRANCH_EFFECTS = true;
            PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
            PelletOptions.USE_INCREMENTAL_DELETION = true;
             
            PelletListener pelletListener = new PelletListener(unionOms.getTBoxModel(),assertionsOms.getTBoxModel(),inferencesOms.getTBoxModel(),ReasonerConfiguration.DEFAULT);
            sce.getServletContext().setAttribute("pelletListener",pelletListener);
            sce.getServletContext().setAttribute("pelletOntModel", pelletListener.getPelletModel());
            
            if (wadf != null) {
                wadf.setPelletListener(pelletListener);
            }
            
            log.info("Pellet reasoner connected for the TBox");
     
           // set up simple reasoning for the ABox
                    
            ServletContext ctx = sce.getServletContext();
            BasicDataSource bds = JenaDataSourceSetupBase
                                    .getApplicationDataSource(ctx);
            String dbType = ConfigurationProperties.getBean(ctx).getProperty( // database type
                    "VitroConnection.DataSource.dbtype","MySQL");
            
                        
            Model rebuildModel = JenaDataSourceSetupBase.makeDBModel(
                    bds, 
                    JENA_INF_MODEL_REBUILD, 
                    JenaDataSourceSetupBase.DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, 
                    dbType, ctx);            
            Model scratchModel = JenaDataSourceSetupBase.makeDBModel(
                    bds, 
                    JENA_INF_MODEL_SCRATCHPAD, 
                    JenaDataSourceSetupBase.DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, 
                    dbType, ctx); 
            
            
            // the simple reasoner will register itself as a listener to the ABox assertions
            SimpleReasoner simpleReasoner = new SimpleReasoner(unionOms.getTBoxModel(), assertionsOms.getABoxModel(), inferencesOms.getABoxModel(), rebuildModel, scratchModel);
            sce.getServletContext().setAttribute(SimpleReasoner.class.getName(),simpleReasoner);
            
            StartupStatus ss = StartupStatus.getBean(ctx);
            List<ReasonerPlugin> pluginList = new ArrayList<ReasonerPlugin>();
            List<String> pluginClassnameList = this.readFileOfListeners(ctx);
            for (String classname : pluginClassnameList) {
                try {
                    ReasonerPlugin plugin = (ReasonerPlugin) Class.forName(
                            classname).getConstructors()[0].newInstance();
                    pluginList.add(plugin);
                } catch(Throwable t) {              
                    ss.info(this, "Could not instantiate reasoner plugin " + classname);
                }
            }
            simpleReasoner.setPluginList(pluginList);
            
            
            if (isRecomputeRequired(sce.getServletContext())) {   
                log.info("ABox inference recompute required.");
                waitForTBoxReasoning(pelletListener);  
                if (JenaDataSourceSetupBase.isFirstStartup()) {
                    simpleReasoner.recompute();
                } else {
                    log.info("starting ABox inference recompute in a separate thread.");
                    new Thread(new ABoxRecomputer(simpleReasoner),"ABoxRecomputer").start();
                }
                
            } else if ( isMSTComputeRequired(sce.getServletContext()) ) {
                log.info("mostSpecificType computation required. It will be done in a separate thread.");
                waitForTBoxReasoning(pelletListener);
                new Thread(new MostSpecificTypeRecomputer(simpleReasoner),"MostSpecificTypeComputer").start();
            }

            SimpleReasonerTBoxListener simpleReasonerTBoxListener = new SimpleReasonerTBoxListener(simpleReasoner);
            sce.getServletContext().setAttribute(SimpleReasonerTBoxListener.class.getName(),simpleReasonerTBoxListener);
            assertionsOms.getTBoxModel().register(simpleReasonerTBoxListener);
            inferencesOms.getTBoxModel().register(simpleReasonerTBoxListener);
            
            log.info("Simple reasoner connected for the ABox");
            
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    private void waitForTBoxReasoning(PelletListener pelletListener) 
        throws InterruptedException {
      int sleeps = 0;
      while (sleeps < 1000 && pelletListener.isReasoning()) {
          if ((sleeps % 10) == 0) { // print message at 10 second intervals
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
    
    private static final String RECOMPUTE_REQUIRED_ATTR = 
            SimpleReasonerSetup.class.getName() + ".recomputeRequired";
    
    public static void setRecomputeRequired(ServletContext ctx) {
        ctx.setAttribute(RECOMPUTE_REQUIRED_ATTR, true);
    }
    
    private static boolean isRecomputeRequired(ServletContext ctx) {
        return (ctx.getAttribute(RECOMPUTE_REQUIRED_ATTR) != null);
    }
  
    private static final String MSTCOMPUTE_REQUIRED_ATTR = 
        SimpleReasonerSetup.class.getName() + ".MSTComputeRequired";

    public static void setMSTComputeRequired(ServletContext ctx) {
        ctx.setAttribute(MSTCOMPUTE_REQUIRED_ATTR, true);
    }
    
    private static boolean isMSTComputeRequired(ServletContext ctx) {
        return (ctx.getAttribute(MSTCOMPUTE_REQUIRED_ATTR) != null);
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
    
    private class MostSpecificTypeRecomputer implements Runnable {
        
        private SimpleReasoner simpleReasoner;
        
        public MostSpecificTypeRecomputer(SimpleReasoner simpleReasoner) {
            this.simpleReasoner = simpleReasoner;
        }
        
        public void run() {
            simpleReasoner.computeMostSpecificType();              
        }
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
}
