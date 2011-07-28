/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;

import org.json.JSONObject;

public interface PageDataGetter{
    Map<String,Object> getData(ServletContext contect, VitroRequest vreq, String pageUri, Map<String, Object> page );
    
    /** Gets the type that this class applies to */
    //This has been changed to return the class name for data getter used in menu management
    String getType();
    
    //get data service url based on data getter requirements
  //Get data servuice
    String getDataServiceUrl();
    
    /**Convert data to JSONObject based on what's required for the data processing**/
   JSONObject convertToJSON(Map<String, Object> map, VitroRequest vreq);
}
