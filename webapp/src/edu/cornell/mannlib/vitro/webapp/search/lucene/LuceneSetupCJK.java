package edu.cornell.mannlib.vitro.webapp.search.lucene;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.search.BooleanQuery;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

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
public class LuceneSetupCJK implements javax.servlet.ServletContextListener {
        private static String indexDir = null;
        private static final Log log = LogFactory.getLog(LuceneSetupCJK.class.getName());

        /**
         * Gets run to set up DataSource when the webapp servlet context gets created.
         */
        @SuppressWarnings({ "static-access", "unchecked" })
        public void contextInitialized(ServletContextEvent sce) {
            ServletContext context = sce.getServletContext();
            log.info("**** Running "+this.getClass().getName()+".contextInitialized()");
            try{
            indexDir = getIndexDirName();
            log.info("Lucene indexDir: " + indexDir);

            setBoolMax();
            
            HashSet dataPropertyBlacklist = new HashSet<String>();
            context.setAttribute(LuceneSetup.SEARCH_DATAPROPERTY_BLACKLIST, dataPropertyBlacklist);
            
            HashSet objectPropertyBlacklist = new HashSet<String>();
            objectPropertyBlacklist.add("http://www.w3.org/2002/07/owl#differentFrom");
            context.setAttribute(LuceneSetup.SEARCH_OBJECTPROPERTY_BLACKLIST, objectPropertyBlacklist);

            //Here we want to put the LuceneSearcher in the application scope.
            // the queries need to know the analyzer to use so that the same one can be used
            // to analyze the fields in the incoming user query terms.
            LuceneSearcher searcher = new LuceneSearcher(
                    new LuceneQueryFactory(getAnalyzer(), indexDir),
                    indexDir);
            searcher.addObj2Doc(new Entity2LuceneDoc());
            context.setAttribute(Searcher.class.getName(), searcher);

            //here we want to put the LuceneIndex object into the application scope
            LuceneIndexer indexer = new LuceneIndexer(indexDir, null, getAnalyzer());
            indexer.addSearcher(searcher);
            context.setAttribute(LuceneSetup.ANALYZER, getAnalyzer());
            context.setAttribute(LuceneSetup.INDEX_DIR, indexDir);
            indexer.addObj2Doc(new Entity2LuceneDoc());

            //This is where the builder gets the list of places to try to 
            //get objects to index. It is filtered so that non-public text
            //does not get into the search index.            
            WebappDaoFactory wadf = 
                (WebappDaoFactory) context.getAttribute("webappDaoFactory");
            VitroFilters vf = 
                VitroFilterUtils.getDisplayFilterByRoleLevel(RoleLevel.PUBLIC, wadf); 
            wadf = new WebappDaoFactoryFiltering(wadf,vf);
            
            List sources = new ArrayList();
            sources.add(wadf.getIndividualDao());

            IndexBuilder builder = new IndexBuilder(context,indexer,sources);

            // here we add the IndexBuilder with the LuceneIndexer
            // to the servlet context so we can access it later in the webapp.
            context.setAttribute(IndexBuilder.class.getName(),builder);
            }catch(Exception ex){
                log.error("Could not setup lucene full text search." , ex);
            }
            
            log.debug("**** End of "+this.getClass().getName()+".contextInitialized()");
        }

        /**
         * Gets run when the webApp Context gets destroyed.
         */
        public void contextDestroyed(ServletContextEvent sce) {
            log.info("**** Running "+this.getClass().getName()+".contextDestroyed()");
        }

        /**
         * In wild card searches the query is first broken into many boolean searches
         * OR'ed together.  So if there is a query that would match a lot of records
         * we need a high max boolean limit for the lucene search.
         *
         * This sets some static method in the lucene library to achieve this.
         */
        public static void setBoolMax() {
            BooleanQuery.setMaxClauseCount(16384);
        }

        /** directory to use when none is specified */
        private String DEFAULT_INDEX_DIR = "/usr/local/lucene/vitrodefault";

        /** name of the properties file to look for in the 'resources' */
        private String LUCENE_PROPERTIES = "/LuceneSetup.properties";

        /**
         * Gets the name of the directory to store the lucene index in.
         * This is stored in a file named LuceneSetup.properties
         * which should be on the classpath in the default package.
         * That file should have a property named 'LuceneSetup.indexDir'
         * which has the directory to store the lucene index for this
         * clone in.  If the property file is not found or the 
         * LuceneSetup.indexDir is not found, then DEFAULT_INDEX_DIR will 
         * be used.
         * @return a string that is the directory to store the lucene
         * index.
         *
         * @throws IOException
         */
        private  String getIndexDirName() {
            Properties props = new Properties();
            InputStream raw = this.getClass().getResourceAsStream( LUCENE_PROPERTIES );
            if (raw == null){
                    log.warn("LuceneSetup.getIndexDirName()" +
                            " Failed to find resource: " + LUCENE_PROPERTIES +
                            ". Using default directory " + DEFAULT_INDEX_DIR);
                    return DEFAULT_INDEX_DIR;
            }

            try{ props.load( raw ); }
            catch (Exception ex){
                log.error("LuceneSetup.getIndexDirName()" +
                        "unable to load properties: \n" + ex.getMessage() +
                        "\nUsing default directory " + DEFAULT_INDEX_DIR);
                return DEFAULT_INDEX_DIR;
            }
            finally { try{raw.close();} catch(Exception ex){} }

            String dirName = props.getProperty("LuceneSetup.indexDir");
            if( dirName == null ){
                log.error("LuceneSetup.getIndexDir:  " +
                        "indexDir not found.  Using default directory "+DEFAULT_INDEX_DIR );
                return DEFAULT_INDEX_DIR;
            }
            return dirName;
        }

    /**
     * Gets the analyzer that will be used when building the indexing
     * and when analyzing the incoming search terms.
     *
     * @return
     */
    @SuppressWarnings("static-access")
    private Analyzer getAnalyzer() {
        return new CJKAnalyzer();        
    }
    
}
