/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstanceIface;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualDeletionEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
public class PropertyInstanceDaoJena extends PropertyDaoJena implements
        PropertyInstanceDao {

    public PropertyInstanceDaoJena(RDFService rdfService, 
                                   DatasetWrapperFactory dwf, 
                                   WebappDaoFactoryJena wadf) {
        super(rdfService, dwf, wadf);
    }

    public void deleteObjectPropertyStatement(String subjectURI, String propertyURI, String objectURI) {
    	deleteObjectPropertyStatement(subjectURI, propertyURI, objectURI, getOntModelSelector());
    }

    public void deleteObjectPropertyStatement(String subjectURI, String propertyURI, String objectURI, 
                                              OntModelSelector ontModelSelector) {
        OntModel ontModel = ontModelSelector.getABoxModel();
        OntModel tboxModel = ontModelSelector.getTBoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource subjRes = ontModel.getResource(subjectURI);
            Property pred = tboxModel.getProperty(propertyURI);
            OntProperty invPred = null;                        
            if (pred.canAs(OntProperty.class)) {
            	invPred = pred.as(OntProperty.class).getInverse();
            }
            Resource objRes = ontModel.getResource(objectURI);
            Model baseModel = getOntModel().getBaseModel();
            String userUri = getWebappDaoFactory().getUserURI();
            if ( (subjRes != null) && (pred != null) && (objRes != null) ) {            	
            	baseModel.notifyEvent(new IndividualUpdateEvent(userUri,true,subjectURI));
            	try {
            		ontModel.remove(subjRes,pred,objRes);            		
            	} finally {
            		baseModel.notifyEvent(new IndividualUpdateEvent(userUri,false,subjectURI));
            	}
            	try{            		
            		baseModel.notifyEvent(new IndividualDeletionEvent(userUri,true,objectURI));            		
            		List<Statement> depResStmts = DependentResourceDeleteJena
                        .getDependentResourceDeleteList(ResourceFactory.createStatement(subjRes, pred, objRes),ontModel);
            		ontModel.remove(depResStmts);
            	} finally {
            		baseModel.notifyEvent(new IndividualDeletionEvent(userUri,false,objectURI));
            	}
                if (invPred != null) {
                	baseModel.notifyEvent(new IndividualUpdateEvent(userUri,true,objectURI));
                	try {
                		ontModel.remove(objRes,invPred,subjRes);
                	} finally {
                		baseModel.notifyEvent(new IndividualUpdateEvent(userUri,false,subjectURI));
                	}
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }
    
    @Override 
    public List<PropertyInstance> getAllPossiblePropInstForIndividual(String individualURI) {
    	return filterAndSort(super.getAllPossiblePropInstForIndividual(individualURI));	
    }
    
    @Override 
    public List<PropertyInstance> getAllPropInstByVClass(String vclassURI) {
    	return filterAndSort(super.getAllPropInstByVClass(vclassURI));
    }
    
    @Override
    public List<PropertyInstance> getAllPropInstByVClasses(List<VClass> vclasses) {
    	return filterAndSort(super.getAllPropInstByVClasses(vclasses));
    }
    
    private List<PropertyInstance>filterAndSort(List<PropertyInstance> propList) {
    	ArrayList<PropertyInstance> propInsts = new ArrayList<PropertyInstance>();
    	for (PropertyInstance propInst : propList) {
    		OntModel tboxModel = getOntModel();
    		tboxModel.enterCriticalSection(Lock.READ);
    		boolean add = false;
    		try { 
    			add = (propInst.getPropertyURI() != null 
    					&& tboxModel.contains(
    							tboxModel.getResource(
    									propInst.getPropertyURI()), 
    									RDF.type, 
    									OWL.ObjectProperty));
	    	} finally {
	    		tboxModel.leaveCriticalSection();
	    	}
	    	if (add) {
	    		propInsts.add(propInst);
	    	}
    	}
    	Collections.sort(propInsts, new PropInstSorter());
    	return propInsts;
    }

    private class PropInstSorter implements Comparator<PropertyInstance> {
        public int compare (PropertyInstance pi1, PropertyInstance pi2) {
            try {
                if (pi1.getDomainPublic().equals(pi2.getDomainPublic())) {
                    return pi1.getRangeClassName().compareTo(pi2.getRangeClassName());
                } else {
                    return (pi1.getDomainPublic().compareTo(pi2.getDomainPublic()));
                }
            } catch (NullPointerException npe) {
                return -1;
            }
        }
    }

    public Collection<PropertyInstance> getExistingProperties(String entityURI,
            String propertyURI) {
        Individual ent = getWebappDaoFactory().getIndividualDao().getIndividualByURI(entityURI);
        if (ent == null)
            return null;
        ent.sortForDisplay();
        List existingPropertyInstances = new ArrayList();
        if (ent.getObjectPropertyList()==null)
            return existingPropertyInstances;
        Iterator objPropertyIter = ent.getObjectPropertyList().iterator();
        while (objPropertyIter.hasNext()) {
            edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty op = (edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty) objPropertyIter.next();
            Iterator objPropertyStmtIter = op.getObjectPropertyStatements().iterator();
            while (objPropertyStmtIter.hasNext()) {
                ObjectPropertyStatement objPropertyStmt = (ObjectPropertyStatement) objPropertyStmtIter.next();
                if (propertyURI==null || objPropertyStmt.getPropertyURI().equals(propertyURI)) {
                    PropertyInstance pi = new PropertyInstance();
                    pi.setSubjectSide(true);
                    pi.setSubjectEntURI(ent.getURI());
                    pi.setSubjectName(ent.getName());
                    if (objPropertyStmt.getObject() != null)
                        pi.setObjectName(objPropertyStmt.getObject().getName());
                    else
                        pi.setObjectName(objPropertyStmt.getObjectURI());
                    pi.setObjectEntURI(objPropertyStmt.getObjectURI());
                    pi.setPropertyURI(objPropertyStmt.getPropertyURI());
                    if (objPropertyStmt.getProperty() != null) {
                        pi.setDomainPublic( (objPropertyStmt.getProperty().getDomainPublic()!=null) ? objPropertyStmt.getProperty().getDomainPublic() : objPropertyStmt.getProperty().getLocalName() );
                    } else {
                        pi.setDomainPublic(objPropertyStmt.getPropertyURI());
                    }
                    existingPropertyInstances.add(pi);
                }
            }
        }
        return existingPropertyInstances;
    }

    public PropertyInstance getProperty(String subjectURI, String predicateURI, String objectURI) {
        PropertyInstance pi = new PropertyInstance();
        pi.setSubjectEntURI(subjectURI);
        Individual sub = getWebappDaoFactory().getIndividualDao().getIndividualByURI(subjectURI);
        pi.setSubjectName(sub.getName());
        pi.setDomainClassURI( sub.getVClassURI());

        pi.setPropertyURI(predicateURI);
        pi.setPropertyName(getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURI(predicateURI).getDomainPublic());;

        pi.setObjectEntURI(objectURI);
        Individual obj = getWebappDaoFactory().getIndividualDao().getIndividualByURI(objectURI);
        pi.setObjectName(obj.getName());
        pi.setRangeClassURI(obj.getVClassURI());
        return pi;
    }

    public int insertProp(PropertyInstanceIface prop) {
    	return insertProp(prop, getOntModelSelector());
    }

    public int insertProp(PropertyInstanceIface prop, OntModelSelector oms) {
        OntModel ontModel = oms.getABoxModel();
        OntModel tboxModel = oms.getTBoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            tboxModel.enterCriticalSection(Lock.READ);
            try {
                Resource subjRes = ontModel.getResource(prop.getSubjectEntURI());
                OntProperty pred = tboxModel.getOntProperty(prop.getPropertyURI());            
                Resource objRes = ontModel.getResource(prop.getObjectEntURI());
                if ( (subjRes != null) && (pred != null) && (objRes != null) ) {
                	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,prop.getSubjectEntURI()));
                	try {
                		ontModel.add(subjRes,pred,objRes);
                	} finally {
                		getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,prop.getSubjectEntURI()));
                	}
                	OntProperty invPred = pred.getInverse();
                    if (invPred != null) {
                    	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,prop.getObjectEntURI()));
                        try {
                        	ontModel.add(objRes,invPred,subjRes);
                        } finally {
                        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,prop.getSubjectEntURI()));
                        }
                    }
                }
                return 0;
            } finally {
                tboxModel.leaveCriticalSection();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void insertPropertyInstance(PropertyInstance prop) {
        this.insertProp(prop);
    }

    public void deletePropertyInstance(PropertyInstance prop) {
        this.deleteObjectPropertyStatement(prop.getSubjectEntURI(), prop.getPropertyURI(), prop.getObjectEntURI());
    }

}
