/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

/**
 * This class is thread safe.  Notice that doAsyncIndexBuild() is frequently 
 * called because the inference system does not seem to send notifyEvents. 
 */
public class SearchReindexingListener implements ModelChangedListener {						
	private IndexBuilder indexBuilder;    
	
	public SearchReindexingListener(IndexBuilder indexBuilder ) {
		if(indexBuilder == null )
			throw new IllegalArgumentException("Constructor parameter indexBuilder must not be null");		
		this.indexBuilder = indexBuilder;			
	}	

	private synchronized void addChange(Statement stmt){	    
		if( stmt == null ) return;
		if( log.isDebugEnabled() ){
		    String sub="unknown";
		    String pred = "unknown";
		    String obj ="unknown";
		    
		    if( stmt.getSubject().isURIResource() ){           
	            sub =  stmt.getSubject().getURI();
	        }	                
		    if( stmt.getPredicate() != null ){
		        pred = stmt.getPredicate().getURI();
		    }
	        if( stmt.getObject().isURIResource() ){          
	            obj =  ((Resource) (stmt.getObject().as(Resource.class))).getURI();
	        }else{
	            obj = stmt.getObject().toString();
	        }
	        log.debug("changed statement: sub='" + sub + "' pred='" + pred +"' obj='" + obj + "'");
        }
				
		indexBuilder.addToChanged(stmt);		
	}

	private void requestAsyncIndexUpdate(){
		indexBuilder.doUpdateIndex();
	}	
	
	@Override
	public void notifyEvent(Model arg0, Object arg1) {
		if ( (arg1 instanceof EditEvent) ){
			EditEvent editEvent = (EditEvent)arg1;
			if( !editEvent.getBegin() ){// editEvent is the end of an edit				
				log.debug("Doing search index build at end of EditEvent");				
				requestAsyncIndexUpdate();
			}		
		} else{
			log.debug("ignoring event " + arg1.getClass().getName() + " "+ arg1 );
		}
	}
	
	@Override
	public void addedStatement(Statement stmt) {
		addChange(stmt);
		requestAsyncIndexUpdate();
	}

	@Override
	public void removedStatement(Statement stmt){
		addChange(stmt);
		requestAsyncIndexUpdate();
	}
	
	private static final Log log = LogFactory.getLog(SearchReindexingListener.class.getName());

	@Override
	public void addedStatements(Statement[] arg0) {
		for( Statement s: arg0){
			addChange(s);
		}
		requestAsyncIndexUpdate();
	}

	@Override
	public void addedStatements(List<Statement> arg0) {
		for( Statement s: arg0){
			addChange(s);
		}
		requestAsyncIndexUpdate();
	}

	@Override
	public void addedStatements(StmtIterator arg0) {
		try{
			while(arg0.hasNext()){
				Statement s = arg0.nextStatement();
				addChange(s);
			}
		}finally{
			arg0.close();
		}
		requestAsyncIndexUpdate();		
	}

	@Override
	public void addedStatements(Model m) {
		m.enterCriticalSection(Lock.READ);
		StmtIterator it = null;
		try{
			it = m.listStatements();
			while(it.hasNext()){
				addChange(it.nextStatement());
			}			
		}finally{
			if( it != null ) it.close();
			m.leaveCriticalSection();
		}
		requestAsyncIndexUpdate();
	}

	@Override
	public void removedStatements(Statement[] arg0) {
		//same as add stmts
		this.addedStatements(arg0);		
	}

	@Override
	public void removedStatements(List<Statement> arg0) {
		//same as add
		this.addedStatements(arg0);		
	}

	@Override
	public void removedStatements(StmtIterator arg0) {
		//same as add
		this.addedStatements(arg0);
	}

	@Override
	public void removedStatements(Model arg0) {
		//same as add
		this.addedStatements(arg0);
	}
}
