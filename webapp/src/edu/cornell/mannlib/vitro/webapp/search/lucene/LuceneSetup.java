/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import static edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames.ALLTEXT;
import static edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames.ALLTEXTUNSTEMMED;
import static edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames.NAME;
import static edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames.NAMEUNSTEMMED;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.search.BooleanQuery;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SearchReindexingListener;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

/**
 * Setup objects for lucene searching and indexing.
 *
 * The indexing and search objects, IndexBuilder and Searcher are found by the
 * controllers IndexController and SearchController through the servletContext.
 * This object will have the method contextInitialized() called when the tomcat
 * server starts this webapp.
 *
 *  The contextInitialized() will try to find the lucene index directory,
 *  make a LueceneIndexer and a LuceneSearcher.  The LuceneIndexer will
 *  also get a list of Obj2Doc objects so it can translate object to lucene docs.
 *
 * To execute this at context creation put this in web.xml:
    <listener>
        <listener-class>
            edu.cornell.mannlib.vitro.search.setup.LuceneSetup
        </listener-class>
    </listener>

 * @author bdc34
 *
 */
public class LuceneSetup implements javax.servlet.ServletContextListener {        
    private static final Log log = LogFactory.getLog(LuceneSetup.class.getName());
        
	/**
	 * Gets run to set up DataSource when the webapp servlet context gets
	 * created.
	 */
	public void contextInitialized(ServletContextEvent sce) {
	    
	    if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
	    
		try {
			ServletContext context = sce.getServletContext();

			String baseIndexDir = getBaseIndexDirName();
			log.info("Setting up Lucene index. Base directory of lucene index: " + baseIndexDir);

			setBoolMax();

			// these should really be set as annotation properties.
			HashSet<String> dataPropertyBlacklist = new HashSet<String>();
			context.setAttribute(SEARCH_DATAPROPERTY_BLACKLIST,	dataPropertyBlacklist);
			HashSet<String> objectPropertyBlacklist = new HashSet<String>();
			objectPropertyBlacklist.add("http://www.w3.org/2002/07/owl#differentFrom");
			context.setAttribute(SEARCH_OBJECTPROPERTY_BLACKLIST, objectPropertyBlacklist);

	         //This is where to get a LucenIndex from.  The indexer will
            //need to reference this to notify it of updates to the index           
			context.setAttribute(BASE_INDEX_DIR, baseIndexDir);
            LuceneIndexFactory lif = LuceneIndexFactory.setup(context, baseIndexDir);                       
            String liveIndexDir = lif.getLiveIndexDir(context);
            
			// Here we want to put the LuceneIndex object into the application scope.
			// This will attempt to create a new directory and empty index if there is none.
			LuceneIndexer indexer = new LuceneIndexer(getBaseIndexDirName(),liveIndexDir, null, getAnalyzer());
			context.setAttribute(ANALYZER, getAnalyzer());
			
			OntModel displayOntModel = (OntModel) sce.getServletContext().getAttribute("displayOntModel");
			Entity2LuceneDoc translator = new Entity2LuceneDoc( 
			        new ProhibitedFromSearch(DisplayVocabulary.PRIMARY_LUCENE_INDEX_URI, displayOntModel),
			        new IndividualProhibitedFromSearch(context) );									
			indexer.addObj2Doc(translator);			
			
			context.setAttribute(LuceneIndexer.class.getName(), indexer);
			indexer.setLuceneIndexFactory(lif);
			
			if( indexer.isIndexCorroupt() ){
			    log.info("lucene index is corrupt, requesting rebuild");
			}
			if( indexer.isIndexEmpty() ){
			    log.info("lucene index is empty, requesting rebuild");
			    sce.getServletContext().setAttribute(INDEX_REBUILD_REQUESTED_AT_STARTUP, Boolean.TRUE);			
			}
			
			// This is where the builder gets the list of places to try to
			// get objects to index. It is filtered so that non-public text
			// does not get into the search index.
			WebappDaoFactory wadf = (WebappDaoFactory) context.getAttribute("webappDaoFactory");
			VitroFilters vf = VitroFilterUtils.getDisplayFilterByRoleLevel(RoleLevel.PUBLIC, wadf);
			wadf = new WebappDaoFactoryFiltering(wadf, vf);

			List<ObjectSourceIface> sources = new ArrayList<ObjectSourceIface>();
			sources.add(wadf.getIndividualDao());

			IndexBuilder builder = new IndexBuilder(context, indexer, sources);

			// here we add the IndexBuilder with the LuceneIndexer
			// to the servlet context so we can access it later in the webapp.
			context.setAttribute(IndexBuilder.class.getName(), builder);

			// set up listeners so search index builder is notified of changes to model
			ServletContext ctx = sce.getServletContext();
			SearchReindexingListener srl = new SearchReindexingListener(builder);
			ModelContext.registerListenerForChanges(ctx, srl);
									
			if( (Boolean)sce.getServletContext().getAttribute(INDEX_REBUILD_REQUESTED_AT_STARTUP) instanceof Boolean &&
				(Boolean)sce.getServletContext().getAttribute(INDEX_REBUILD_REQUESTED_AT_STARTUP) ){
			    log.info("Rebuild of lucene index required before startup.");
				builder.doIndexRebuild();												
				int n = 0;
				while( builder.isReindexRequested() || builder.isIndexing() ){
				    n++;
					if( n % 20 == 0 ) //output message every 10 sec. 
					    log.info("Still rebuilding lucene  index");
					Thread.currentThread().sleep(500);
				}				
			}
			
			log.info("Setup of Lucene index completed.");			
		} catch (Throwable t) {
		    AbortStartup.abortStartup(sce.getServletContext());
			log.error("***** Error setting up Lucene index *****", t);
			throw new RuntimeException("Startup of vitro application was prevented by errors in the lucene configuration");
		}
	}

	/**
	 * Gets run when the webApp Context gets destroyed.
	 */
	public void contextDestroyed(ServletContextEvent sce) {
		log.debug("**** Running " + this.getClass().getName() + ".contextDestroyed()");
		IndexBuilder builder = (IndexBuilder) sce.getServletContext().getAttribute(IndexBuilder.class.getName());
		if( builder != null){		    		
		    builder.stopIndexingThread();
		}
	}

	/**
	 * In wild card searches the query is first broken into many boolean
	 * searches OR'ed together. So if there is a query that would match a lot of
	 * records we need a high max boolean limit for the lucene search.
	 * 
	 * This sets some static method in the lucene library to achieve this.
	 */
	public static void setBoolMax() {
		BooleanQuery.setMaxClauseCount(50000);
	}
       
	/**
	 * Gets the name of the directory to store the lucene index in. The
	 * {@link ConfigurationProperties} should have a property named
	 * 'LuceneSetup.indexDir' which has the directory to store the lucene index
	 * for this clone in. If the property is not found, an exception will be
	 * thrown.
	 * 
	 * @return a string that is the directory to store the lucene index.
	 * @throws IllegalStateException
	 *             if the property is not found.
	 * @throws IOException
	 *             if the directory doesn't exist and we fail to create it.
	 */
	private String getBaseIndexDirName()
			throws IOException {
		String dirName = ConfigurationProperties
				.getProperty("LuceneSetup.indexDir");
		if (dirName == null) {
			throw new IllegalStateException(
					"LuceneSetup.indexDir not found in properties file.");
		}

		File dir = new File(dirName);
		if (!dir.exists()) {
			boolean created = dir.mkdir();
			if (!created) {
				throw new IOException(
						"Unable to create Lucene index directory at '" + dir
								+ "'");
			}
		}

		return dirName;
	}

    /**
     * Gets the analyzer that will be used when building the indexing
     * and when analyzing the incoming search terms.
     *
     * @return
     */
    private Analyzer getAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper( new KeywordAnalyzer());
        analyzer.addAnalyzer(ALLTEXT, new HtmlLowerStopStemAnalyzer());
        analyzer.addAnalyzer(NAME, new HtmlLowerStopStemAnalyzer());
        analyzer.addAnalyzer(ALLTEXTUNSTEMMED, new HtmlLowerStopAnalyzer());
        analyzer.addAnalyzer(NAMEUNSTEMMED, new HtmlLowerStopAnalyzer());        
        return analyzer;
    }
        
    public static final String INDEX_REBUILD_REQUESTED_AT_STARTUP = "LuceneSetup.indexRebuildRequestedAtStarup";
    public static final String ANALYZER= "lucene.analyzer";
    public static final String BASE_INDEX_DIR = "lucene.indexDir";
    public static final String SEARCH_DATAPROPERTY_BLACKLIST = 
        "search.dataproperty.blacklist";
    public static final String SEARCH_OBJECTPROPERTY_BLACKLIST = 
        "search.objectproperty.blacklist";

}
