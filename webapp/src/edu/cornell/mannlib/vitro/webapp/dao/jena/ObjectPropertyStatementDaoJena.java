/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;

public class ObjectPropertyStatementDaoJena extends JenaBaseDao implements ObjectPropertyStatementDao {

    private static final Log log = LogFactory.getLog(ObjectPropertyStatementDaoJena.class);
    
    private DatasetWrapperFactory dwf;
    
    public ObjectPropertyStatementDaoJena(DatasetWrapperFactory dwf,
                                          WebappDaoFactoryJena wadf) {
        super(wadf);
        this.dwf = dwf;
    }

    @Override
    public void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
    	deleteObjectPropertyStatement(objPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,objPropertyStmt.getSubjectURI()));
        try {
            Resource s = ontModel.getResource(objPropertyStmt.getSubjectURI());
            com.hp.hpl.jena.rdf.model.Property p = ontModel.getProperty(objPropertyStmt.getPropertyURI());
            Resource o = ontModel.getResource(objPropertyStmt.getObjectURI());
            if ((s != null) && (p != null) && (o != null)) {
                ontModel.remove(s,p,o);
            }
            List<Statement> dependentResources = DependentResourceDeleteJena.getDependentResourceDeleteList(o, ontModel);
            ontModel.remove(dependentResources);
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,objPropertyStmt.getSubjectURI()));
            ontModel.leaveCriticalSection();
        }
    }

    @Override
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity.getURI() == null)
            return entity;
        else {
        	Map<String, ObjectProperty> uriToObjectProperty = new HashMap<String,ObjectProperty>();
        	
        	ObjectPropertyDaoJena opDaoJena = new ObjectPropertyDaoJena(dwf, getWebappDaoFactory());
        	
        	OntModel ontModel = getOntModelSelector().getABoxModel();
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
	                                log.error(t, t);
	                            }
	                            objPropertyStmt.setPropertyURI(st.getPredicate().getURI());
	                            try {
	                                Property prop = st.getPredicate();
	                                if( uriToObjectProperty.containsKey(prop.getURI())){
	                                	objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
	                                }else{
	                                	ObjectProperty p = opDaoJena.propertyFromOntProperty(getOntModel().getObjectProperty(prop.getURI()));
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
	                                Individual objInd = getWebappDaoFactory().getIndividualDao().getIndividualByURI(objPropertyStmt.getObjectURI());
	                                objPropertyStmt.setObject(objInd);
	                            }
	
	                            //add object property statement to list for Individual
	                            if ((objPropertyStmt.getSubjectURI() != null) && (objPropertyStmt.getPropertyURI() != null) && (objPropertyStmt.getObject() != null)){
	                                objPropertyStmtList.add(objPropertyStmt);                           
	                            } 
	                        } catch (Throwable t) {
	                            log.error(t, t);
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
    
    private int NO_LIMIT = -1;
    
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements (ObjectProperty objectProperty) {
    	return getObjectPropertyStatements(objectProperty, NO_LIMIT, NO_LIMIT);
    }
    
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements (ObjectProperty objectProperty, int startIndex, int endIndex) {
    	getOntModel().enterCriticalSection(Lock.READ);
    	List<ObjectPropertyStatement> opss = new ArrayList<ObjectPropertyStatement>();
    	try {
    		Property prop = ResourceFactory.createProperty(objectProperty.getURI());
    		ClosableIterator<Statement> opsIt = getOntModel().listStatements(null,prop,(Resource)null);
    		try {
    			int count = 0;
    			while ( (opsIt.hasNext()) && ((endIndex<0) || (count<endIndex)) ) {
    				Statement stmt = (Statement) opsIt.next();
    				if (stmt.getObject().isResource()) {
	    				++count;
	    				if (startIndex<0 || startIndex<=count) {
	    					Resource objRes = (Resource) stmt.getObject();
	    					if (!objRes.isAnon()) {
			    				ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
			    				ops.setSubjectURI(stmt.getSubject().getURI());
			    				ops.setPropertyURI(objectProperty.getURI());		
			    				ops.setObjectURI(objRes.getURI());
			    				opss.add(ops);
	    					}
	    				}
    				}
    			}
    		} finally {
    			opsIt.close();
    		}
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    	return opss;
    }

	@Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements(
			ObjectPropertyStatement objPropertyStmt) {
		List<ObjectPropertyStatement> opss = new ArrayList<ObjectPropertyStatement>();

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			String subjectUri = objPropertyStmt.getSubjectURI();
			String propertyUri = objPropertyStmt.getPropertyURI();
			String objectUri = objPropertyStmt.getObjectURI();

			Resource subject = (subjectUri == null) ? null : ResourceFactory.createResource(subjectUri);
			Property property = (propertyUri == null) ? null : ResourceFactory
					.createProperty(propertyUri);
			Resource object = (objectUri == null) ? null : ResourceFactory.createResource(objectUri);
			StmtIterator opsIt = getOntModel().listStatements(subject, property, object);
			try {
				while (opsIt.hasNext()) {
					Statement stmt = opsIt.next();
					if (stmt.getObject().isResource()) {
						Resource objRes = (Resource) stmt.getObject();
						if (!objRes.isAnon()) {
							ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
							ops.setSubjectURI(stmt.getSubject().getURI());
							ops.setPropertyURI(stmt.getPredicate().getURI());
							ops.setObjectURI(objRes.getURI());
							opss.add(ops);
						}
					}
				}
			} finally {
				opsIt.close();
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}
		return opss;
	}

    @Override
	public int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
    	return insertNewObjectPropertyStatement(objPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,objPropertyStmt.getSubjectURI()));
        try {
            Resource s = ontModel.getResource(objPropertyStmt.getSubjectURI());
            com.hp.hpl.jena.rdf.model.Property p = ontModel.getProperty(objPropertyStmt.getPropertyURI());
            Resource o = ontModel.getResource(objPropertyStmt.getObjectURI());
            if ((s != null) && (p != null) && (o != null)) {
                ontModel.add(s,p,o);
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,objPropertyStmt.getSubjectURI()));
            ontModel.leaveCriticalSection();
        }
        return 0;
    }

    /*
     * SPARQL-based method for getting values related to a single object property.
     * We cannot return a List<ObjectPropertyStatement> here, the way the corresponding method of
     * DataPropertyStatementDaoJena returns a List<DataPropertyStatement>. We need to accomodate
     * custom queries that could request any data in addition to just the object of the statement.
     * However, we do need to get the object of the statement so that we have it to create editing links.
     */
    
    @Override 
    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
            String subjectUri, 
            String propertyUri, 
            String objectKey, String queryString) {
        
        return getObjectPropertyStatementsForIndividualByProperty(
                subjectUri, propertyUri, objectKey, objectKey, null);
        
    }
    
    @Override
    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
            String subjectUri, 
            String propertyUri, 
            String objectKey, 
            String queryString, Set<String> constructQueryStrings ) {  
        
        Model constructedModel = constructModelForSelectQueries(
                subjectUri, propertyUri, constructQueryStrings);
        
        log.debug("Query string for object property " + propertyUri + ": " + queryString);
        
        Query query = null;
        try {
            query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        } catch(Throwable th){
            log.error("Could not create SPARQL query for query string. " + th.getMessage());
            log.error(queryString);
            return Collections.emptyList();
        } 
        
        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("subject", ResourceFactory.createResource(subjectUri));
        initialBindings.add("property", ResourceFactory.createResource(propertyUri));

        // Run the SPARQL query to get the properties
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        DatasetWrapper w = dwf.getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qexec = null;
        try {

            qexec = (constructedModel == null) 
                    ? QueryExecutionFactory.create(
                            query, dataset, initialBindings)
                    : QueryExecutionFactory.create(
                            query, constructedModel, initialBindings);
            
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                RDFNode node = soln.get(objectKey);
                if (node.isURIResource()) {
                    list.add(QueryUtils.querySolutionToStringValueMap(soln));
                }
            }
            return list;
            
        } catch (Exception e) {
            log.error("Error getting object property values for subject " + subjectUri + " and property " + propertyUri);
            return Collections.emptyList();
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
            if (qexec != null) {
                qexec.close();
            }
        }
       
    }
 
    private Model constructModelForSelectQueries(String subjectUri,
                                                 String propertyUri,
                                                 Set<String> constructQueries) {
        
        if (constructQueries == null) {
            return null;
        }
        
        Model constructedModel = ModelFactory.createDefaultModel();
        
        for (String queryString : constructQueries) {
         
            log.debug("CONSTRUCT query string for object property " + 
                    propertyUri + ": " + queryString);
            
            Query query = null;
            try {
                query = QueryFactory.create(queryString, Syntax.syntaxARQ);
            } catch(Throwable th){
                log.error("Could not create CONSTRUCT SPARQL query for query " +
                          "string. " + th.getMessage());
                log.error(queryString);
                return constructedModel;
            } 
        
            QuerySolutionMap initialBindings = new QuerySolutionMap();
            initialBindings.add(
                    "subject", ResourceFactory.createResource(subjectUri));
            initialBindings.add(
                    "property", ResourceFactory.createResource(propertyUri));
        
            DatasetWrapper w = dwf.getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            QueryExecution qe = null;
            try {                           
                qe = QueryExecutionFactory.create(
                        query, dataset, initialBindings);
                qe.execConstruct(constructedModel);
            } catch (Exception e) {
                log.error("Error getting constructed model for subject " + subjectUri + " and property " + propertyUri);
            } finally {
                if (qe != null) {
                    qe.close();
                }
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
        }
        
        return constructedModel;
        
    }
    
    protected static final String MOST_SPECIFIC_TYPE_QUERY = ""
        + "PREFIX rdfs: <" + VitroVocabulary.RDFS + "> \n"
        + "PREFIX vitro: <" + VitroVocabulary.vitroURI + "> \n"
        + "SELECT DISTINCT ?label ?type WHERE { \n"
        + "    ?subject vitro:mostSpecificType ?type . \n"
        + "    ?type rdfs:label ?label . \n"
        + "    ?type vitro:inClassGroup ?classGroup . \n"
        + "    ?classGroup a ?ClassGroup \n"
        + "} ORDER BY ?label ";

    @Override
    /** 
     * Finds all mostSpecificTypes of an individual that are members of a classgroup.
     * Returns a list of type labels.
     * **/
    public Map<String, String> getMostSpecificTypesInClassgroupsForIndividual(String subjectUri) {
        
        String queryString = QueryUtils.subUriForQueryVar(MOST_SPECIFIC_TYPE_QUERY, "subject", subjectUri);
        
        log.debug("Query string for vitro:mostSpecificType : " + queryString);
        
        Query query = null;
        try {
            query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        } catch(Throwable th){
            log.error("Could not create SPARQL query for query string. " + th.getMessage());
            log.error(queryString);
            return Collections.emptyMap();
        }        
        
        Map<String, String> types = new LinkedHashMap<String, String>();
        DatasetWrapper w = dwf.getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qexec = null;
        try {
            
            qexec = QueryExecutionFactory.create(query, dataset);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();       

                RDFNode typeNode = soln.get("type");
                String type = null;
                if (typeNode.isURIResource()) {
                     type = typeNode.asResource().getURI();
                }
                
                RDFNode labelNode = soln.get("label");
                String label = null;
                if (labelNode.isLiteral()) {
                    label = labelNode.asLiteral().getLexicalForm();
                }
                
                if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(label)) {
                    types.put(type, label);
                }
            }
            return types;
            
        } catch (Exception e) {
            log.error("Error getting most specific types for subject " + subjectUri);
            return Collections.emptyMap();
            
        } finally {
            dataset.getLock().leaveCriticalSection();
            if (qexec != null) {
                qexec.close();
            }
            w.close();
        }
           
    }
}
