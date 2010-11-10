package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
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

public class ObjectPropertyStatementDaoSDB extends
		ObjectPropertyStatementDaoJena implements ObjectPropertyStatementDao {

	private Dataset dataset;
	
	public ObjectPropertyStatementDaoSDB(Dataset dataset, WebappDaoFactoryJena wadf) {
		super (wadf);
		this.dataset = dataset;
	}
	
	@Override
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity.getURI() == null)
            return entity;
        else {
        	Map<String, ObjectProperty> uriToObjectProperty = new HashMap<String,ObjectProperty>();
        	String query = "CONSTRUCT { \n" +
        			       "   <" + entity.getURI() + "> ?p ?o . \n" +
        			       "   ?o a ?oType . \n" +
        			       "   ?o <" + RDFS.label.getURI() + "> ?oLabel .  \n" +
        			       "   ?o <" + VitroVocabulary.MONIKER + "> ?oMoniker  \n" +
        			       "} WHERE { GRAPH ?g { \n" +
        			       "   <" + entity.getURI() + "> ?p ?o . \n" +
        			       "   ?o a ?oType \n" +
        			       "   OPTIONAL { ?o <" + RDFS.label.getURI() + "> ?oLabel }  \n" +
        			       "   OPTIONAL { ?o <" + VitroVocabulary.MONIKER + "> ?oMoniker }  \n" +
                           "} }";
        	long startTime = System.currentTimeMillis();
        	dataset.getLock().enterCriticalSection(Lock.READ);
        	Model m = null;
        	try {
        		m = QueryExecutionFactory.create(QueryFactory.create(query), dataset).execConstruct();
        	} finally {
        		dataset.getLock().leaveCriticalSection();
        	}
        	if (log.isDebugEnabled()) {
	        	log.debug("Time (ms) to query for related individuals: " + (System.currentTimeMillis() - startTime));
	        	if (System.currentTimeMillis() - startTime > 1000) {
	        		//log.debug(query);
	        		log.debug("Results size (statements): " + m.size());
	        	}
        	}
            ObjectPropertyDaoJena opDaoJena = new ObjectPropertyDaoJena(getWebappDaoFactory());
        	
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
	                            try {
	                                objPropertyStmt.setObjectURI(((Resource)st.getObject()).getURI());
	                            } catch (Throwable t) {
	                                t.printStackTrace();
	                            }
	                            objPropertyStmt.setPropertyURI(st.getPredicate().getURI());
	                            try {
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
	                            } catch (Throwable g) {
	                                //do not add statement to list
	                            	log.debug("exception while trying to get object property for statement list, statement skipped.", g);
	                            	continue;                                                                
	                            }
	                            if (objPropertyStmt.getObjectURI() != null) {
	                                Individual objInd = new IndividualSDB(objPropertyStmt.getObjectURI(), dataset, getWebappDaoFactory(), m);
	                                objPropertyStmt.setObject(objInd);
	                            }
	
	                            //add object property statement to list for Individual
	                            if ((objPropertyStmt.getSubjectURI() != null) && (objPropertyStmt.getPropertyURI() != null) && (objPropertyStmt.getObject() != null)){
	                                objPropertyStmtList.add(objPropertyStmt);                           
	                            } 
	                        } catch (Throwable t) {
	                            t.printStackTrace();
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
