/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.DatatypePropertyDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ObjectPropertyDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.PropertyDisplayConfig;

public interface PropertyDisplayConfigDao {

    ObjectPropertyDisplayConfig getObjectPropertyDisplayConfigByURI(String URI);
    DatatypePropertyDisplayConfig getDataPropertyDisplayConfigByURI(String URI);

    void insertNewPropertyDisplayConfig(PropertyDisplayConfig conf) throws InsertException;
    void updatePropertyDisplayConfig(PropertyDisplayConfig conf);

    void deletePropertyDisplayConfig(String URI);
    void deletePropertyDisplayConfig(PropertyDisplayConfig conf);  
}
