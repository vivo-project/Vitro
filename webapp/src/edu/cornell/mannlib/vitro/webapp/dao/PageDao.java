/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Map;
import java.util.List;
public interface PageDao {

    Map<String, Object> getPage(String pageUri);
    
    /**
     * Returns a list of urlMappings to URIs.
     * 
     * @return
     */
    Map<String, String> getPageMappings();
    
    /**
     * Returns URI of home page.
     * @return
     */
    String getHomePageUri();
   
    String getClassGroupPage(String pageUri);
    
    List<String> getClassIntersections(String pageUri);
}
