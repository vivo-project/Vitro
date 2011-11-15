/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

/**
 * This wraps a WebappDaoFactory and applies filtering.
 *
 *  Objects that can be filtered by this class:
 *
 *  IndividualWebapp
 *  ObjectProperty
 *  Tab
 *  VClassGroup
 *  VClass
 *  User
 *
 *  If getCoreDaoFactory() is called on a WebappDaoFactoryFiltering, the returned
 *  CoreDaoFactory will have the same filtering as the WebappDaoFactoryFiltering.
 *
 *  Notice: A WebappDaoFactoryFiltering with a set of filters will always
 *  filter the same regardless of context.  If you need to filter one
 *  way on an entity page and a different way on a index page, then
 *  you will need WebappDaoFactoryFiltering objects with different sets
 *  of filters.
 *
 * @author bdc34
 */
public class WebappDaoFactoryFiltering implements WebappDaoFactory {

    transient private WebappDaoFactory innerWebappDaoFactory;

    transient private VitroFilters filters;

    transient private DataPropertyDao          filteringDataPropertyDao=null;
    transient private DataPropertyStatementDao filteringDataPropertyStatementDao=null;
    transient private IndividualDao            filteringIndividualDao=null;
    transient private ObjectPropertyDao        filteringObjectPropertyDao=null;
    transient private ObjectPropertyStatementDao filteringObjectPropertyStatementDao=null;
    transient private VClassDao                filteringVClassDao=null;

    transient private UserAccountsDao filteringUserAccountsDao=null;
    transient private VClassGroupDao filteringVClassGroupDao=null;
    transient private PropertyGroupDao filteringPropertyGroupDao=null;
    transient private PropertyInstanceDao filteringPropertyInstanceDao=null;

    public WebappDaoFactoryFiltering( WebappDaoFactory innerDao, VitroFilters filters){                
        if( innerDao == null )
            throw new Error("innerWebappDaoFactory must be non-null");
        this.filters = filters;
        this.innerWebappDaoFactory = innerDao;
    }

    /* ******************* filtering *********************** */

	public Map<String,String> getProperties() {
		return innerWebappDaoFactory.getProperties();
	}

    public String checkURI(String uriStr) {
    	return innerWebappDaoFactory.checkURI(uriStr);
    }
    
    public String checkURI(String uriStr, boolean checkUniqueness) {
    	return innerWebappDaoFactory.checkURI(uriStr, checkUniqueness);
    }
    
    public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
        //TODO: need to clone the filtering factory
        return innerWebappDaoFactory.getUserAwareDaoFactory(userURI);
    }
    
    public String getDefaultNamespace() {
        return innerWebappDaoFactory.getDefaultNamespace();
    }
    
    public Set<String> getNonuserNamespaces() {
    	return innerWebappDaoFactory.getNonuserNamespaces();
    }
    
    public String[] getPreferredLanguages() {
    	return innerWebappDaoFactory.getPreferredLanguages();
    }
    
    public List<String> getCommentsForResource(String resourceURI) {
    	return innerWebappDaoFactory.getCommentsForResource(resourceURI);
    }
    
    public IndividualDao getIndividualDao(){
        if( filteringIndividualDao == null)
            filteringIndividualDao =
                new IndividualDaoFiltering(innerWebappDaoFactory.getIndividualDao(),filters);
        return filteringIndividualDao;
    }

    public UserAccountsDao getUserAccountsDao() {
    	if( filteringUserAccountsDao == null)
    		filteringUserAccountsDao =
    			new UserAccountsDaoFiltering(innerWebappDaoFactory.getUserAccountsDao(),filters);                                     
    	return filteringUserAccountsDao;
    }
    
    public VClassGroupDao getVClassGroupDao() {
        if( filteringVClassGroupDao == null)
            filteringVClassGroupDao =
                new VClassGroupDaoFiltering(innerWebappDaoFactory.getVClassGroupDao(),
                                            this,filters);
                                            
        return filteringVClassGroupDao;
    }
    
    public PropertyGroupDao getPropertyGroupDao() {
        if( filteringPropertyGroupDao == null)
            filteringPropertyGroupDao =
                new PropertyGroupDaoFiltering(innerWebappDaoFactory.getPropertyGroupDao(),
                                            this, filters);
        return filteringPropertyGroupDao;
    }
    

//    public VClassWebappDao getVClassWebappDao() {
//        if( filteringVClassWebappDao == null)
//            filteringVClassWebappDao =
//                new VClassWebappDaoFiltering(innerWebappDaoFactory.getVClassWebappDao(),
//                                             this,
//                                             dataPropertyFilter,objectPropertyFilter,
//                                             dataPropertyStatementFilter,objectPropertyStatementFilter,
//                                             individualFilter, classFilter,
//                                             tabFilter,vClassGroupFilter, userFilter);
//        return filteringVClassWebappDao;
//    }

    /* ********************* non-filtering ******************* */

    public String getUserURI() {
        return innerWebappDaoFactory.getUserURI();
    }

    public ApplicationDao getApplicationDao() {
    	return innerWebappDaoFactory.getApplicationDao();
    }

///////////////////////////////////////////////////////////////////


    /* ******************* non-filtering DAOs *************************** */

    public Classes2ClassesDao getClasses2ClassesDao() {
        return innerWebappDaoFactory.getClasses2ClassesDao();
    }

    public DatatypeDao getDatatypeDao() {
        return innerWebappDaoFactory.getDatatypeDao();
    }

    public OntologyDao getOntologyDao() {
        return innerWebappDaoFactory.getOntologyDao();
    }
  
    /* ******************* filtering DAOs *************************** */

    public PropertyInstanceDao getPropertyInstanceDao() {
        if( filteringPropertyInstanceDao == null ){
            filteringPropertyInstanceDao = 
                new FilteringPropertyInstanceDao(
                        innerWebappDaoFactory.getPropertyInstanceDao(),                        
                        innerWebappDaoFactory.getObjectPropertyDao(),
                        innerWebappDaoFactory.getIndividualDao(),
                        filters);
        }           
        return filteringPropertyInstanceDao;
    }
    
    public DataPropertyDao getDataPropertyDao() {
        if (filteringDataPropertyDao == null ){
            filteringDataPropertyDao =
                new DataPropertyDaoFiltering(innerWebappDaoFactory.getDataPropertyDao(),
                                             filters);
        }
        return filteringDataPropertyDao;
    }


    public DataPropertyStatementDao getDataPropertyStatementDao() {
        if (filteringDataPropertyStatementDao == null ){
            filteringDataPropertyStatementDao = 
                new DataPropertyStatementDaoFiltering(innerWebappDaoFactory.getDataPropertyStatementDao(),
                        filters);
        }
        return filteringDataPropertyStatementDao;
    }


    public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
        if (filteringObjectPropertyStatementDao == null ){
            filteringObjectPropertyStatementDao = 
                new ObjectPropertyStatementDaoFiltering(innerWebappDaoFactory.getObjectPropertyStatementDao(),
                        filters);
        }
        return filteringObjectPropertyStatementDao;
    }


    public ObjectPropertyDao getObjectPropertyDao() {
        if (filteringObjectPropertyDao == null ){
            filteringObjectPropertyDao = new ObjectPropertyDaoFiltering(innerWebappDaoFactory.getObjectPropertyDao(),
                    filters);
        }
        return filteringObjectPropertyDao;
    }


    public VClassDao getVClassDao() {
        if (filteringVClassDao == null ){
            filteringVClassDao = new VClassDaoFiltering(
                    innerWebappDaoFactory.getVClassDao(),
                    innerWebappDaoFactory.getIndividualDao(),
                    filters
            );
        }
        return filteringVClassDao;
    }

    @Override
    public PageDao getPageDao() {
        return innerWebappDaoFactory.getPageDao();
    }

    @Override
    public MenuDao getMenuDao(){
        return innerWebappDaoFactory.getMenuDao();
    }    
    
    @Override
    public DisplayModelDao getDisplayModelDao(){
        return innerWebappDaoFactory.getDisplayModelDao();
    }
    
    @Override 
    public void close() {
        innerWebappDaoFactory.close();
    }
}
