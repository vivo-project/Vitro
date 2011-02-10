/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import freemarker.template.Configuration;

public class NavigationController extends FreemarkerHttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(NavigationController.class.getName());
	
	//private Map<Pattern,String> urlPatternToURI;
	private NavigationURLPatternListener urlPatterns;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		OntModel displayOntModel = (OntModel)config.getServletContext().getAttribute("displayOntModel");
		this.urlPatterns = new NavigationURLPatternListener( );
		displayOntModel.getBaseModel().register( urlPatterns );
	}
	
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {		
		OntModel displayOntModel = (OntModel)getServletContext().getAttribute("displayOntModel");
		OntModel jenaOntModel = (OntModel)getServletContext().getAttribute("jenaOntModel");
				
		//figure out what is being requested
		Individual ind = urlPatterns.getDisplayIndividual(vreq.getPathInfo(),displayOntModel);		
		Map<String,Object> values = getValues(ind, displayOntModel,jenaOntModel, getValuesFromRequest(/*?*/) );		
		String template = getTemplate(ind, displayOntModel);		
		
		return new TemplateResponseValues(template, values);
	}
	
	private Map<String,Object>getValuesFromRequest(){
		// TODO: figure out how to get request for FreeMarkerHttpServlet. 
		return Collections.emptyMap();
	}
	
	private String getTemplate(Individual ind, OntModel displayOntModel) {
		if( ind == null ) return "defaultBody";
		
		// check vitroDisplay:requiresBodyTemplate
		displayOntModel.enterCriticalSection(Model.READ);
		StmtIterator it = displayOntModel.listStatements(ind, DisplayVocabulary.REQUIRES_BODY_TEMPLATE, (RDFNode) null);
		//NodeIterator it = ind.listPropertyValues(DisplayVocabulary.REQUIRES_BODY_TEMPLATE);
		try{										
			while(it.hasNext()){
				Statement stmt = it.nextStatement();
				if(  stmt.getObject().isLiteral() ){
					String template = ((Literal)stmt.getObject().as(Literal.class)).getLexicalForm();
					if( template != null && template.length() > 0 ){
						return template;							
					}
				}
			}
		}finally{
			it.close();
			displayOntModel.leaveCriticalSection();
		}
		return "defaultBody";
	}

	Map<String,Object> getValues(Individual ind, OntModel displayOntModel, OntModel assertionModel, Map<String,Object> baseValues){
		if( ind == null ) return Collections.emptyMap();
								
		/* Figure out what ValueFactories are specified in the display ontology for this individual. */
		Set<ValueFactory> valueFactories = new HashSet<ValueFactory>();
		displayOntModel.enterCriticalSection(Model.READ);
		StmtIterator stmts = ind.listProperties(DisplayVocabulary.REQUIRES_VALUES);		
		try{										
			while(stmts.hasNext()){
				Statement stmt = stmts.nextStatement();
				RDFNode obj =  stmt.getObject();
				valueFactories.addAll(getValueFactory(obj,displayOntModel));
			}
		}finally{
			stmts.close();
			displayOntModel.leaveCriticalSection();
		}
		
		/* Get values from the ValueFactories. */
		HashMap<String,Object> values = new HashMap<String,Object>();
		values.putAll(baseValues);
		for(ValueFactory vf : valueFactories){
			values.putAll( vf.getValues(assertionModel, values));
		}
		return values;
	}
	
	protected Set<ValueFactory> getValueFactory( RDFNode valueNode, OntModel displayOntModel) {
		//maybe use jenabean or owl2java for this?
		if(  valueNode.isResource() ){
			Resource res = (Resource)valueNode.as(Resource.class);
			Statement stmt = res.getProperty(DisplayVocabulary.JAVA_CLASS_NAME);
			if( stmt == null || !stmt.getObject().isLiteral() ){
				log.debug("Cannot build value factory: java class was " + stmt.getObject());
				return Collections.emptySet();
			}
			String javaClassName = ((Literal)stmt.getObject().as(Literal.class)).getLexicalForm();
			if( javaClassName == null || javaClassName.length() == 0 ){
				log.debug("Cannot build value factory: no java class was set.");
				return Collections.emptySet();
			}
			Class<?> clazz;
			Object newObj;
			try {
				clazz = Class.forName(javaClassName);
			} catch (ClassNotFoundException e) {
				log.debug("Cannot build value factory: no class found for " + javaClassName);
				return Collections.emptySet();
			}
			try {
				newObj = clazz.newInstance();
			} catch (Exception e) {
				log.debug("Cannot build value factory: exception while creating object of java class " + javaClassName + " " + e.getMessage());
				return Collections.emptySet();
			}
			if( newObj instanceof ValueFactory){
				ValueFactory valueFactory = (ValueFactory)newObj;
				return Collections.singleton( valueFactory );
			}else{
				log.debug("Cannot build value factory: " + javaClassName + " does not implement " + ValueFactory.class.getName() );
				return Collections.emptySet();
			}			
		}else{
			log.debug("Cannot build value factory for " + valueNode);
			return Collections.emptySet();
		}		
	}
	
	 interface ValueFactory {
		void configure( Map<String,String> config);
		Map<String,Object> getValues(OntModel model, Map<String,Object> values);		
	}
		
	private class NavigationURLPatternListener extends StatementListener {		
		private Map<Pattern,String> urlPatternToURI;
		
		public synchronized Map<Pattern,String> getUrlPatternToURIMap(){
			if( urlPatternToURI == null || urlPatternToURI.isEmpty() ){
				this.urlPatternToURI = buildUrlPatternToURI();
			}
			return urlPatternToURI;
		}
		
		protected synchronized void invalidateURLPatternMap(){
			this.urlPatternToURI = null;
		}
		
		public Individual getDisplayIndividual( String pathInfo , OntModel displayModel){
			Map<Pattern,String> map = getUrlPatternToURIMap();
			for( Pattern regex : map.keySet()){
				Matcher m = regex.matcher(pathInfo);
				if(m.matches() ){
					return displayModel.getIndividual(map.get(regex));
				}
			}
			return null;
		}
		
		protected synchronized Map<Pattern,String> buildUrlPatternToURI(){
			OntModel displayModel = (OntModel)getServletContext().getAttribute("displayOntModel");
			Map<Pattern,String> map = new HashMap<Pattern,String>();
			StmtIterator stmts = displayModel.listStatements(null, DisplayVocabulary.URL_MAPPING,(Literal)null);		
			while(stmts.hasNext()){			
				Statement stmt = stmts.nextStatement();
				if( stmt.getSubject().isURIResource() && stmt.getObject().isLiteral()){
					Resource r = (Resource)stmt.getSubject().as( Resource.class);				
					Pattern regex = Pattern.compile(stmt.getLiteral().getLexicalForm());
					map.put(regex,r.getURI());
				}
			}
			return map;
		}			
		
		@Override
		public void addedStatement(Statement s) {
			invalidateURLPatternMap();
		}
		@Override
		public void removedStatement(Statement s) {
			invalidateURLPatternMap();
		}	
	}	
}
