/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.vclassgroup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

public class ProhibitedFromSearch {
	List<String> prohibitedClasses;
	String ProhibitedFromSearchURI;
	
	private static final String queryForProhibitedClasses = "SELECT ?prohibited WHERE{" +
			"?searchConfig <" + DisplayVocabulary.EXCLUDE_CLASS + "> ?prohibited . }";
		
	protected static final Log log = LogFactory.getLog(ProhibitedFromSearch.class.getName()); 
		
	public ProhibitedFromSearch(String URI, OntModel model){
		this.ProhibitedFromSearchURI = URI;
		this.prohibitedClasses = new ArrayList<String>();
		addAllProhibitedClasses( buildProhibitedClassesList(URI,model) );
		model.register(new ProhibitedFromSearchChangeListener( this ));
	}
	 
	public synchronized boolean isClassProhibitedFromSearch(String classURI){
		if( classURI != null ){
			boolean p = prohibitedClasses.contains(classURI);
			if( p )
			    log.debug( classURI + " is prohibited from search");
			return p;
		}else{
			return false;
		}
	}
	
	private synchronized void removeProhibitedClass(String classURI){
		prohibitedClasses.remove(classURI);
	}
	
	private synchronized void addProhibitedClass(String classURI){
		prohibitedClasses.add(classURI);
	}
	
	private synchronized void addAllProhibitedClasses(List<String> classURIs){
		prohibitedClasses.addAll(classURIs);
	}
	
	private List<String> buildProhibitedClassesList( String URI, OntModel model){
		List<String> newProhibitedClasses = new ArrayList<String>();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource searchConfig = ResourceFactory.createResource(URI);
		initialBinding.add("searchConfig", searchConfig);

		Query query = QueryFactory.create(queryForProhibitedClasses);
		model.enterCriticalSection(Lock.READ);
		try{
			QueryExecution qExec = QueryExecutionFactory.create(query,model,initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				for(;results.hasNext();){
					QuerySolution soln = results.nextSolution();				
					RDFNode n = soln.get("prohibited");
					if( n.isResource() && !n.isAnon()){
						newProhibitedClasses.add(((Resource) n).getURI());
					}else{
						log.warn("unexpected node in object position for prohibited classes: " + n.toString());
					}
				}
			}catch(Throwable t){
				log.error(t,t);			
			}finally{ qExec.close(); }
		}finally{ model.leaveCriticalSection(); }
		
		return newProhibitedClasses;
	}

	private static enum ChangeType { ADD, REMOVE }
	
	class ProhibitedFromSearchChangeListener extends StatementListener {
		ProhibitedFromSearch pfs;
		
		ProhibitedFromSearchChangeListener(ProhibitedFromSearch pfs){
			this.pfs = pfs;
		}
		
		@Override
		public void addedStatement(Statement s) { processChange(s,ChangeType.ADD);}

		@Override
		public void removedStatement(Statement s) {	processChange(s,ChangeType.REMOVE); }
	
		private void processChange( Statement s, ChangeType add){
			//is it a change to an exclude class property?
			if( s != null && s.getPredicate() != null 
				&& s.getPredicate().getURI() != null 
				&& s.getPredicate().getURI().equals(DisplayVocabulary.EXCLUDE_CLASS.getURI())){
				
				//is it about this ProhibitedFromSearch?
				if( s.getSubject() != null ){
					String subURI = s.getSubject().getURI() ;
					if( subURI != null && subURI.equals( ProhibitedFromSearchURI )){
						if( s.getObject() != null && s.getObject().canAs(Resource.class)){
							String classURI = s.getObject().as(Resource.class).getURI();
							if( add == ChangeType.ADD )
								pfs.addProhibitedClass(classURI);
							else
								pfs.removeProhibitedClass(classURI);
						}
					}
				}
			}							
		}
		
	}
}
