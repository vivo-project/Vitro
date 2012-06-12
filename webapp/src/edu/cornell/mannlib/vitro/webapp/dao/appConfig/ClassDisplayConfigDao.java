/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ClassDisplayConfig;

public interface ClassDisplayConfigDao {            
    ClassDisplayConfig getClassDisplayConfigByURI(String URI);

    void insertNewClassDisplayConfig(ClassDisplayConfig cdc) throws InsertException;
    void updateClassDisplayConfig(ClassDisplayConfig cdc);

    void deleteClassDisplayConfig(String URI);
    void deleteClassDisplayConfig(ClassDisplayConfig cdc);        
}
