/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;


public class EditConfigurationConstants {
    /** Constants used by edit configuration */
	//forces creation of new uri if present
    public static final String NEW_URI_SENTINEL = ">NEW URI REQUIRED<";
    public static final String BLANK_SENTINEL = ">SUBMITTED VALUE WAS BLANK<";

    //For freemarker configuration
    public static Map<String, String> exportConstants() {
    	Map<String, String> constants = new HashMap<String, String>();
    	java.lang.reflect.Field[] fields = EditConfigurationConstants.class.getDeclaredFields();
    	for(java.lang.reflect.Field f: fields) {
    		if(Modifier.isStatic(f.getModifiers()) && 
    			Modifier.isPublic(f.getModifiers())) {
    			try {
    				constants.put(f.getName(), f.get(null).toString());
    			} catch(Exception ex) {
    				log.error("An exception occurred in trying to retrieve this field ", ex);
    			}
    		}
    	}
    	return constants;
    }
    private static Log log = LogFactory.getLog(EditConfigurationConstants.class);

}
