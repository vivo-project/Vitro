/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.menuManagement;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class includes methods that help in selecting a data getter based on 
 * parameters, and VIVO will have its own version or extend this
 */
public class MenuManagementDataUtils {
    private static final Log log = LogFactory.getLog(MenuManagementDataUtils.class);

    private static IMenuManagementDataUtils impl = null;

    //Data that is to be returned to template that does not involve data getters
    //e.g. what are the current class groups, etc.
    public static void includeRequiredSystemData(ServletContext context, Map<String, Object> templateData) {
    	if (impl != null) {
            impl.includeRequiredSystemData(context, templateData);
        }
    }

    public static void setImplementation(IMenuManagementDataUtils impl) {
        MenuManagementDataUtils.impl = impl;
    }
    

    public interface IMenuManagementDataUtils {
        public void includeRequiredSystemData(ServletContext context, Map<String, Object> templateData);
    }
    
}