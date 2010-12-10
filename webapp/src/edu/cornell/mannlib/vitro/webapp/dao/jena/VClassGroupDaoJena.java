/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.web.DisplayVocabulary;

public class VClassGroupDaoJena extends JenaBaseDao implements VClassGroupDao {

    private static final Log log = LogFactory.getLog(TabDaoJena.class.getName());

    public VClassGroupDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }
    
    public void deleteVClassGroup(VClassGroup vcg) {
    	deleteVClassGroup(vcg,getOntModel());
    }

    public void deleteVClassGroup(VClassGroup vcg, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Individual groupInd = ontModel.getIndividual(vcg.getURI());
            if (groupInd != null) {
                groupInd.remove();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public LinkedHashMap<String, VClassGroup> getClassGroupMap() {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            LinkedHashMap<String, VClassGroup> map = new LinkedHashMap<String, VClassGroup>();
            List<VClassGroup> groups = new ArrayList<VClassGroup>();
            ClosableIterator<Individual> groupIt = getOntModel().listIndividuals(CLASSGROUP);
            try {
                while (groupIt.hasNext()) {
                    Individual groupInd = (Individual) groupIt.next();
                    VClassGroup group = groupFromGroupIndividual(groupInd);
                    if (group!=null) {
                        groups.add(group);
                    }
                }
            } finally {
                groupIt.close();
            }
            Collections.sort(groups);
            Iterator<VClassGroup> groupsIt = groups.iterator();
            while (groupsIt.hasNext()) {
                VClassGroup group = (VClassGroup) groupsIt.next();
                map.put(group.getPublicName(), group);
            }
            return map;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public VClassGroup getGroupByURI(String uri) {
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


    public List<VClassGroup> getPublicGroupsWithVClasses() {
        return getPublicGroupsWithVClasses(false);
    }

    public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder) {
        return getPublicGroupsWithVClasses(displayOrder, true);
    }

    public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses) {
        return getPublicGroupsWithVClasses(displayOrder, includeUninstantiatedClasses, false);
    }

    public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses,
            boolean getIndividualCount) {
        VClassDao classDao = getWebappDaoFactory().getVClassDao();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List<VClassGroup> groups = new ArrayList<VClassGroup>();
            ClosableIterator<Individual> groupIt = getOntModel().listIndividuals(CLASSGROUP);
            try {
                while (groupIt.hasNext()) {
                    Individual grp = (Individual) groupIt.next();
                    VClassGroup vgrp = groupFromGroupIndividual(grp);
                    if (vgrp!=null) {
                        classDao.addVClassesToGroup(vgrp, includeUninstantiatedClasses, getIndividualCount);
                        groups.add(vgrp);
                        java.util.Collections.sort(groups);
                    }
                }    
            } finally {
                groupIt.close();
            }
            // BJL23 2008-12-18
            // It's often problematic that classes don't show up in editing picklists until they're in a classgroup.
            // I'm going to try adding all other classes to a classgroup called "ungrouped"
            // We really need to rework these methods and move the filtering behavior into the nice filtering framework
            /* commenting this out until I rework the filtering DAO to use this method */
            /*
            List<VClass> ungroupedClasses = new ArrayList<VClass>();
            List<VClass> allClassList = getWebappDaoFactory().getVClassDao().getAllVclasses();
            Iterator<VClass> allClassIt = allClassList.iterator();
            while (allClassIt.hasNext()) {
            	VClass cls = allClassIt.next();
            	if (cls.getGroupURI()==null) {
            		ungroupedClasses.add(cls);
            	}
            }
            if (ungroupedClasses.size()>0) {
            	VClassGroup ungrouped = new VClassGroup();
            	ungrouped.setPublicName("ungrouped");
            	groups.add(ungrouped);
            }
            */
            if (groups.size()>0) {
                return groups;
            } else {
                classDao.addVClassesToGroups(groups);
                return groups;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }

    }

    public VClassGroup groupFromGroupIndividual(Individual groupInd) {
        if (groupInd==null) {
            return null;
        }
        VClassGroup group = new VClassGroup();
        group.setPublicName(groupInd.getLabel(null));
        group.setURI(groupInd.getURI());
        group.setNamespace(groupInd.getNameSpace());
        group.setLocalName(groupInd.getLocalName());
        try {
            group.setDisplayRank(Integer.decode(((Literal)(groupInd.getProperty(getOntModel().getDatatypeProperty(VitroVocabulary.DISPLAY_RANK)).getObject())).getString()).intValue());
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return group;
    }

    public int insertNewVClassGroup(VClassGroup vcg) {
    	insertNewVClassGroup(vcg,getOntModelSelector().getApplicationMetadataModel());
        return 0;
    }

    public int insertNewVClassGroup(VClassGroup vcg, OntModel ontModel) {
    	
    	// VitroClassGroups should really inherit from Individual objects now,
    	// but they don't (yet).
    	// I'm going to make an Individual so I can avoid duplicating URI code.
    	
    	edu.cornell.mannlib.vitro.webapp.beans.Individual groupInd = 
    		new IndividualImpl(); // We should make a factory for these
    	groupInd.setURI(vcg.getURI());
    	groupInd.setNamespace(DEFAULT_NAMESPACE+"vitroClassGroup");
    	groupInd.setName(vcg.getPublicName());
    	groupInd.setVClassURI(CLASSGROUP.getURI());
    	
    	String groupURI = null;
    	try {
    		groupURI = (new WebappDaoFactoryJena(ontModel)).getIndividualDao().insertNewIndividual(groupInd);
    	} catch (InsertException ie) {
    		throw new RuntimeException(InsertException.class.getName() + "Unable to insert class group "+groupURI, ie);
    	}
    	
    	if (groupURI != null) {
	        ontModel.enterCriticalSection(Lock.WRITE);
	        try {
	        	Individual groupJenaInd = ontModel.getIndividual(groupURI);
	            try {
	                groupJenaInd.addProperty(DISPLAY_RANK, Integer.toString(vcg.getDisplayRank()), XSDDatatype.XSDint);
	            } catch (Exception e) {
	                log.error("error setting displayRank for "+groupInd.getURI());
	            }
	        } finally {
	            ontModel.leaveCriticalSection();
	        }
	        return 0;
    	} else {
    		log.error("Unable to insert class group " + vcg.getPublicName());
    		return 1;
    	}
        
    }

    public int removeUnpopulatedGroups(List<VClassGroup> groups) {
        if (groups==null || groups.size()==0)
            return 0;
        int removedGroupsCount = 0;
        ListIterator<VClassGroup> it = groups.listIterator();
        while(it.hasNext()){
            VClassGroup group = (VClassGroup) it.next();
            List<VClass> classes = group.getVitroClassList();
            if( classes == null || classes.size() < 1 ){
                removedGroupsCount++;
                it.remove();
            }
        }
        return removedGroupsCount;
    }

    public void sortGroupList(List<VClassGroup> groupList) {
        Collections.sort(groupList, new Comparator<VClassGroup>() {
            public int compare(VClassGroup first, VClassGroup second) {
                if (first!=null) {
                    if (second!=null) {
                        return (first.getDisplayRank()-second.getDisplayRank());
                    } else {
                        log.error("error--2nd VClassGroup is null in VClassGroupDao.getGroupList().compare()");
                    }
                } else {
                    log.error("error--1st VClassGroup is null in VClassGroupDao.getGroupList().compare()");
                }
                return 0;
            }
        });
    }

    public void updateVClassGroup(VClassGroup vcg) {
    	updateVClassGroup(vcg,getOntModelSelector().getApplicationMetadataModel());
    }

    public void updateVClassGroup(VClassGroup vcg, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Individual groupInd = ontModel.getIndividual(vcg.getURI());
            try {
                groupInd.setLabel(vcg.getPublicName(), (String) getDefaultLanguage());
            } catch (Exception e) {
                log.error("error updating name for "+groupInd.getURI());
            }
            try {
                groupInd.removeAll(DISPLAY_RANK);
                groupInd.addProperty(DISPLAY_RANK, Integer.toString(vcg.getDisplayRank()), XSDDatatype.XSDint);
            } catch (Exception e) {
                log.error("error updating display rank for "+groupInd.getURI());
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    @Override
    public void removeClassesHiddenFromSearch(List<VClassGroup> groups) {        
        OntModel displayOntModel = getOntModelSelector().getDisplayModel();
        ProhibitedFromSearch pfs = new ProhibitedFromSearch(
                DisplayVocabulary.PRIMARY_LUCENE_INDEX_URI, displayOntModel);
        for (VClassGroup group : groups) {
            List<VClass> classList = new ArrayList<VClass>();
            for (VClass vclass : group.getVitroClassList()) {
                if (!pfs.isClassProhibited(vclass.getURI())) {
                    classList.add(vclass);
                }
            }
            group.setVitroClassList(classList);
        }        
    }

    @Override
    public VClassGroup getGroupByName(String vcgName) {
        if( vcgName == null )
            return null;
        else{
            return getClassGroupMap().get(vcgName);
        }
    }

}
