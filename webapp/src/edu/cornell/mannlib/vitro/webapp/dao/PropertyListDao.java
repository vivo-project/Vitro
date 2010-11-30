/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

public interface PropertyListDao {
    
    public List<Property> getPropertyListForSubject(Individual subject);
    
    public List<Property> getPropertyListForSubject(String subjectUri); 
}
