/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

class FauxPropertyDaoFiltering extends BaseFiltering implements FauxPropertyDao{
    final FauxPropertyDao innerFauxPropertyDao;    
    final VitroFilters filters;   

    public FauxPropertyDaoFiltering(FauxPropertyDao fauxPropertyDao,
            VitroFilters filters) {
        super();
        this.innerFauxPropertyDao = fauxPropertyDao;
        this.filters = filters;              
    }  
    
//    /* filtered methods */
//    public List getAllObjectProperties() {
//        return filterAndWrap(innerFauxPropertyDao.getAllObjectProperties(), filters);        
//    }
//
//    public List getObjectPropertiesForFauxPropertyStatements(List fauxPropertyStatements) {
//        //assume that the objPropStmts are already filtered
//        List<FauxProperty> list =
//            innerFauxPropertyDao
//                .getObjectPropertiesForFauxPropertyStatements(fauxPropertyStatements);
//       return filterAndWrap(list, filters);
//    }
//
//
//    public FauxProperty getFauxPropertyByURI(String fauxPropertyURI) {
//        FauxProperty newOprop=innerFauxPropertyDao.getFauxPropertyByURI(fauxPropertyURI);
//        return (newOprop == null) ? null : new FauxPropertyFiltering(newOprop, filters);
//    }
//    
//    public FauxProperty getFauxPropertyByURIs(String fauxPropertyURI, String domainURI, String rangeURI) {
//        FauxProperty newOprop=innerFauxPropertyDao.getFauxPropertyByURIs(fauxPropertyURI, domainURI, rangeURI);
//        return (newOprop == null) ? null : new FauxPropertyFiltering(newOprop, filters);
//    }
//
//    public FauxProperty getFauxPropertyByURIs(String fauxPropertyURI, String domainURI, String rangeURI, FauxProperty base) {
//        FauxProperty newOprop=innerFauxPropertyDao.getFauxPropertyByURIs(fauxPropertyURI, domainURI, rangeURI, base);
//        return (newOprop == null) ? null : new FauxPropertyFiltering(newOprop, filters);
//    }
//    
//    public List<FauxPropertyStatement> getStatementsUsingFauxProperty(FauxProperty op) {
//        return FauxPropertyStatementDaoFiltering.filterAndWrapList(innerFauxPropertyDao.getStatementsUsingFauxProperty(op),filters);       
//    }
//    
//    public List getRootObjectProperties() {
//        return filterAndWrap(innerFauxPropertyDao.getRootObjectProperties(),filters);
//    }
// 
//    
//    /* other methods */
//    public void deleteFauxProperty(String fauxPropertyURI) {
//        innerFauxPropertyDao.deleteFauxProperty(fauxPropertyURI);
//    }
//
//
//    public void deleteFauxProperty(FauxProperty fauxProperty) {
//        innerFauxPropertyDao.deleteFauxProperty(fauxProperty);
//    }
//
//
//    public void fillObjectPropertiesForIndividual(Individual individual) {
//        innerFauxPropertyDao.fillObjectPropertiesForIndividual(individual);
//    }
//
//    public int insertFauxProperty(FauxProperty fauxProperty) throws InsertException {
//        return innerFauxPropertyDao.insertFauxProperty(fauxProperty);
//    }
//
//    public void updateFauxProperty(FauxProperty fauxProperty) {
//        innerFauxPropertyDao.updateFauxProperty(fauxProperty);
//    }
//
//    public void addSuperproperty(FauxProperty property, FauxProperty superproperty) {
//    	innerFauxPropertyDao.addSuperproperty(property, superproperty);
//    }
//    
//    public void addSuperproperty(String propertyURI, String superpropertyURI) {
//    	innerFauxPropertyDao.addSuperproperty(propertyURI, superpropertyURI);
//    }
//    
//    public void removeSuperproperty(FauxProperty property, FauxProperty superproperty) {
//    	innerFauxPropertyDao.removeSuperproperty(property, superproperty);
//    }
//    
//    public void removeSuperproperty(String propertyURI, String superpropertyURI) {
//    	innerFauxPropertyDao.removeSuperproperty(propertyURI, superpropertyURI);
//    }
//    
//    public void addSubproperty(FauxProperty property, FauxProperty subproperty) {
//    	innerFauxPropertyDao.addSubproperty(property, subproperty);
//    }
//    
//    public void addSubproperty(String propertyURI, String subpropertyURI) {
//    	innerFauxPropertyDao.addSubproperty(propertyURI, subpropertyURI);
//    }
//    
//    public void removeSubproperty(FauxProperty property, FauxProperty subproperty) {
//    	innerFauxPropertyDao.removeSubproperty(property, subproperty);
//    }
//    
//    public void removeSubproperty(String propertyURI, String subpropertyURI) {
//    	innerFauxPropertyDao.removeSubproperty(propertyURI, subpropertyURI);
//    }
//    
//    public List <String> getSubPropertyURIs(String propertyURI) {
//    	return innerFauxPropertyDao.getSubPropertyURIs(propertyURI);
//    }
//
//    public List <String> getAllSubPropertyURIs(String propertyURI) {
//    	return innerFauxPropertyDao.getAllSubPropertyURIs(propertyURI);
//    }
//
//    public List <String> getSuperPropertyURIs(String propertyURI, boolean direct) {
//    	return innerFauxPropertyDao.getSuperPropertyURIs(propertyURI, direct);
//    }
//
//    public List <String> getAllSuperPropertyURIs(String propertyURI) {
//    	return innerFauxPropertyDao.getAllSuperPropertyURIs(propertyURI);
//    }    
//                                             
//    public static List<FauxProperty> filterAndWrap(List<FauxProperty> list, VitroFilters filters){
//        if( list == null ) return null;
//        if( list.size() ==0 ) return list;
//        
//        List<FauxProperty> filtered = new LinkedList<FauxProperty>();         
//        Filter.filter(list, 
//                new AndUnary<FauxProperty>(notNull,filters.getFauxPropertyFilter()), 
//                filtered);
//                                
//        List<FauxProperty> wrapped = new LinkedList<FauxProperty>();        
//        for( FauxProperty prop : filtered){
//            if( prop != null){
//                wrapped.add( new FauxPropertyFiltering(prop, filters));
//            }
//        }        
//        return wrapped;   
//    }
//    
//    private static final UnaryFunctor<FauxProperty,Boolean> notNull =
//        new UnaryFunctor<FauxProperty,Boolean>(){
//            @Override
//            public Boolean fn(FauxProperty arg) {
//                return arg != null;
//            }
//    };
//
//	public void addSubproperty(Property property, Property subproperty) {
//		innerFauxPropertyDao.addSubproperty(property, subproperty);	
//	}
//
//	public void addSuperproperty(Property property, Property superproperty) {
//		innerFauxPropertyDao.addSuperproperty(property, superproperty);
//	}
//
//	public void removeSubproperty(Property property, Property subproperty) {
//		innerFauxPropertyDao.removeSubproperty(property, subproperty);
//	}
//
//	public void removeSuperproperty(Property property, Property superproperty) {
//		innerFauxPropertyDao.removeSuperproperty(property, superproperty);
//	}
//
//	public void addEquivalentProperty(String propertyURI,
//			String equivalentPropertyURI) {
//		innerFauxPropertyDao.addEquivalentProperty(propertyURI, equivalentPropertyURI);
//	}
//
//	public void addEquivalentProperty(Property property,
//			Property equivalentProperty) {
//		innerFauxPropertyDao.addEquivalentProperty(property, equivalentProperty);
//	}
//
//	public List<String> getEquivalentPropertyURIs(String propertyURI) {
//		return innerFauxPropertyDao.getEquivalentPropertyURIs(propertyURI);
//	}
//
//	public void removeEquivalentProperty(String propertyURI,
//			String equivalentPropertyURI) {
//		innerFauxPropertyDao.removeEquivalentProperty(propertyURI, equivalentPropertyURI);
//	}
//
//	public void removeEquivalentProperty(Property property,
//			Property equivalentProperty) {
//		innerFauxPropertyDao.removeEquivalentProperty(property, equivalentProperty);
//	}
//    
//	public boolean skipEditForm(String predicateURI) {
//		return innerFauxPropertyDao.skipEditForm(predicateURI);
//	}
//	
//    public List <VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
//    	return innerFauxPropertyDao.getClassesWithRestrictionOnProperty(propertyURI);
//    }
//
//    @Override
//    // This may need to be filtered at some point.
//    public List<FauxProperty> getFauxPropertyList(Individual subject) {
//        return innerFauxPropertyDao.getFauxPropertyList(subject);
//    }
//    
//    @Override
//    // This may need to be filtered at some point.
//    public List<FauxProperty> getFauxPropertyList(String subjectUri) {
//        return innerFauxPropertyDao.getFauxPropertyList(subjectUri);
//    }
//
//    @Override
//    public String getCustomListViewConfigFileName(FauxProperty fauxProperty) {
//        return innerFauxPropertyDao.getCustomListViewConfigFileName(fauxProperty);
//    }
}
