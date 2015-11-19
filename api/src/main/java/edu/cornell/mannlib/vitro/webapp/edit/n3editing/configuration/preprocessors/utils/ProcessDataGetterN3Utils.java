/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class determines what n3 should be returned for a particular data getter and can be overwritten or extended in VIVO. 
 */
public class ProcessDataGetterN3Utils {
    private static final Log log = LogFactory.getLog(ProcessDataGetterN3Utils.class);
    
    public static ProcessDataGetterN3 getDataGetterProcessorN3(String dataGetterClass, JSONObject jsonObject) {
    	HashMap<String, String> map = ProcessDataGetterN3Map.getDataGetterTypeToProcessorMap();
    	//
    	if(map.containsKey(dataGetterClass)) {
    		String processorClass = map.get(dataGetterClass);
    		try {
    			ProcessDataGetterN3 pn = instantiateClass(processorClass, jsonObject);
    			return pn;
    		} catch(Exception ex) {
    			log.error("Exception occurred in trying to get processor class for n3 for " + dataGetterClass, ex);
    			return null;
    		}
    	}
    	return null;
    }
    
    private static ProcessDataGetterN3 instantiateClass(String processorClass, JSONObject jsonObject) {
    	ProcessDataGetterN3 pn = null;
    	try {
	    	Class<?> clz = Class.forName(processorClass);
	    	Constructor<?>[] ctList = clz.getConstructors();
	    	for (Constructor<?> ct: ctList) {
		    	Class<?>[] parameterTypes =ct.getParameterTypes();
				if(parameterTypes.length > 0 && parameterTypes[0].isAssignableFrom(jsonObject.getClass())) {
						 pn = (ProcessDataGetterN3) ct.newInstance(jsonObject);
				} 	else {
						pn = (ProcessDataGetterN3) ct.newInstance();
				} 
	    	}
		
    	} catch(Exception ex) {
			log.error("Error occurred instantiating " + processorClass, ex);
		}
    	return pn;
        		
    }
    
}