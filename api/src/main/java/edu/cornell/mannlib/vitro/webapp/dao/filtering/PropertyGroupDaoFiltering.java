/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Filter;

import com.hp.hpl.jena.ontology.DatatypeProperty;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class PropertyGroupDaoFiltering implements PropertyGroupDao {

    final PropertyGroupDao innerDao;
    final WebappDaoFactoryFiltering filteredDaos;
    final VitroFilters filters;

	public PropertyGroupDaoFiltering(PropertyGroupDao propertyGroupDao,
            WebappDaoFactoryFiltering webappDaoFactoryFiltering,
            VitroFilters filters) {	    
	    this.innerDao = propertyGroupDao;
        this.filteredDaos = webappDaoFactoryFiltering;
        this.filters = filters;	    
    }

    public void deletePropertyGroup(PropertyGroup group) {
		innerDao.deletePropertyGroup(group);
	}

	
    public PropertyGroup getGroupByURI(String uri) {
        PropertyGroup grp  = innerDao.getGroupByURI(uri);
         wrapPropertyGroup( grp );
         return grp;
    }
   
	private void  wrapPropertyGroup( PropertyGroup grp ){
	    if( grp == null ) return ;        
        List<Property> props =  grp.getPropertyList();
        if( props == null ||  props.size() == 0 ) 
            return ;
        
        List<Property> filteredProps = new LinkedList<Property>();
        for( Property prop : props ){
    	    if( prop != null ){
	           	if( prop instanceof ObjectProperty ){
                	if( filters.getObjectPropertyFilter().fn( (ObjectProperty)prop ) ){
        	           	filteredProps.add( new ObjectPropertyFiltering((ObjectProperty)prop,filters));
            	    }
	            }else if( prop instanceof ObjectPropertyFiltering ){                
                	//log.debug("property instanceof ObjectPropertyFiltering == true but property instanceof ObjectProperty == false");
	                if( filters.getObjectPropertyFilter().fn( (ObjectProperty)prop ) ){
    	                filteredProps.add( new ObjectPropertyFiltering((ObjectProperty)prop,filters));
        	        }
            	}else if( prop instanceof DatatypeProperty ){
                	if( filters.getDataPropertyFilter().fn((DataProperty)prop)){
                    	filteredProps.add( prop );
	                }
    	        }
            }
        }
        
        grp.setPropertyList(filteredProps); //side effect 
        return ;	    
	}
	
	public List<PropertyGroup> getPublicGroups(boolean withProperties) {
		List<PropertyGroup> groups =  innerDao.getPublicGroups(withProperties);
		for( PropertyGroup grp : groups ){
		    wrapPropertyGroup(grp);
		}
		return groups;
	}
	
	public PropertyGroup createDummyPropertyGroup(String name, int rank) {
	    return innerDao.createDummyPropertyGroup(name, rank);
	}
	
	public String insertNewPropertyGroup(PropertyGroup group) {
		return innerDao.insertNewPropertyGroup(group);
	}

	
	public int removeUnpopulatedGroups(List<PropertyGroup> groups) {
		return innerDao.removeUnpopulatedGroups(groups);
	}

	public void updatePropertyGroup(PropertyGroup group) {
		innerDao.updatePropertyGroup(group);
	}

}
