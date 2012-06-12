/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ClassEditConfig;

public interface ClassEditConfigDao {
    ClassEditConfig getByURI(String URI);

    void insertNewConfig(ClassEditConfig cdc) throws InsertException;
    void updateConfig(ClassEditConfig cdc);

    void deleteConfig(String URI);
    void deleteConfig(ClassEditConfig cdc);     
}
