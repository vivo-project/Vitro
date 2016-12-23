/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Map;
import java.util.List;
public interface PageDao {

    Map<String, Object> getPage(String pageUri);
    
    /**
     * Returns a list of urlMappings to URIs.
     */
    Map<String, String> getPageMappings();
    
    /**
     * Returns URI of home page.
     */
    String getHomePageUri();
   
    String getClassGroupPage(String pageUri);
        
    Map<String, Object> getClassesAndRestrictionsForPage(String pageUri);

    Map<String, Object> getClassesAndCheckInternal(String pageUri);
    
    List<String> getDataGetterClass(String pageUri);

    /**
     * Gets the required actions directly associated with a page.
     * Does not get required actions for any data getters that are
     * related to the page.
     */
    List<String> getRequiredActions(String pageUri);
}
