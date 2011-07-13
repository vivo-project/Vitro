/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

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
import edu.cornell.mannlib.vitro.webapp.search.IndexConstants;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearchImpl;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.indexing.AdditionalURIsForContextNodes;
import edu.cornell.mannlib.vitro.webapp.search.indexing.AdditionalURIsToIndex;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
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
            server = new CommonsHttpSolrServer(new URL( solrServerUrl ),null,new XMLResponseParser(),false); 
            //server = new CommonsHttpSolrServer(new URL( solrServerUrl ));
            server.setSoTimeout(10000);  // socket read timeout
            server.setConnectionTimeout(10000);
            server.setDefaultMaxConnectionsPerHost(100);
            server.setMaxTotalConnections(100);         
            server.setMaxRetries(1);            
            context.setAttribute(LOCAL_SOLR_SERVER, server);
            
            /* set up the individual to solr doc translation */            
            OntModel displayOntModel = (OntModel) sce.getServletContext().getAttribute("displayOntModel");
            
            OntModel abox = ModelContext.getBaseOntModelSelector(context).getABoxModel();            
            OntModel inferences = (OntModel)context.getAttribute( JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME);
            Dataset dataset = DatasetFactory.create(ModelContext.getJenaOntModel(context));

            OntModel jenaOntModel = ModelContext.getJenaOntModel(context);
            
            List<DocumentModifier> modifiers = new ArrayList<DocumentModifier>();
            modifiers.add(new CalculateParameters(dataset));
            modifiers.add(new ContextNodeFields(jenaOntModel));
            modifiers.add(new NameBoost());
            
            IndividualToSolrDocument indToSolrDoc = new IndividualToSolrDocument(
            		new ProhibitedFromSearch(DisplayVocabulary.PRIMARY_SEARCH_INDEX_URI, displayOntModel),
            		new IndividualProhibitedFromSearchImpl(context), 
            		modifiers);                        
            
            /* setup solr indexer */
            SolrIndexer solrIndexer = new SolrIndexer(server, indToSolrDoc);            
           /* if( solrIndexer.isIndexEmpty() ){
                log.info("solr index is empty, requesting rebuild");
                sce.getServletContext().setAttribute(IndexConstants.INDEX_REBUILD_REQUESTED_AT_STARTUP, Boolean.TRUE);         
            } */           
            
            // This is where the builder gets the list of places to try to
            // get objects to index. It is filtered so that non-public text
            // does not get into the search index.
            WebappDaoFactory wadf = (WebappDaoFactory) context.getAttribute("webappDaoFactory");
            VitroFilters vf = VitroFilterUtils.getPublicFilter(context);
            wadf = new WebappDaoFactoryFiltering(wadf, vf);            
            
            //make objects that will find additional URIs for context nodes etc
            List<AdditionalURIsToIndex> uriFinders = new ArrayList<AdditionalURIsToIndex>();
            uriFinders.add( new AdditionalURIsForContextNodes(jenaOntModel) );
            
            IndexBuilder builder = new IndexBuilder(context, solrIndexer, wadf, uriFinders);
            // to the servlet context so we can access it later in the webapp.
            context.setAttribute(IndexBuilder.class.getName(), builder);
            
            // set up listeners so search index builder is notified of changes to model
            ServletContext ctx = sce.getServletContext();
            SearchReindexingListener srl = new SearchReindexingListener(builder);
            ModelContext.registerListenerForChanges(ctx, srl);
                                    
           /* if( sce.getServletContext().getAttribute(IndexConstants.INDEX_REBUILD_REQUESTED_AT_STARTUP) instanceof Boolean &&
                (Boolean)sce.getServletContext().getAttribute(IndexConstants.INDEX_REBUILD_REQUESTED_AT_STARTUP) ){
                log.info("Rebuild of solr index required before startup.");
                builder.doIndexRebuild();                                               
                int n = 0;
                while( builder.isReindexRequested() || builder.isIndexing() ){
                    n++;
                    if( n % 20 == 0 ) //output message every 10 sec. 
                        log.info("Still rebuilding solr index");
                    Thread.sleep(500);
                }               
            }*/
            
            log.info("Setup of Solr index completed.");   
        } catch (Throwable e) {
            log.error("could not setup local solr server",e);
        }
       
    }

    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {       
        IndexBuilder builder = (IndexBuilder)sce.getServletContext().getAttribute(IndexBuilder.class.getName());
        if( builder != null )
            builder.stopIndexingThread();
        
    }
    
    public static SolrServer getSolrServer(ServletContext ctx){
        return (SolrServer) ctx.getAttribute(LOCAL_SOLR_SERVER);
    }
    
}
