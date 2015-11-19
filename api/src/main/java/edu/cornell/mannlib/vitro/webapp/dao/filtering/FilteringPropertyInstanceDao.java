/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstanceIface;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class FilteringPropertyInstanceDao implements PropertyInstanceDao {
    private final PropertyInstanceDao innerPropertyInstanceDao;
    private final VitroFilters filters;
    private final UnaryFunctor<PropertyInstance,Boolean> propertyInstanceFilter;
    private final IndividualDao individualDao;
    private final ObjectPropertyDao objectPropDao;
    
    
    public FilteringPropertyInstanceDao(
            final PropertyInstanceDao propertyInstanceDao, 
            final ObjectPropertyDao objectPropDao,
            final IndividualDao individualDao,
            final VitroFilters filters) {
        if( propertyInstanceDao == null ) 
            throw new IllegalArgumentException("Must pass a non null PropertyInstanceDao to constructor");
        if( filters == null )
            throw new IllegalArgumentException("Must pass a non-null VitroFilters to constructor");
        
        this.innerPropertyInstanceDao = propertyInstanceDao;
        this.filters = filters;
        this.individualDao = individualDao;
        this.objectPropDao = objectPropDao;
        
        this.propertyInstanceFilter = new UnaryFunctor<PropertyInstance,Boolean>(){
            @Override
            public Boolean fn(PropertyInstance inst) {
                if( inst == null ) return false;
                
                //this shouldn't happen
                if( inst.getSubjectEntURI()== null && inst.getPropertyURI() == null &&
                        inst.getRangeClassURI() == null )
                    return false;
                
                //in some classes, like PropertyDWR.java, a PropertyInstance with nulls
                //in the subjectUri and objectUri represent an ObjectProperty, not
                //an ObjectPropertyStatement.                
                if( inst.getSubjectEntURI() == null && inst.getObjectEntURI() == null
                        && inst.getPropertyURI() != null ){
                    //is it a property we can show?
                    ObjectProperty op = objectPropDao.getObjectPropertyByURI(inst.getPropertyURI());
                    if( op == null )
                        return false;
                    else
                        return filters.getObjectPropertyFilter().fn(op);                    
                }
                
                
                //Filter based on subject, property and object.  This could be changed
                //to filter on the subject's and object's class.
                Individual sub = individualDao.getIndividualByURI(inst.getSubjectEntURI());
                if( filters.getIndividualFilter().fn(sub) == false )
                    return false;
                Individual obj = individualDao.getIndividualByURI(inst.getObjectEntURI());
                if( filters.getIndividualFilter().fn(obj) == false)
                    return false;
                ObjectProperty prop = objectPropDao.getObjectPropertyByURI(inst.getPropertyURI());
                if( filters.getObjectPropertyFilter().fn(prop) == false)
                    return false;
                else 
                    return true;                                
            }            
        };
    }
     
    /* ******************** filtered methods ********************* */    
    public Collection<PropertyInstance> getAllPossiblePropInstForIndividual(
            String individualURI) {
        Collection<PropertyInstance> innerInst = innerPropertyInstanceDao.getAllPossiblePropInstForIndividual(individualURI);
        Collection<PropertyInstance> out = new LinkedList<PropertyInstance>();
        Filter.filter(innerInst, propertyInstanceFilter , out);                
        return out;        
    }

    public Collection<PropertyInstance> getAllPropInstByVClass(String classURI) {
        Collection<PropertyInstance> innerInst = innerPropertyInstanceDao.getAllPropInstByVClass(classURI);
        Collection<PropertyInstance> out = new LinkedList<PropertyInstance>();
        Filter.filter(innerInst, propertyInstanceFilter , out);                
        return out;
    }

    public Collection<PropertyInstance> getExistingProperties(String entityURI,
            String propertyURI) {
        Collection<PropertyInstance> innerInst = innerPropertyInstanceDao.getExistingProperties(entityURI, propertyURI);
        Collection<PropertyInstance> out = new LinkedList<PropertyInstance>();
        Filter.filter(innerInst, propertyInstanceFilter , out);                
        return out;
    }

    public PropertyInstance getProperty(String subjectURI, String predicateURI,
            String objectURI) {
        PropertyInstance propInst = innerPropertyInstanceDao.getProperty(subjectURI, predicateURI, objectURI);
        if( propertyInstanceFilter.fn(propInst) )
            return propInst;
        else
            return null;
    }

    /* **************** unfiltered methods ***************** */    
    public void deleteObjectPropertyStatement(String subjectURI,
            String propertyURI, String objectURI) {
        innerPropertyInstanceDao.deleteObjectPropertyStatement(subjectURI, propertyURI, objectURI);
    }

    public void deletePropertyInstance(PropertyInstance prop) {
        innerPropertyInstanceDao.deletePropertyInstance(prop);
    }

    public int insertProp(PropertyInstanceIface prop) {
        return innerPropertyInstanceDao.insertProp(prop);
    }

    public void insertPropertyInstance(PropertyInstance prop) {
        innerPropertyInstanceDao.insertPropertyInstance(prop);

    }

    
}
