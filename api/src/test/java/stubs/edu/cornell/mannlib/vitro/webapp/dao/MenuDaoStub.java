/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MainMenu;

/**
 * A minimal implementation of the MenuDao.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class MenuDaoStub implements MenuDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private MainMenu mainMenu;

	public void setMainMenu(MainMenu mainMenu) {
		this.mainMenu = mainMenu;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	/**
	 * For this first implementation, we just ignore the "url" parameter and
	 * return whatever MainMenu has been loaded.
	 */
	@Override
	public MainMenu getMainMenu(String url) {
		return this.mainMenu;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

}
