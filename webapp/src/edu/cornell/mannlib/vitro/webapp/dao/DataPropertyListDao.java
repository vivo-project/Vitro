/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

public interface DataPropertyListDao extends PropertyListDao {

    public List<DataProperty> getDataPropertyList(Individual subject);
    
    public List<DataProperty> getDataPropertyList(String subjectUri);
    
}
