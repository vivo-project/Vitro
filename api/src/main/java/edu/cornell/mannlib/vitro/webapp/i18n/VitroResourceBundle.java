/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.util.ArrayList;
import java.util.List;

/**
 * If you use 3 tier architecture with custom prefix for properties files
 * you can add it with {@link #addAppPrefix(String)}
 * 
 */
public class VitroResourceBundle  {
	
	private static final List<String> appPrefixes = new ArrayList<>();

	static {
		addAppPrefix("vitro");
	}

	public static List<String> getAppPrefixes(){
		return appPrefixes;
	}
	
	public static void addAppPrefix(String prefix) {
		if (!prefix.endsWith("-") && !prefix.endsWith("_")) {
			prefix = prefix + "_";
		}

		if (!appPrefixes.contains(prefix)) {
			appPrefixes.add(prefix);
		}
	}

}
