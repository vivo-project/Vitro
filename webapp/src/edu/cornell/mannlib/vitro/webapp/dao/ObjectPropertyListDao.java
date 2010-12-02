/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public interface ObjectPropertyListDao extends PropertyListDao {

    public List<ObjectProperty> getObjectPropertyList(Individual subject);
    
    public List<ObjectProperty> getObjectPropertyList(String subjectUri); 
}
