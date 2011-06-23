/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SearchReindexingListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearchImpl;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

public class SolrSetup implements javax.servlet.ServletContextListener{   
    private static final Log log = LogFactory.getLog(SolrSetup.class.getName());
    
    protected static final String LOCAL_SOLR_SERVER  = "vitro.local.solr.server";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {        
        if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
                              
        try {        
            ServletContext context = sce.getServletContext();
            
            /* setup the http connection with the solr server */
            String solrServerUrl = ConfigurationProperties.getBean(sce).getProperty("vitro.local.solr.url");
            if( solrServerUrl == null ){
                log.error("Could not find vitro.local.solr.url in deploy.properties.  "+
                        "Vitro application needs a URL of a solr server that it can use to index its data. " +
                        "It should be something like http://localhost:${port}" + context.getContextPath() + "solr" 
                        );
                return;
            }            
            CommonsHttpSolrServer server;
            server = new CommonsHttpSolrServer( solrServerUrl );
            server.setSoTimeout(10000);  // socket read timeout
            server.setConnectionTimeout(10000);
            server.setDefaultMaxConnectionsPerHost(100);
            server.setMaxTotalConnections(100);         
            server.setMaxRetries(1);            
            context.setAttribute(LOCAL_SOLR_SERVER, server);
                        
            /* setup the individual to solr doc translation */            
            //first we need a ent2luceneDoc translator
            OntModel displayOntModel = (OntModel) sce.getServletContext().getAttribute("displayOntModel");
            
            OntModel abox = ModelContext.getBaseOntModelSelector(context).getABoxModel();
            
            OntModel inferences = (OntModel)context.getAttribute( JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME);
            Dataset dataset = WebappDaoFactoryJena.makeInMemoryDataset(abox, inferences);
            
            List<DocumentModifier> modifiers = new ArrayList<DocumentModifier>();
           // modifiers.add(new CalculateParameters(ModelContext.getJenaOntModel(context)));
            modifiers.add(new CalculateParameters(dataset));
            modifiers.add(new ContextNodeFields(dataset));
            
            IndividualToSolrDocument indToSolrDoc = new IndividualToSolrDocument(
            		new ProhibitedFromSearch(DisplayVocabulary.PRIMARY_LUCENE_INDEX_URI, displayOntModel),
            		new IndividualProhibitedFromSearchImpl(context), 
            		modifiers);                        
            
            /* setup solr indexer */
            SolrIndexer solrIndexer = new SolrIndexer(server, indToSolrDoc);            
            if( solrIndexer.isIndexEmpty() ){
                log.info("solr index is empty, requesting rebuild");
                sce.getServletContext().setAttribute(LuceneSetup.INDEX_REBUILD_REQUESTED_AT_STARTUP, Boolean.TRUE);         
            }            
            
            // This is where the builder gets the list of places to try to
            // get objects to index. It is filtered so that non-public text
            // does not get into the search index.
            WebappDaoFactory wadf = (WebappDaoFactory) context.getAttribute("webappDaoFactory");
            VitroFilters vf = VitroFilterUtils.getPublicFilter(context);
            wadf = new WebappDaoFactoryFiltering(wadf, vf);
            List<ObjectSourceIface> sources = new ArrayList<ObjectSourceIface>();
            sources.add(wadf.getIndividualDao());
            
            IndexBuilder builder = new IndexBuilder(context, solrIndexer, sources);
            // to the servlet context so we can access it later in the webapp.
            context.setAttribute(IndexBuilder.class.getName(), builder);
            
            // set up listeners so search index builder is notified of changes to model
            ServletContext ctx = sce.getServletContext();
            SearchReindexingListener srl = new SearchReindexingListener(builder);
            ModelContext.registerListenerForChanges(ctx, srl);
                                    
            if( sce.getServletContext().getAttribute(LuceneSetup.INDEX_REBUILD_REQUESTED_AT_STARTUP) instanceof Boolean &&
                (Boolean)sce.getServletContext().getAttribute(LuceneSetup.INDEX_REBUILD_REQUESTED_AT_STARTUP) ){
                log.info("Rebuild of solr index required before startup.");
                builder.doIndexRebuild();                                               
                int n = 0;
                while( builder.isReindexRequested() || builder.isIndexing() ){
                    n++;
                    if( n % 20 == 0 ) //output message every 10 sec. 
                        log.info("Still rebuilding solr index");
                    Thread.sleep(500);
                }               
            }
            
            log.info("Setup of Solr index completed.");   
        } catch (Throwable e) {
            log.error("could not setup local solr server",e);
        }
       
    }

    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {       
        
    }
    
    public static SolrServer getSolrServer(ServletContext ctx){
        return (SolrServer) ctx.getAttribute(LOCAL_SOLR_SERVER);
    }
    
}
