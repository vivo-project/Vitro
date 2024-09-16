/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * This class determines what n3 should be returned for a particular data getter and can be overwritten or extended in VIVO.
 */
public class ProcessDataGetterN3Utils {
    private static final Log log = LogFactory.getLog(ProcessDataGetterN3Utils.class);

    public static ProcessDataGetterN3 getDataGetterProcessorN3(String dataGetterClass, ObjectNode jsonObject) {
    	HashMap<String, Class> map = ProcessDataGetterN3Map.getDataGetterTypeToProcessorMap();
    	//
    	if(map.containsKey(dataGetterClass)) {
    		Class<?> processorClass = map.get(dataGetterClass);
    		try {
    			ProcessDataGetterN3 pn = instantiateClass(processorClass, jsonObject);
    			return pn;
    		} catch(Exception ex) {
    			log.error("Exception occurred in trying to get processor class for n3 for " + dataGetterClass, ex);
    			return null;
    		} 
    	}
    	log.error(ProcessDataGetterN3Map.class.getSimpleName() + " doesn't contain processor class for n3 for " + dataGetterClass);
    	return null;
    }

    private static ProcessDataGetterN3 instantiateClass(Class<?> processorClass, ObjectNode jsonObject) {
    	ProcessDataGetterN3 pn = null;
    	try {
	    	Constructor<?>[] ctList = processorClass.getConstructors();
	    	for (Constructor<?> ct: ctList) {
		    	Class<?>[] parameterTypes =ct.getParameterTypes();
				if(parameterTypes.length > 0 && parameterTypes[0].isAssignableFrom(jsonObject.getClass())) {
						 pn = (ProcessDataGetterN3) ct.newInstance(jsonObject);
				} 	else {
						pn = (ProcessDataGetterN3) ct.newInstance();
				}
	    	}

    	} catch(Exception ex) {
			log.error("Error occurred instantiating " + processorClass.getCanonicalName(), ex);
		}
    	return pn;

    }

}
