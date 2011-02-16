/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public interface PageDataGetter{
    Map<String,Object> getData(ServletContext contect, VitroRequest vreq, String pageUri, Map<String, Object> page, String type );
    
    /** Gets the type that this class applies to */
    String getType();
}
