/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MainMenu;

public interface MenuDao {
    /**
     * @param url - url of request for setting active menu items. This should start with a / and not have the context path.
     * These values will be checked against urlMapping. ex. /people or /home 
     */
    public MainMenu getMainMenu( String url); 
}
