/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstanceIface;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualDeletionEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;
public class PropertyInstanceDaoJena extends JenaBaseDao implements
        PropertyInstanceDao {

    public PropertyInstanceDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    public void deleteObjectPropertyStatement(String subjectURI, String propertyURI, String objectURI) {
    	deleteObjectPropertyStatement(subjectURI, propertyURI, objectURI, getOntModelSelector());
    }

    public void deleteObjectPropertyStatement(String subjectURI, String propertyURI, String objectURI, OntModelSelector ontModelSelector) {
        OntModel ontModel = ontModelSelector.getABoxModel();
        OntModel tboxModel = ontModelSelector.getTBoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource subjRes = ontModel.getResource(subjectURI);
            Property pred = tboxModel.getProperty(propertyURI);
            OntProperty invPred = null;                        
            if (pred.canAs(OntProperty.class)) {
            	invPred = ((OntProperty)pred.as(OntProperty.class)).getInverse();
            }
            Resource objRes = ontModel.getResource(objectURI);
            if ( (subjRes != null) && (pred != null) && (objRes != null) ) {            	
            	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,subjectURI));
            	try {
            		ontModel.remove(subjRes,pred,objRes);
            		
            		updatePropertyDateTimeValue(subjRes,MODTIME,Calendar.getInstance().getTime(),getOntModel());
            	} finally {
            		getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,subjectURI));
            	}
            	try{            		
            		getOntModel().getBaseModel().notifyEvent(new IndividualDeletionEvent(getWebappDaoFactory().getUserURI(),true,objectURI));            		
            		List<Statement> depResStmts = DependentResourceDeleteJena.getDependentResourceDeleteList(ResourceFactory.createStatement(subjRes, pred, objRes),ontModel);
            		getOntModel().remove(depResStmts);
            	} finally {
            		getOntModel().getBaseModel().notifyEvent(new IndividualDeletionEvent(getWebappDaoFactory().getUserURI(),false,objectURI));
            	}
                if (invPred != null) {
                	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,objectURI));
                	try {
                		ontModel.remove(objRes,invPred,subjRes);
                		updatePropertyDateTimeValue(objRes,MODTIME,Calendar.getInstance().getTime(),getOntModel());
                	} finally {
                		getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,subjectURI));
                	}
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public Iterator getAllOfThisTypeIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<PropertyInstance> getAllPossiblePropInstForIndividual(String individualURI) {
    	Individual ind = getWebappDaoFactory().getIndividualDao().getIndividualByURI(individualURI);
    	VClassDao vcDao = getWebappDaoFactory().getVClassDao();
    	
    	List<VClass> allTypes = ind.getVClasses(false); // include indirect types
        
        Set<String> allSuperclassURIs = new HashSet<String>();
       
        for (VClass type : allTypes) {
            String classURI = type.getURI();
            if (classURI != null) {
                allSuperclassURIs.add(type.getURI());
            }
            for (String equivURI : vcDao.getEquivalentClassURIs(classURI)) {
                allSuperclassURIs.add(equivURI);
                allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(equivURI));
            }
            allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(classURI));
        }
        
        List<VClass> vclasses = new ArrayList<VClass>();
        for(String vclassURI : allSuperclassURIs) {
            VClass vclass = vcDao.getVClassByURI(vclassURI);
            if (vclass != null) {
                vclasses.add(vclass);
            }
        }
    	
    	Collection<PropertyInstance> piList = getAllPropInstByVClasses(vclasses);
    	
    	for (PropertyInstance pi : piList) {
    		pi.setDomainClassURI(ind.getVClassURI());
    		// TODO: improve.  This is so the DWR property editing passes the 
    		// individual's VClass to get the right restrictions
    	}

		return piList;
    }
    
    /*
     * sorts VClasses so that subclasses come before superclasses
     */
    private class VClassHierarchyRanker implements Comparator<VClass> {
    	private VClassDao vcDao;
    	public VClassHierarchyRanker(VClassDao vcDao) {
    		this.vcDao = vcDao;
    	}
    	public int compare(VClass vc1, VClass vc2) {
    		if (vcDao.isSubClassOf(vc1, vc2)) {
    			return -1;
    		} else if (vcDao.isSubClassOf(vc2, vc1)) {
    		    return 1;
    		} else {
    		    return 0;
    		}
    	}
    }
    
    
    public Collection<PropertyInstance> getAllPropInstByVClass(String classURI) {
        if (classURI==null || classURI.length()<1) {
            return null;
        }
        
        VClassDao vcDao = getWebappDaoFactory().getVClassDao();
        
        Set<String> allSuperclassURIs = new HashSet<String>();
       
        allSuperclassURIs.add(classURI);
        for (String equivURI : vcDao.getEquivalentClassURIs(classURI)) {
            allSuperclassURIs.add(equivURI);
            allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(equivURI));
        }
        allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(classURI));
        
        List<VClass> vclasses = new ArrayList<VClass>();
        for(String vclassURI : allSuperclassURIs) {
            VClass vclass = vcDao.getVClassByURI(vclassURI);
            if (vclass != null) {
                vclasses.add(vclass);
            }
        }
        return getAllPropInstByVClasses(vclasses);
    }
    
    private void updatePropertyRangeMap(Map<String, Resource[]> map, 
                                        String propURI, 
                                        Resource[] ranges) {
        Resource[] existingRanges = map.get(propURI);
        if (existingRanges == null) {
            map.put(propURI, ranges);
        } else if (existingRanges[0] == null && existingRanges[1] != null) {
            existingRanges[0] = ranges[0];
            map.put(propURI, existingRanges);
        } else if (existingRanges[0] != null && existingRanges[1] == null) {
            existingRanges[1] = ranges[1];
            map.put(propURI, existingRanges);            
        }
    }
    
    public Collection<PropertyInstance> getAllPropInstByVClasses(List<VClass> vclasses) {
        
        List<PropertyInstance> propInsts = new ArrayList<PropertyInstance>();
        
        if(vclasses == null || vclasses.isEmpty()) {
            return propInsts;
        }
        
        Collections.sort(vclasses, new VClassHierarchyRanker(this.getWebappDaoFactory().getVClassDao()));
        
        OntModel ontModel = getOntModelSelector().getTBoxModel();
        
        try {
        
        	ontModel.enterCriticalSection(Lock.READ);
        	
        	// map object property URI to an array of two resources:
        	// the first is the "allValuesFrom" resource and the second is
        	// "someValuesFrom"
        	Map<String, Resource[]> applicableProperties = 
        	        new HashMap<String, Resource[]>();
        	
        	try {
		        for (VClass vclass : vclasses) {
		            if (vclass.isAnonymous()) {
		                continue; 
		            }
		            String VClassURI = vclass.getURI();
		            
		        	OntClass ontClass = getOntClass(ontModel,VClassURI);
		        	if (ontClass != null) {
		        	    List<OntClass> relatedClasses = new ArrayList<OntClass>();
		        	    relatedClasses.addAll(ontClass.listEquivalentClasses().toList());
		        	    relatedClasses.addAll(ontClass.listSuperClasses().toList());
		        	    for (OntClass relatedClass : relatedClasses) {
    		        	    // find properties in restrictions
    		        		if (relatedClass.isRestriction()) {
    		        			// TODO: check if restriction is something like
    		        			// maxCardinality 0 or allValuesFrom owl:Nothing,
    		        			// in which case the property is NOT applicable!
    		        			Restriction rest = (Restriction) relatedClass.as(Restriction.class);
    		        			OntProperty onProperty = rest.getOnProperty();
    		        			if (onProperty != null && onProperty.canAs(ObjectProperty.class)) {
    		        			    Resource[] ranges = new Resource[2];
    		        			    if (rest.isAllValuesFromRestriction()) {
    		        			        ranges[0] = (rest.asAllValuesFromRestriction()).getAllValuesFrom();
    		        			    } else if (rest.isSomeValuesFromRestriction()) {
                                        ranges[1] = (rest.asSomeValuesFromRestriction()).getSomeValuesFrom();
                                    }
    		        				updatePropertyRangeMap(applicableProperties, onProperty.getURI(), ranges);
    		        			}
    		        		}
		        	    }
		        		
		        		// find properties with class in domain
		        		ResIterator pit = ontModel.listSubjectsWithProperty(
		        		        RDFS.domain, ontClass);
		        		while (pit.hasNext()) {
		        		    Resource prop = pit.nextResource();
		        		    if (prop.getNameSpace() != null 
		        		            && !NONUSER_NAMESPACES.contains(
		        		                    prop.getNameSpace()) ) {
		        		        StmtIterator rangeSit = prop.listProperties(
		        		                RDFS.range);
		        		        Resource rangeRes = null;
		        		        while (rangeSit.hasNext()) {    
		        		            Statement s = rangeSit.nextStatement();
		        		            if (s.getObject().isURIResource()) {
		        		                rangeRes = (Resource) s.getObject();
		        		            }
		        		        }
		        		        Resource[] ranges = new Resource[2];
		        		        ranges[0] = rangeRes;
		        		        updatePropertyRangeMap(
		        		                applicableProperties, prop.getURI(), ranges);
		        		        
		        		    }
		        		}
		        		
		        	}
		        }       
        	} catch (Exception e) {
        		log.error("Unable to get applicable properties " +
        		          "by examining property restrictions and domains", e);
        	}
        	
        	// make the PropertyInstance objects
	        for (String propertyURI : applicableProperties.keySet()) {
	        	ObjectProperty op = ontModel.getObjectProperty(propertyURI);
	        	if (op == null) {
	        	    continue;
	        	}
	        	String domainURIStr = getURIStr(op.getDomain());
	        	Resource[] foundRanges = applicableProperties.get(propertyURI);
	        	Resource rangeRes = (foundRanges[0] != null) 
	        	        ? foundRanges[0]
	        	        : (op.getRange() == null && foundRanges[1] != null)
	        	                ? foundRanges[1]
	        	                : op.getRange();
                PropertyInstance pi = new PropertyInstance();
                if (rangeRes != null) {
                	String rangeClassURI;
                	if (rangeRes.isAnon()) {
                		rangeClassURI = PSEUDO_BNODE_NS+rangeRes.getId().toString();
                	} else {
                		rangeClassURI = (String) rangeRes.getURI();
                	}
                    pi.setRangeClassURI(rangeClassURI);
                    try {
                    	pi.setRangeClassName(getWebappDaoFactory().getVClassDao().getVClassByURI(rangeClassURI).getName());
                    	//pi.setRangeClassName(getLabel(getOntModel().getOntResource(rangeClassURI)));
                    } catch (NullPointerException e) {/* probably a union or intersection - need to handle this somehow */}
                } else {
                	pi.setRangeClassURI(OWL.Thing.getURI()); // TODO see above
                }
                pi.setDomainClassURI(domainURIStr);
                try {
                	pi.setDomainClassName(getWebappDaoFactory().getVClassDao().getVClassByURI(domainURIStr).getName());
                	//pi.setDomainClassName(getLabel(getOntModel().getOntResource(op.getDomain().getURI())));
                } catch (NullPointerException e) {/* probably a union or intersection - need to handle this somehow */}
                pi.setSubjectSide(true);
                pi.setPropertyURI(op.getURI());
                pi.setPropertyName(getLabelOrId(op)); // TODO
                pi.setRangePublic(getLabelOrId(op));
                pi.setDomainPublic(getLabelOrId(op));
                propInsts.add(pi);
	        }      
        } finally {
        	ontModel.leaveCriticalSection();
        }
        
        Collections.sort(propInsts, new PropInstSorter());
        return propInsts;
        
    }
    
    private String getURIStr(Resource res) {
    	String URIStr;
    	if (res == null) {
    		URIStr = OWL.Thing.getURI(); // TODO: rdf:Resource if using RDF model; or option to turn off entirely
    	} else {
            if (res.isAnon()) {
            	URIStr = PSEUDO_BNODE_NS+res.getId().toString();
            } else {
            	URIStr = res.getURI();
            }
    	}
    	return URIStr;
    }

    private class PropInstSorter implements Comparator {
        public int compare (Object o1, Object o2) {
            PropertyInstance pi1 = (PropertyInstance) o1;
            PropertyInstance pi2 = (PropertyInstance) o2;
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
                    	updatePropertyDateTimeValue(subjRes,MODTIME,Calendar.getInstance().getTime(),getOntModel());
                	} finally {
                		getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,prop.getSubjectEntURI()));
                	}
                	OntProperty invPred = pred.getInverse();
                    if (invPred != null) {
                    	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,prop.getObjectEntURI()));
                        try {
                        	ontModel.add(objRes,invPred,subjRes);
                        	updatePropertyDateTimeValue(objRes,MODTIME,Calendar.getInstance().getTime(),getOntModel());
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
