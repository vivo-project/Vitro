/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;


public class SearchReindexingListener implements ModelChangedListener {			
	private ServletContext context;		
	private HashSet<String> changedUris;	
	
	public SearchReindexingListener(OntModel ontModel, ServletContext sc) {		
		this.context = sc;
		this.changedUris = new HashSet<String>();		
	}	

	public void notifyEvent(Model arg0, Object arg1) {
		if ( (arg1 instanceof EditEvent) ){
			EditEvent editEvent = (EditEvent)arg1;
			if( editEvent.getBegin() ){
				
			}else{ // editEvent is the end of an edit
				log.debug("doing search index build");
				IndexBuilder builder = (IndexBuilder) context.getAttribute(IndexBuilder.class.getName());
				if( builder != null ){
					for( String uri: getAndClearChangedUris()){
						builder.addToChangedUris(uri);
					}				
					new Thread(builder).start();
				}else{
					log.debug("Could not get IndexBuilder from servlet context, cannot create index for full text seraching.");
					getAndClearChangedUris(); //clear list of changes because they cannot be indexed.
				}								
			}		
		} 
	}
	
	private boolean isNormalPredicate(Property p) {		
		if( p == null ) return false;
		
		/* currently the only predicate that is filtered out is rdf:type.
		 * It may be useful to improve this so that it may be configured 
		 * at run time.*/
		if( RDF.type.equals( p ))
			return false;
		else 
			return true;
	}

	private synchronized Set<String> getAndClearChangedUris(){
		log.debug("getting and clearing changed URIs.");
		
		Set<String> out = changedUris;
		changedUris = new HashSet<String>();
		return out;
	}
	
	private synchronized void addChange(Statement stmt){
		if( stmt == null ) return;
		if( stmt.getSubject().isURIResource() ){			
			changedUris.add( stmt.getSubject().getURI());
			log.debug(stmt.getSubject().getURI());
		}
				
		if( stmt.getObject().isURIResource() && isNormalPredicate( stmt.getPredicate() ) ){
			changedUris.add( ((Resource) stmt.getObject().as(Resource.class)).getURI() );
			log.debug(((Resource) stmt.getObject().as(Resource.class)).getURI());
		}	
	}
	
	public void addedStatement(Statement stmt) {
		addChange(stmt);
	}

	public void addedStatements(Statement[] arg0) {
		for(Statement stmt : arg0)
			addChange(stmt);
	}
	
	public void addedStatements(List arg0) {		
		for(Statement stmt : (List<Statement>)arg0)
			addChange(stmt);
	}
	
	public void addedStatements(StmtIterator arg0) {
		if( arg0 != null ){
			while( arg0.hasNext() ){
				addChange(arg0.nextStatement());
			}
		}
	}
	
	public void addedStatements(Model arg0) {
		if( arg0 != null)
			addedStatements(arg0.listStatements());
	}

	public void removedStatement(Statement stmt){
		addChange(stmt);
	}
	
	public void removedStatements(Statement[] arg0) {
		for(Statement stmt : arg0)
			addChange(stmt);
	}
	
	public void removedStatements(List arg0) {		
		for(Statement stmt : (List<Statement>)arg0)
			addChange(stmt);
	}
	
	public void removedStatements(StmtIterator arg0) {
		if( arg0 != null ){
			while( arg0.hasNext() ){
				addChange(arg0.nextStatement());
			}
		}
	}
	
	public void removedStatements(Model arg0) {
		if( arg0 != null)
			removedStatements(arg0.listStatements());
	}
	
	private static final Log log = LogFactory.getLog(SearchReindexingListener.class.getName());
}
