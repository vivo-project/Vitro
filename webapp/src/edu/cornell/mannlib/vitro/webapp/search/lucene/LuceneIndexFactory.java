package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cornell.mannlib.vitro.webapp.search.SearchException;

public class LuceneIndexFactory {
    
	IndexSearcher searcher = null;	
	private static final Log log = LogFactory.getLog(LuceneIndexFactory.class.getName());

	public static final String LUCENE_INDEX_FACTORY= "LuceneIndexFactory";
	
	/**
	 * Get a lucene IndexSearch. This may return null.
	 */
    public static IndexSearcher getIndexSearcher( ServletContext context){    	
    	return getLuceneIndexFactoryFromContext(context).innerGetIndexSearcher(context);    	
    }
    
    public static LuceneIndexFactory getLuceneIndexFactoryFromContext(ServletContext context){
        Object obj = context.getAttribute(LUCENE_INDEX_FACTORY);
        if( obj == null ){
            setup(context);
            obj = context.getAttribute(LUCENE_INDEX_FACTORY);
        }
        if( obj == null ){
            log.error("cannot get LuceneIndexFactory from context.  Search is not setup correctly");
            return null;
        }
        if( ! (obj instanceof LuceneIndexFactory)){
            log.error("LuceneIndexFactory in context was not of correct type. Expected " + LuceneIndexFactory.class.getName() 
                    + " found " + obj.getClass().getName() + " Search is not setup correctly");
            return null;
        }        
        return (LuceneIndexFactory)obj;
    }
    
    
    public static void setup(ServletContext context){
        LuceneIndexFactory lif = (LuceneIndexFactory)context.getAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY);
        if( lif == null ){
            context.setAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY, new LuceneIndexFactory());
        }   
    }   
        
    /**
     * This method can be used to force the LuceneIndexFactory to return a new IndexSearcher.
     * This will force a re-opening of the search index.
     * 
     * This could be useful if the index was rebult in a different directory on the file system.
     */
    public synchronized void forceNewIndexSearcher(){
        log.debug("forcing the re-opening of the search index");
        searcher = null;
    }
    
	private synchronized IndexSearcher innerGetIndexSearcher(ServletContext context) {
		if (searcher == null ) {	    
			String indexDir = getIndexDir( context );
			if( indexDir != null ){
				try {
					Directory fsDir = FSDirectory.getDirectory(indexDir);
					searcher = new IndexSearcher(fsDir);
				} catch (IOException e) {
					log.error("could not make indexSearcher " + e);
					log.error("It is likely that you have not made a directory for the lucene index.  "
								+ "Create the directory indicated in the error and set permissions/ownership so"
								+ " that the tomcat server can read and write to it.");
				}		
			}else{
			    log.error("Could not create IndexSearcher because index directory was null. It may be that the LucenSetup.indexDir is " +
			    		" not set in your deploy.properties file.");
			}
	    }		
		return searcher;
	}
		
	private String getIndexDir(ServletContext servletContext){
		Object obj = servletContext.getAttribute(LuceneSetup.INDEX_DIR);
		if (obj == null ){
			log.error("could not find " + LuceneSetup.INDEX_DIR + " in context. Search is not configured correctly.");
			return null;
		}else if ( !(obj instanceof String) ){
			log.error( LuceneSetup.INDEX_DIR + " from context was not a String. Search is not configured correctly.");
			return null;
		}else
			return (String) obj;
	}
		
}
