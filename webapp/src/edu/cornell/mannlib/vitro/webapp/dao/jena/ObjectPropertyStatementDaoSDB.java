/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;

public class ObjectPropertyStatementDaoSDB extends
		ObjectPropertyStatementDaoJena implements ObjectPropertyStatementDao {

    private static final Log log = LogFactory.getLog(ObjectPropertyStatementDaoSDB.class);
    
	private DatasetWrapperFactory dwf;
	private SDBDatasetMode datasetMode;
	
	public ObjectPropertyStatementDaoSDB(
	            DatasetWrapperFactory dwf, 
	            SDBDatasetMode datasetMode,
	            WebappDaoFactoryJena wadf) {
		super (dwf, wadf);
		this.dwf = dwf;
		this.datasetMode = datasetMode;
	}
	
	@Override
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity.getURI() == null)
            return entity;
        else {
        	Map<String, ObjectProperty> uriToObjectProperty = new HashMap<String,ObjectProperty>();
        	String query = "CONSTRUCT { \n" +
        			       "   <" + entity.getURI() + "> ?p ?o . \n" +
//        			       "   ?o a ?oType . \n" +
//        			       "   ?o <" + RDFS.label.getURI() + "> ?oLabel .  \n" +
//        			       "   ?o <" + VitroVocabulary.MONIKER + "> ?oMoniker  \n" +
        			       "} WHERE { \n" +
        			       "   { <" + entity.getURI() + "> ?p ?o } \n" +
//        			       "   UNION { <" + entity.getURI() + "> ?p ?o . ?o a ?oType } \n" +
//        			       "   UNION { <" + entity.getURI() + "> ?p ?o . \n" +
//        			       "           ?o <" + RDFS.label.getURI() + "> ?oLabel } \n" +
//        			       "   UNION { <" + entity.getURI() + "> ?p ?o . \n " +
//        			       "           ?o <" + VitroVocabulary.MONIKER + "> ?oMoniker } \n" +
        			       "}";
        	long startTime = System.currentTimeMillis();
        	Model m = null;
        	DatasetWrapper w = dwf.getDatasetWrapper();
        	Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
        	QueryExecution qexec = null;
        	try {
        		qexec = QueryExecutionFactory.create(QueryFactory.create(query), dataset);
        		m = qexec.execConstruct();
        	} finally {
        	    if(qexec != null) qexec.close();
        		dataset.getLock().leaveCriticalSection();
        		w.close();
        	}
        	if (log.isDebugEnabled()) {
	        	log.debug("Time (ms) to query for related individuals: " + (System.currentTimeMillis() - startTime));
	        	if (System.currentTimeMillis() - startTime > 1000) {
	        		//log.debug(query);
	        		log.debug("Results size (statements): " + m.size());
	        	}
        	}
        	
        	OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
        	ontModel.enterCriticalSection(Lock.READ);
        	try {
	            Resource ind = ontModel.getResource(entity.getURI());
	            List<ObjectPropertyStatement> objPropertyStmtList = new ArrayList<ObjectPropertyStatement>();
	            ClosableIterator<Statement> propIt = ind.listProperties();
	            try {
	                while (propIt.hasNext()) {
	                    Statement st = (Statement) propIt.next();
	                    if (st.getObject().isResource() && !(NONUSER_NAMESPACES.contains(st.getPredicate().getNameSpace()))) {
	                        try {
	                            ObjectPropertyStatement objPropertyStmt = new ObjectPropertyStatementImpl();
	                            objPropertyStmt.setSubjectURI(entity.getURI());
	                            objPropertyStmt.setSubject(entity);
	                            objPropertyStmt.setObjectURI(((Resource)st.getObject()).getURI());
	                            
	                            objPropertyStmt.setPropertyURI(st.getPredicate().getURI());
                                Property prop = st.getPredicate();
                                if( uriToObjectProperty.containsKey(prop.getURI())){
                                	objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
                                }else{
                                	ObjectProperty p = getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURI(prop.getURI());
                                	if( p != null ){
                                		uriToObjectProperty.put(prop.getURI(), p);
                                		objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
                                	}else{
                                		//if ObjectProperty not found in ontology, skip it
                                		continue;
                                	}
                                }                                

	                            if (objPropertyStmt.getObjectURI() != null) {
	                                //this might throw IndividualNotFoundException
                                    Individual objInd = new IndividualSDB(
                                        objPropertyStmt.getObjectURI(), 
                                        this.dwf, 
                                        datasetMode,
                                        getWebappDaoFactory());
                                    objPropertyStmt.setObject(objInd);	                                
	                            }
	                            
	                            //only add statement to list if it has its values filled out
	                            if (    (objPropertyStmt.getSubjectURI() != null) 
	                                 && (objPropertyStmt.getPropertyURI() != null) 
	                                 && (objPropertyStmt.getObject() != null) ) {
	                                objPropertyStmtList.add(objPropertyStmt);                           
	                            } 
	                            
	                        } catch (IndividualNotFoundException t) {
	                            log.debug(t,t);
	                            continue;
	                        } catch (Throwable t){
	                            log.error(t,t);
                                continue;
	                        }
	                    }
	                }
	            } finally {
	                propIt.close();
	            }
	            entity.setObjectPropertyStatements(objPropertyStmtList);
        	} finally {
        		ontModel.leaveCriticalSection();
        	}
            return entity;
        }
    }
	
}
