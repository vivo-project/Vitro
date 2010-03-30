/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;

public class ObjectPropertyStatementDaoJena extends JenaBaseDao implements ObjectPropertyStatementDao {

    public ObjectPropertyStatementDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

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

    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity.getURI() == null)
            return entity;
        else {
        	Map<String, ObjectProperty> uriToObjectProperty = new HashMap<String,ObjectProperty>();
        	
        	ObjectPropertyDaoJena opDaoJena = new ObjectPropertyDaoJena(getWebappDaoFactory());
            Resource ind = getOntModel().getResource(entity.getURI());
            List<ObjectPropertyStatement> objPropertyStmtList = new ArrayList<ObjectPropertyStatement>();
            ClosableIterator propIt = ind.listProperties();
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
                                	ObjectProperty p = opDaoJena.propertyFromOntProperty(getOntModel().createOntProperty(prop.getURI()));
                                	uriToObjectProperty.put(prop.getURI(), p);
                                	objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
                                }                                
                            } catch (Throwable g) {
                                ObjectProperty q = new ObjectProperty();
                                q.setDomainPublic("error");
                                // g.printStackTrace();
                            }
                            if (objPropertyStmt.getObjectURI() != null) {
                                Individual objInd = getWebappDaoFactory().getIndividualDao().getIndividualByURI(objPropertyStmt.getObjectURI());
                                objPropertyStmt.setObject(objInd);
                            }
                            if ((objPropertyStmt.getSubjectURI() != null) && (objPropertyStmt.getPropertyURI() != null) && (objPropertyStmt.getObject() != null))
                                objPropertyStmtList.add(objPropertyStmt);
                            else {
                                //log.error("At least one null value in ObjectPropertyStatement.  Discarding.");
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
            return entity;
        }
    }
    
    private int NO_LIMIT = -1;
    
    public List<ObjectPropertyStatement> getObjectPropertyStatements (ObjectProperty objectProperty) {
    	return getObjectPropertyStatements(objectProperty, NO_LIMIT, NO_LIMIT);
    }
    
    public List<ObjectPropertyStatement> getObjectPropertyStatements (ObjectProperty objectProperty, int startIndex, int endIndex) {
    	getOntModel().enterCriticalSection(Lock.READ);
    	List<ObjectPropertyStatement> opss = new ArrayList<ObjectPropertyStatement>();
    	try {
    		Property prop = ResourceFactory.createProperty(objectProperty.getURI());
    		ClosableIterator opsIt = getOntModel().listStatements(null,prop,(Resource)null);
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
    		getOntModel().leaveCriticalSection()
;    	}
    	return opss;
    }

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

}
