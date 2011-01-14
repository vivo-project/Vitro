/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.adaptor.AndUnary;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

class ObjectPropertyDaoFiltering extends BaseFiltering implements ObjectPropertyDao{
    final ObjectPropertyDao innerObjectPropertyDao;    
    final VitroFilters filters;   

    public ObjectPropertyDaoFiltering(ObjectPropertyDao objectPropertyDao,
            VitroFilters filters) {
        super();
        this.innerObjectPropertyDao = objectPropertyDao;
        this.filters = filters;              
    }  
    
    /* filtered methods */
    public List getAllObjectProperties() {
        return filterAndWrap(innerObjectPropertyDao.getAllObjectProperties(), filters);        
    }

    public List getObjectPropertiesForObjectPropertyStatements(List objectPropertyStatements) {
        //assume that the objPropStmts are already filtered
        List<ObjectProperty> list =
            innerObjectPropertyDao
                .getObjectPropertiesForObjectPropertyStatements(objectPropertyStatements);
       return filterAndWrap(list, filters);
    }


    public ObjectProperty getObjectPropertyByURI(String objectPropertyURI) {
        ObjectProperty newOprop=innerObjectPropertyDao.getObjectPropertyByURI(objectPropertyURI);
        return (newOprop == null) ? null : new ObjectPropertyFiltering(newOprop, filters);
    }

    public List<ObjectPropertyStatement> getStatementsUsingObjectProperty(ObjectProperty op) {
        return ObjectPropertyStatementDaoFiltering.filterAndWrapList(innerObjectPropertyDao.getStatementsUsingObjectProperty(op),filters);       
    }
    
    public List getRootObjectProperties() {
        return filterAndWrap(innerObjectPropertyDao.getRootObjectProperties(),filters);
    }
 
    
    /* other methods */
    public void deleteObjectProperty(String objectPropertyURI) {
        innerObjectPropertyDao.deleteObjectProperty(objectPropertyURI);
    }


    public void deleteObjectProperty(ObjectProperty objectProperty) {
        innerObjectPropertyDao.deleteObjectProperty(objectProperty);
    }


    public void fillObjectPropertiesForIndividual(Individual individual) {
        innerObjectPropertyDao.fillObjectPropertiesForIndividual(individual);
    }

    public int insertObjectProperty(ObjectProperty objectProperty) throws InsertException {
        return innerObjectPropertyDao.insertObjectProperty(objectProperty);
    }

    public void updateObjectProperty(ObjectProperty objectProperty) {
        innerObjectPropertyDao.updateObjectProperty(objectProperty);
    }

    public void addSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	innerObjectPropertyDao.addSuperproperty(property, superproperty);
    }
    
    public void addSuperproperty(String propertyURI, String superpropertyURI) {
    	innerObjectPropertyDao.addSuperproperty(propertyURI, superpropertyURI);
    }
    
    public void removeSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	innerObjectPropertyDao.removeSuperproperty(property, superproperty);
    }
    
    public void removeSuperproperty(String propertyURI, String superpropertyURI) {
    	innerObjectPropertyDao.removeSuperproperty(propertyURI, superpropertyURI);
    }
    
    public void addSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	innerObjectPropertyDao.addSubproperty(property, subproperty);
    }
    
    public void addSubproperty(String propertyURI, String subpropertyURI) {
    	innerObjectPropertyDao.addSubproperty(propertyURI, subpropertyURI);
    }
    
    public void removeSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	innerObjectPropertyDao.removeSubproperty(property, subproperty);
    }
    
    public void removeSubproperty(String propertyURI, String subpropertyURI) {
    	innerObjectPropertyDao.removeSubproperty(propertyURI, subpropertyURI);
    }
    
    public List <String> getSubPropertyURIs(String propertyURI) {
    	return innerObjectPropertyDao.getSubPropertyURIs(propertyURI);
    }

    public List <String> getAllSubPropertyURIs(String propertyURI) {
    	return innerObjectPropertyDao.getAllSubPropertyURIs(propertyURI);
    }

    public List <String> getSuperPropertyURIs(String propertyURI, boolean direct) {
    	return innerObjectPropertyDao.getSuperPropertyURIs(propertyURI, direct);
    }

    public List <String> getAllSuperPropertyURIs(String propertyURI) {
    	return innerObjectPropertyDao.getAllSuperPropertyURIs(propertyURI);
    }    
                                             
    public static List<ObjectProperty> filterAndWrap(List<ObjectProperty> list, VitroFilters filters){
        if( list == null ) return null;
        if( list.size() ==0 ) return list;
        
        List<ObjectProperty> filtered = new LinkedList<ObjectProperty>();         
        Filter.filter(list, 
                new AndUnary<ObjectProperty>(notNull,filters.getObjectPropertyFilter()), 
                filtered);
                                
        List<ObjectProperty> wrapped = new LinkedList<ObjectProperty>();        
        for( ObjectProperty prop : filtered){
            if( prop != null){
                wrapped.add( new ObjectPropertyFiltering(prop, filters));
            }
        }        
        return wrapped;   
    }
    
    private static final UnaryFunctor<ObjectProperty,Boolean> notNull =
        new UnaryFunctor<ObjectProperty,Boolean>(){
            @Override
            public Boolean fn(ObjectProperty arg) {
                return arg != null;
            }
    };

	public void addSubproperty(Property property, Property subproperty) {
		innerObjectPropertyDao.addSubproperty(property, subproperty);	
	}

	public void addSuperproperty(Property property, Property superproperty) {
		innerObjectPropertyDao.addSuperproperty(property, superproperty);
	}

	public void removeSubproperty(Property property, Property subproperty) {
		innerObjectPropertyDao.removeSubproperty(property, subproperty);
	}

	public void removeSuperproperty(Property property, Property superproperty) {
		innerObjectPropertyDao.removeSuperproperty(property, superproperty);
	}

	public void addEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		innerObjectPropertyDao.addEquivalentProperty(propertyURI, equivalentPropertyURI);
	}

	public void addEquivalentProperty(Property property,
			Property equivalentProperty) {
		innerObjectPropertyDao.addEquivalentProperty(property, equivalentProperty);
	}

	public List<String> getEquivalentPropertyURIs(String propertyURI) {
		return innerObjectPropertyDao.getEquivalentPropertyURIs(propertyURI);
	}

	public void removeEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		innerObjectPropertyDao.removeEquivalentProperty(propertyURI, equivalentPropertyURI);
	}

	public void removeEquivalentProperty(Property property,
			Property equivalentProperty) {
		innerObjectPropertyDao.removeEquivalentProperty(property, equivalentProperty);
	}
    
	public boolean skipEditForm(String predicateURI) {
		return innerObjectPropertyDao.skipEditForm(predicateURI);
	}
	
    public List <VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
    	return innerObjectPropertyDao.getClassesWithRestrictionOnProperty(propertyURI);
    }

    @Override
    // This may need to be filtered at some point.
    public List<ObjectProperty> getObjectPropertyList(Individual subject) {
        return innerObjectPropertyDao.getObjectPropertyList(subject);
    }
    
    @Override
    // This may need to be filtered at some point.
    public List<ObjectProperty> getObjectPropertyList(String subjectUri) {
        return innerObjectPropertyDao.getObjectPropertyList(subjectUri);
    }

    @Override
    public String getCustomListViewConfigFileName(ObjectProperty objectProperty) {
        return innerObjectPropertyDao.getCustomListViewConfigFileName(objectProperty);
    }
}
