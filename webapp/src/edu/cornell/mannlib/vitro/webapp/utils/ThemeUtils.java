package edu.cornell.mannlib.vitro.webapp.utils;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletContext;

public class ThemeUtils {
	
	public static ArrayList<String> getThemes(ServletContext sc, boolean doSort) {

		// Find the themes directory on the file system
        String themesDirName = sc.getRealPath("/themes");          
        File themesDir = new File(themesDirName);
        
        // Get the children of the themes directory and their names
        File[] children = themesDir.listFiles();
        String[] childNames = themesDir.list();
        
        // Create a list of valid themes
        ArrayList<String> themeNames = new ArrayList<String>(childNames.length);
        for (int i = 0; i < children.length; i++) {
        	// Get only directories, not files
        	if (children[i].isDirectory()) {	
        		themeNames.add(childNames[i]);
        	}
        }
        
        // File.list() does not guarantee a specific order, so sort alphabetically
        if (doSort == true) {
        	Collections.sort(themeNames);
        }
        
        return themeNames;
        
	}

}
