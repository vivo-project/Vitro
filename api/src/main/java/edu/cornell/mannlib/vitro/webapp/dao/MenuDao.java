/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MainMenu;

import javax.servlet.ServletRequest;

public interface MenuDao {
    /**
     * @param url - url of request for setting active menu items. This should start with a / and not have the context path.
     * These values will be checked against urlMapping. ex. /people or /home
     */
    public MainMenu getMainMenu( String url);

    /**
     * @param req - the ServletRequest for the current request
     * @param url - url of request for setting active menu items. This should start with a / and not have the context path.
     * These values will be checked against urlMapping. ex. /people or /home
     */
    public MainMenu getMainMenu(ServletRequest req, String url);
}
