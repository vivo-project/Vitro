/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;
import java.util.Iterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstanceIface;

public interface PropertyInstanceDao {

    void deleteObjectPropertyStatement(String subjectURI, String propertyURI, String objectURI);

    Collection<PropertyInstance> getAllPossiblePropInstForIndividual(String individualURI);
    
    Collection<PropertyInstance> getAllPropInstByVClass(String classURI);

    Collection<PropertyInstance> getExistingProperties(String entityURI, String propertyURI);

    PropertyInstance getProperty(String subjectURI, String predicateURI, String objectURI);

    int insertProp(PropertyInstanceIface prop);

    void insertPropertyInstance(PropertyInstance prop);

    void deletePropertyInstance(PropertyInstance prop);

}