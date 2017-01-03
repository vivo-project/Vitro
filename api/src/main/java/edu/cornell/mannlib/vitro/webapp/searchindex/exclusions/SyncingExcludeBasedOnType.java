/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.EXCLUDE_CLASS;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.SEARCH_INDEX_URI;

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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

/**
 * This excludes based on types defined as EXCLUDE_CLASS in the 
 * configuration RDF model. 
 */
public class SyncingExcludeBasedOnType extends ExcludeBasedOnType implements ModelChangedListener, ContextModelsUser {
    static final Log log = LogFactory.getLog(SyncingExcludeBasedOnType.class);        

    private static final String queryForProhibitedClasses = 
        "SELECT ?prohibited WHERE{" +
        "?searchConfig <" + EXCLUDE_CLASS + "> ?prohibited . " +
        "}";
    
    private ContextModelAccess models;

    @Override
	public void setContextModels(ContextModelAccess models) {
		this.models = models;
	}

	@Validation
    public void buildClassList( ){
		OntModel model = models.getOntModel(ModelNames.DISPLAY);
        this.setExcludedTypes( buildProhibitedClassesList(SEARCH_INDEX_URI, model) );
        log.debug(this);
    }       
        
    private List<String> buildProhibitedClassesList( String URI, Model model){
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

    
    /* ************* Methods for ModelChangeListener *************** */
    
    @Override
    public void addedStatement(Statement s) {
        try{
            if( isExcludeClassPredicate( s ) && isAboutSearchIndex(s)){             
                if( s.getObject() != null && s.getObject().isURIResource()){                
                    String classURI = s.getObject().asResource().getURI();     
                    this.addTypeToExclude(classURI);
                    log.debug("prohibited classes: " + this);
                }
            }
        }catch(Exception ex){
            log.error("could not add statement",ex);
        }
        
    }

    @Override
    public void removedStatement(Statement s) { 
        try{
            if( isExcludeClassPredicate( s ) && isAboutSearchIndex(s)){             
                if( s.getObject() != null && s.getObject().isURIResource()){            
                    String classURI = s.getObject().asResource().getURI();                            
                    this.removeTypeToExclude(classURI);
                    log.debug("prohibited classes: " + this);
                }
            }
        }catch(Exception ex){
            log.error("could not remove statement",ex);
        }
    }
    
    private boolean isExcludeClassPredicate(Statement s){
        return s != null
            && s.getPredicate() != null
            && EXCLUDE_CLASS.getURI().equals( s.getPredicate().getURI()); 
    }
    
    private boolean isAboutSearchIndex(Statement s){
        if( s.getSubject() != null ){
            String subURI = s.getSubject().getURI() ;
            return SEARCH_INDEX_URI.equals(subURI);
        }else{
            return false;
        }
    }
    
    @Override
    public void addedStatements(Statement[] stmts) {
        if( stmts != null ){
            for( Statement stmt : stmts){
                addedStatement(stmt);
            }
        }        
    }

    @Override
    public void addedStatements(List<Statement> stmts) {
        if( stmts != null ){
            for( Statement stmt : stmts){
                addedStatement(stmt);
            }
        }   
    }

    @Override
    public void addedStatements(StmtIterator it) {
        while(it.hasNext()){
            Statement stmt = it.nextStatement();
            addedStatement(stmt);
        }        
    }

    @Override
    public void addedStatements(Model model) {
        if( model != null){
			addedStatements(model.listStatements(
					model.createResource(SEARCH_INDEX_URI), EXCLUDE_CLASS,
					(RDFNode) null));
        }        
    }

    @Override
    public void notifyEvent(Model arg0, Object arg1) {
        //nothing        
    }

    @Override
    public void removedStatements(Statement[] stmts) {
        if( stmts != null ){
            for( Statement stmt : stmts){
                removedStatement(stmt);
            }
        }       
    }

    @Override
    public void removedStatements(List<Statement> stmts) {
        if( stmts != null ){
            for( Statement stmt : stmts){
                removedStatement(stmt);
            }
        }       
    }

    @Override
    public void removedStatements(StmtIterator it) {
        while(it.hasNext()){
            Statement stmt = it.nextStatement();
            removedStatement(stmt);
        } 
    }

    @Override
    public void removedStatements(Model model) {
        if( model != null){
			removedStatements(model.listStatements(
					model.createResource(SEARCH_INDEX_URI), EXCLUDE_CLASS,
					(RDFNode) null));
        }                
    }

}
