/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.utils.ThemeUtils;

public class DefaultThemeSetup implements ServletContextListener {
	
	// Set default theme based on themes present on the file system
	public void contextInitialized(ServletContextEvent event) {

	    if (AbortStartup.isStartupAborted(event.getServletContext())) {
            return;
        }
	    
    	// Find the themes directory in the file system
		ServletContext sc = event.getServletContext();		
    	boolean doSort = true;
    	ArrayList<String> themeNames = ThemeUtils.getThemes(sc, doSort);
        
        String defaultTheme;
        if (themeNames.contains("enhanced")) {
        	defaultTheme = "enhanced";
        }
        else if (themeNames.contains("default")) {
        	defaultTheme = "default";
        }
        else {
        	defaultTheme = themeNames.get(0);
        }
        
        String defaultThemeDir = "themes/" + defaultTheme + "/";
        // Define as a static variable of Portal so getThemeDir() method of portal can access it.
        Portal.DEFAULT_THEME_DIR_FROM_CONTEXT = defaultThemeDir;
        
	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}
}
