/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class PropertyList extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyList.class);

    private List<Property> propertyList;
    
    PropertyList() {
        propertyList = new ArrayList<Property>();
    }

    protected void addObjectProperties(List<ObjectProperty> propertyList) {
        for (ObjectProperty op : propertyList) {           
            add(op); 
        }
    }
    
    protected void add(ObjectProperty op) {
        propertyList.add(op);                   
    }
    
    protected void addDataProperties(List<DataProperty> propertyList) {
        for (DataProperty dp : propertyList) {       
            add(dp); 
        }
    }
    
    protected void add(DataProperty p) {
        propertyList.add(p);
    }
    
    protected boolean contains(Property property) {
        String propertyUri = property.getURI();
        if (propertyUri == null) {
            log.error("Property has no propertyURI in alreadyOnPropertyList()");
            return true; // don't add to list
        }
        for (Property p : propertyList) {
            if (propertyUri.equals(p.getURI())) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean contains(List<ObjectProperty> list, PropertyInstance pi) {
        if (pi.getPropertyURI() == null) {
            return false;
        }
        for (ObjectProperty op : list) {
            if (op.getURI() != null && op.getURI().equals(pi.getPropertyURI())) {
                return op.isSubjectSide() == pi.getSubjectSide();
            }
        }
        return false;
    }  
    
    protected List<Property> getProperties() {
        return propertyList;
    }
    
    protected void mergeAllPossibleObjectProperties(WebappDaoFactory wdf, Individual subject, List<ObjectProperty> objectPropertyList) {
        PropertyInstanceDao piDao = wdf.getPropertyInstanceDao();
        // RY *** Does this exclude properties in the excluded namespaces already? If not, need same test as above
        Collection<PropertyInstance> allPropInstColl = piDao.getAllPossiblePropInstForIndividual(subject.getURI());
        if (allPropInstColl != null) {
            for (PropertyInstance pi : allPropInstColl) {
                if (pi != null) {
                    // RY Do we need to check this before checking if it's on the property list??
                    if (! contains(objectPropertyList, pi)) {
                        ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
                        ObjectProperty op = opDao.getObjectPropertyByURI(pi.getPropertyURI());
                        if (op == null) {
                            log.error("ObjectProperty op returned null from opDao.getObjectPropertyByURI()");
                        } else if (op.getURI() == null) {
                            log.error("ObjectProperty op returned with null propertyURI from opDao.getObjectPropertyByURI()");
                        } else  if (! contains(op)) {
                            add(op);
                        }
                    }
                } else {
                    log.error("a property instance in the Collection created by PropertyInstanceDao.getAllPossiblePropInstForIndividual() is unexpectedly null");
                }
            }
        } else {
            log.error("a null Collection is returned from PropertyInstanceDao.getAllPossiblePropInstForIndividual()");
        }                    
    }
    
    protected void mergeAllPossibleDataProperties(WebappDaoFactory wdf, Individual subject) {
        DataPropertyDao dpDao = wdf.getDataPropertyDao();
        // RY *** Does this exclude properties in the excluded namespaces already? If not, need same test as above
        Collection <DataProperty> allDatapropColl = dpDao.getAllPossibleDatapropsForIndividual(subject.getURI());
        if (allDatapropColl != null) {
            for (DataProperty dp : allDatapropColl ) {
                if (dp!=null) {
                    if (dp.getURI() == null) {
                        log.error("DataProperty dp returned with null propertyURI from dpDao.getAllPossibleDatapropsForIndividual()");
                    } else if (! contains(dp)) {
                        add(dp);
                    }
                } else {
                    log.error("a data property in the Collection created in DataPropertyDao.getAllPossibleDatapropsForIndividual() is unexpectedly null)");
                }
            }
        } else {
            log.error("a null Collection is returned from DataPropertyDao.getAllPossibleDatapropsForIndividual())");
        }        
    }

    @SuppressWarnings("unchecked")
    protected void sort(VitroRequest vreq) {            
        try {
            Collections.sort(propertyList, new PropertyRanker(vreq));
        } catch (Exception ex) {
            log.error("Exception sorting merged property list: " + ex.getMessage());
        }
    }
    
    private class PropertyRanker implements Comparator {

        WebappDaoFactory wdf;
        PropertyGroupDao pgDao;

        private PropertyRanker(VitroRequest vreq) {
            this.wdf = vreq.getWebappDaoFactory();
            this.pgDao = wdf.getPropertyGroupDao();
        }
        
        public int compare (Object o1, Object o2) {
            Property p1 = (Property) o1;
            Property p2 = (Property) o2;
            
            // sort first by property group rank; if the same, then sort by property rank
            final int MAX_GROUP_RANK=99;
            
            int p1GroupRank=MAX_GROUP_RANK;
            try {
                if (p1.getGroupURI()!=null) {
                    PropertyGroup pg1 = pgDao.getGroupByURI(p1.getGroupURI());
                    if (pg1!=null) {
                        p1GroupRank=pg1.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p1GroupRank for group "+p1.getEditLabel());
            }
            
            int p2GroupRank=MAX_GROUP_RANK;
            try {
                if (p2.getGroupURI()!=null) {
                    PropertyGroup pg2 = pgDao.getGroupByURI(p2.getGroupURI());
                    if (pg2!=null) {
                        p2GroupRank=pg2.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p2GroupRank for group "+p2.getEditLabel());
            }
            
            // int diff = pgDao.getGroupByURI(p1.getGroupURI()).getDisplayRank() - pgDao.getGroupByURI(p2.getGroupURI()).getDisplayRank();
            int diff=p1GroupRank - p2GroupRank;
            if (diff==0) {
                diff = determineDisplayRank(p1) - determineDisplayRank(p2);
                if (diff==0) {
                    return p1.getEditLabel().compareTo(p2.getEditLabel());
                } else {
                    return diff;
                }
            }
            return diff;
        }
        
        private int determineDisplayRank(Property p) {
            if (p instanceof DataProperty) {
                DataProperty dp = (DataProperty)p;
                return dp.getDisplayTier();
            } else if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty)p;
                String tierStr = op.getDomainDisplayTier(); // no longer used: p.isSubjectSide() ? op.getDomainDisplayTier() : op.getRangeDisplayTier();
                try {
                    return Integer.parseInt(tierStr);
                } catch (NumberFormatException ex) {
                    log.error("Cannot decode object property display tier value "+tierStr+" as an integer");
                }
            } else {
                log.error("Property is of unknown class in PropertyRanker()");  
            }
            return 0;
        }
    }
}
