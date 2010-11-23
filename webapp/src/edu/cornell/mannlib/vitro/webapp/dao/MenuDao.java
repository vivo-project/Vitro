/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.Menu;

public interface MenuDao {
    public Menu getMenu(String uri); 
}
