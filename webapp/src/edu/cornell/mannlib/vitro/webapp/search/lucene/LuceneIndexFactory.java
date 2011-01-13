/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.File;
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
	String baseIndexDirName = null;
	
	private static final Log log = LogFactory.getLog(LuceneIndexFactory.class.getName());

	public static final String LUCENE_INDEX_FACTORY= "LuceneIndexFactory";
	
	public LuceneIndexFactory(String baseIndexDirName){
	    this.baseIndexDirName = baseIndexDirName;
	}
	
	/**
	 * Get a lucene IndexSearch. This may return null.
	 */
    public static IndexSearcher getIndexSearcher( ServletContext context){    	
    	return getLuceneIndexFactoryFromContext(context).innerGetIndexSearcher(context);    	
    }
    
    public static LuceneIndexFactory getLuceneIndexFactoryFromContext(ServletContext context){
        Object obj = context.getAttribute(LUCENE_INDEX_FACTORY);        
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
    
    
    public static LuceneIndexFactory setup(ServletContext context, String baseIndexDirName){
        LuceneIndexFactory lif = (LuceneIndexFactory)context.getAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY);
        if( lif == null ){
            lif = new LuceneIndexFactory(baseIndexDirName);
            context.setAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY, lif);
        }   
        return lif;        
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
			String liveDir = getLiveIndexDir( context );
			if( liveDir != null ){
				try {
					Directory fsDir = FSDirectory.getDirectory(liveDir);
					searcher = new IndexSearcher(fsDir);
				} catch (IOException e) {
				    String base = getBaseIndexDir();
					log.error("could not make IndexSearcher " + e);
					log.error("It is likely that you have not made a directory for the lucene index.  "
								+ "Create the directory " + base + " and set permissions/ownership so"
								+ " that the tomcat process can read and write to it.");
				}		
			}else{
			    log.error("Could not create IndexSearcher because index directory was null. It may be that the LucenSetup.indexDir is " +
			    		" not set in your deploy.properties file.");
			}
	    }		
		return searcher;
	}
		
	protected String getBaseIndexDir(){
	    if( this.baseIndexDirName == null )
	        log.error("LucenIndexFactory was not setup correctly, it must have a value for baseIndexDir");
		return this.baseIndexDirName;
	}
	
	protected String getLiveIndexDir(ServletContext servletContext){
	    String base = getBaseIndexDir();
	    if( base == null )
	        return null;
	    else
	        return base + File.separator + "live";	    
	}
	
	
		
}
