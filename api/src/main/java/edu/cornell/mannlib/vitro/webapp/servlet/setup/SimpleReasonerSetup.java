/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_INFERENCES;

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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
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
    	SearchIndexer searchIndexer = ApplicationUtils.instance().getSearchIndexer();
        
        try {    
        	OntModel tboxAssertionsModel = ModelAccess.on(ctx).getOntModel(ModelNames.TBOX_ASSERTIONS);
        	OntModel tboxInferencesModel = ModelAccess.on(ctx).getOntModel(ModelNames.TBOX_INFERENCES);
        	OntModel tboxUnionModel = ModelAccess.on(ctx).getOntModel(ModelNames.TBOX_UNION);

            // set up simple reasoning for the ABox
                                
            RDFService rdfService = ModelAccess.on(ctx).getRDFService();            
            Dataset dataset = ModelAccess.on(ctx).getDataset();
            
            Model rebuildModel = dataset.getNamedModel(JENA_INF_MODEL_REBUILD);
            if(rebuildModel.contains(null, null, (RDFNode) null)) {
                log.info("Clearing obsolete data from inference rebuild model");
                rebuildModel.removeAll();
            }
            Model scratchModel = dataset.getNamedModel(JENA_INF_MODEL_SCRATCHPAD);
            if(scratchModel.contains(null, null, (RDFNode) null)) {
                log.info("Clearing obsolete data from inference scratchpad model");
                scratchModel.removeAll();
            }
            Model inferenceModel = dataset.getNamedModel(ABOX_INFERENCES);

            // the simple reasoner will register itself as a listener to the ABox assertions
            SimpleReasoner simpleReasoner = new SimpleReasoner(
                    tboxUnionModel, rdfService, inferenceModel, rebuildModel, scratchModel, searchIndexer);
            sce.getServletContext().setAttribute(SimpleReasoner.class.getName(),simpleReasoner);
            
            StartupStatus ss = StartupStatus.getBean(ctx);
            List<ReasonerPlugin> pluginList = new ArrayList<ReasonerPlugin>();
            List<String> pluginClassnameList = this.readFileOfListeners(ctx);
            for (String classname : pluginClassnameList) {
                try {
                    ReasonerPlugin plugin = (ReasonerPlugin) Class.forName(
                            classname).getConstructors()[0].newInstance();
                    plugin.setSimpleReasoner(simpleReasoner);
                    if (!plugin.isConfigurationOnlyPlugin()) {
                        pluginList.add(plugin);
                        log.info("adding reasoner plugin " + plugin.getClass().getName());
                    }
                } catch(Throwable t) {              
                    ss.info(this, "Could not instantiate reasoner plugin " + classname);
                }
            }
            simpleReasoner.setPluginList(pluginList);
            
            SimpleReasonerTBoxListener simpleReasonerTBoxListener = new SimpleReasonerTBoxListener(simpleReasoner);
            sce.getServletContext().setAttribute(SimpleReasonerTBoxListener.class.getName(),simpleReasonerTBoxListener);
            tboxAssertionsModel.register(simpleReasonerTBoxListener);
            tboxInferencesModel.register(simpleReasonerTBoxListener);
            
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

        @Override
		public void run() {
            simpleReasoner.recompute();
        }
    }
}
