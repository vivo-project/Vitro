/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

/**
 * This excludes based on types defined as EXCLUDE_CLASS in the 
 * configuration RDF model. 
 */
public class SyncingExcludeBasedOnType extends ExcludeBasedOnType implements ModelChangedListener{
    static final Log log = LogFactory.getLog(SyncingExcludeBasedOnType.class);        

    private static final String queryForProhibitedClasses = 
        "SELECT ?prohibited WHERE{" +
        "?searchConfig <" + DisplayVocabulary.EXCLUDE_CLASS + "> ?prohibited . " +
        "}";
    
    String searchIndexURI = DisplayVocabulary.SEARCH_INDEX_URI;        
    
    public SyncingExcludeBasedOnType( Model model){            
        this.setExcludedTypes( buildProhibitedClassesList(searchIndexURI, model) );
        log.info("types excluded from search: " + typeURIs);
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
                if( s.getObject() != null && s.getObject().canAs(Resource.class)){                
                    String classURI = ((Resource)s.getObject().as(Resource.class)).getURI();     
                    this.addTypeToExclude(classURI);
                    log.debug("prohibited classes: " + this.typeURIs);
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
                if( s.getObject() != null && s.getObject().canAs(Resource.class)){            
                    String classURI = ((Resource)s.getObject().as(Resource.class)).getURI();                            
                    this.removeTypeToExclude(classURI);
                    log.debug("prohibited classes: " + this.typeURIs);
                }
            }
        }catch(Exception ex){
            log.error("could not remove statement",ex);
        }
    }
    
    private boolean isExcludeClassPredicate(Statement s){
        return s != null
            && s.getPredicate() != null
            && DisplayVocabulary.EXCLUDE_CLASS.getURI().equals( s.getPredicate().getURI()); 
    }
    
    private boolean isAboutSearchIndex(Statement s){
        if( s.getSubject() != null ){
            String subURI = ((Resource) s.getSubject()).getURI() ;
            return this.searchIndexURI.equals(subURI);
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
                    model.createResource(searchIndexURI), 
                    DisplayVocabulary.EXCLUDE_CLASS, 
                    (RDFNode)null));
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
                    model.createResource(searchIndexURI), 
                    DisplayVocabulary.EXCLUDE_CLASS, 
                    (RDFNode)null));
        }                
    }

}
