/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class PropertyGroupDaoJena extends JenaBaseDao implements PropertyGroupDao {

    private static final Log log = LogFactory.getLog(PropertyGroupDaoJena.class.getName());

    public PropertyGroupDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
	
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }
    
	public void deletePropertyGroup(PropertyGroup group) {
        getOntModel().enterCriticalSection(Lock.WRITE);
        try {
            Individual groupInd = getOntModel().getIndividual(group.getURI());
            if (groupInd != null) {
                groupInd.remove();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        OntModel tboxModel = getOntModelSelector().getTBoxModel();
        tboxModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource groupRes = ResourceFactory.createResource(group.getURI());            
            tboxModel.removeAll(groupRes, null, null);
            tboxModel.removeAll(null, null, groupRes);
        } finally {
            tboxModel.leaveCriticalSection();
        }
	}
	
	public PropertyGroup getGroupByURI(String uri) {
	    if (uri == null) {
	        return null;
	    }
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual group = getOntModel().getIndividual(uri);
            return groupFromGroupIndividual(group);
        } catch (IllegalArgumentException ex) {
            return null;
        } finally {
            getOntModel().leaveCriticalSection();
        }
	}

	
	public List<PropertyGroup> getPublicGroups(boolean withProperties) {
        ObjectPropertyDao opDao = getWebappDaoFactory().getObjectPropertyDao();
        DataPropertyDao dpDao = getWebappDaoFactory().getDataPropertyDao();
        List<PropertyGroup> groups = new ArrayList<PropertyGroup>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator groupIt = getOntModel().listIndividuals(PROPERTYGROUP);
            try {
                while (groupIt.hasNext()) {
                    Individual grp = (Individual) groupIt.next();
                    PropertyGroup pgrp = groupFromGroupIndividual(grp);
                    if (pgrp != null) {
                    	groups.add(pgrp);
                    }
                }
            } finally {
                groupIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        if (withProperties) {
        	Model tboxModel = getOntModelSelector().getTBoxModel();
        	tboxModel.enterCriticalSection(Lock.READ);
        	try {
	        	for (PropertyGroup pgrp : groups) {	
		            List<Property> properties = new ArrayList<Property>();            
		            ClosableIterator closeIt = tboxModel.listStatements(
		            		null, PROPERTY_INPROPERTYGROUPANNOT, tboxModel.getResource(pgrp.getURI()));
		            try {
		            	for (Iterator stmtIt = closeIt; stmtIt.hasNext(); ) {
		            		Statement stmt = (Statement) stmtIt.next();
		            		Resource subjRes = stmt.getSubject();
		            		if (subjRes.canAs(com.hp.hpl.jena.ontology.ObjectProperty.class)) {
		            			properties.add(opDao.getObjectPropertyByURI(subjRes.getURI()));
		            		} else if (subjRes.canAs(com.hp.hpl.jena.ontology.DatatypeProperty.class)) {
		            			properties.add(dpDao.getDataPropertyByURI(subjRes.getURI()));
		            		}
		            	}
		            } finally {
		            	closeIt.close();
		            }
		            pgrp.setPropertyList(properties);
	        	}
        	} finally {
        		tboxModel.leaveCriticalSection();
        	}
        }
        Collections.sort(groups);
        return groups;
	}
	
	public String insertNewPropertyGroup(PropertyGroup group) {
		
	 	// VitroPropertyGroups should really inherit from Individual objects now,
    	// but they don't (yet).
    	// I'm going to make an Individual so I can avoid duplicating URI code.
    	
    	edu.cornell.mannlib.vitro.webapp.beans.Individual groupInd = 
    		new IndividualImpl(); // We should make a factory for these
    	groupInd.setNamespace(DEFAULT_NAMESPACE+"vitroPropertyGroup");
    	groupInd.setName(group.getName());
    	groupInd.setVClassURI(PROPERTYGROUP.getURI());
    	groupInd.setURI(group.getURI());
    	
    	String groupURI = null;
    	
        OntModel unionForURIGeneration = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM, ModelFactory.createUnion(
                        getOntModelSelector().getApplicationMetadataModel(), 
                        getOntModelSelector().getFullModel()));
        
        WebappDaoFactory wadfForURIGeneration = null;
        try {
            wadfForURIGeneration = new WebappDaoFactoryJena(
                    unionForURIGeneration);
            groupURI = wadfForURIGeneration
                    .getIndividualDao().insertNewIndividual(groupInd);
    	} catch (InsertException ie) {
    		throw new RuntimeException(InsertException.class.getName() + 
    		        "Unable to insert property group " + groupURI, ie);
    	} finally {
    	    wadfForURIGeneration.close();
    	}
    	
    	if (groupURI != null) {
	        getOntModel().enterCriticalSection(Lock.WRITE);
	        try {
	        	com.hp.hpl.jena.ontology.Individual groupJenaInd = 
	        	        getOntModel().getIndividual(groupURI);
	            try {
	                groupJenaInd.addProperty(DISPLAY_RANK, Integer.toString(
	                        group.getDisplayRank()), XSDDatatype.XSDint);
	            } catch (Exception e) {
	                log.error(
	                        "error setting displayRank for " 
	                                + groupInd.getURI());
	            }
	            if (group.getPublicDescription() != null 
	                    && group.getPublicDescription().length()>0) {
		            try {   
		                groupJenaInd.addProperty(
		                        PUBLIC_DESCRIPTION_ANNOT, 
		                        group.getPublicDescription(), 
		                        XSDDatatype.XSDstring);
		            } catch (Exception ex) {
		                log.error(
		                        "error setting public description for "
		                                + groupInd.getURI());
		            }
		        }
	        } finally {
	            getOntModel().leaveCriticalSection();
	        }
    	} else {
    		log.error("Unable to insert property group " + group.getName());
    	}
    
	    return groupURI;
	}
	
	public int removeUnpopulatedGroups(List<PropertyGroup> groups) {
        if (groups==null || groups.size()==0)
            return 0;
        int removedGroupsCount = 0;
        ListIterator<PropertyGroup> it = groups.listIterator();
        while(it.hasNext()){
            PropertyGroup group = it.next();
            List properties = group.getPropertyList();
            if( properties == null || properties.size() < 1 ){
                removedGroupsCount++;
                it.remove();
            }
        }
        return removedGroupsCount;
	}

	public void updatePropertyGroup(PropertyGroup group) {
		OntModel ontModel = getOntModelSelector().getApplicationMetadataModel();
	    ontModel.enterCriticalSection(Lock.WRITE);
	    try {
	        Individual groupInd = ontModel.getIndividual(group.getURI());
	        try {
	            groupInd.setLabel(group.getName(), (String) getDefaultLanguage());
	        } catch (Exception e) {
	            log.error("error updating name for "+groupInd.getURI());
	        }
	        try {
	            groupInd.removeAll(PUBLIC_DESCRIPTION_ANNOT);
	            if (group.getPublicDescription()!=null && group.getPublicDescription().length()>0) {
	                groupInd.addProperty(PUBLIC_DESCRIPTION_ANNOT, group.getPublicDescription(), XSDDatatype.XSDstring);
	            }
	        } catch (Exception e) {
	            log.error("Error updating public description for "+groupInd.getURI());
	        }
	        try {
	            groupInd.removeAll(DISPLAY_RANK);
	            groupInd.addProperty(DISPLAY_RANK, Integer.toString(group.getDisplayRank()), XSDDatatype.XSDint);
	        } catch (Exception e) {
	            log.error("error updating display rank for "+groupInd.getURI());
	        }
	    } finally {
	        ontModel.leaveCriticalSection();
	    }
	}
	
	public PropertyGroup createDummyPropertyGroup(String name, int rank) {
	    PropertyGroup newGroup = new PropertyGroup();
	    newGroup.setName(name);
	    newGroup.setDisplayRank(rank);
	    newGroup.setURI(DEFAULT_NAMESPACE+"vitroPropertyGroup"+"name");
	    List<Property> propsList = new ArrayList<Property>();
	    newGroup.setPropertyList(propsList);
	    return newGroup;
	}

    private PropertyGroup groupFromGroupIndividual(Individual groupInd) {
        if (groupInd==null) {
            return null;
        }
        PropertyGroup group = new PropertyGroup();
        group.setName(getLabelOrId(groupInd));
        group.setURI(groupInd.getURI());
        group.setNamespace(groupInd.getNameSpace());
        group.setLocalName(groupInd.getLocalName());
        group.setPublicDescription(getPropertyStringValue(
                groupInd, PUBLIC_DESCRIPTION_ANNOT));
        group.setDisplayRank(getPropertyNonNegativeIntValue(groupInd, DISPLAY_RANK));    
        return group;
    }
	
}
