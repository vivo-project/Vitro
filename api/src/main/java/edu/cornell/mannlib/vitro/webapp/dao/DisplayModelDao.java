/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import javax.servlet.ServletContext;

public interface DisplayModelDao {
    
    /**
     * ServletContext should only be used for getRealPath()
     */
    public void replaceDisplayModel(String n3, ServletContext sc) throws Exception;
    
    /**
     * ServletContext should only be used for getRealPath()
     */
    public String getDisplayModel(ServletContext sc) throws Exception;
}