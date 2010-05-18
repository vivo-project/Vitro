/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

// For now this class just holds constants for creating links. Could later be used to implement custom routing,
// if we go that route.
public class Router {

    public static final String ABOUT = "/about";
    public static final String CONTACT = "/comments";
    public static final String BROWSE = "/browse";
    public static final String INDIVIDUAL = "/individual";
    public static final String INDIVIDUAL_LIST = "/individuallist";
    public static final String SEARCH = "/search"; 
    public static final String TERMS_OF_USE = "/termsOfUse";
    
    // Put these under /siteAdmin/...
    // Currently login, logout, and site admin are all the same page, but they don't have to be.
    public static final String LOGIN = "/siteAdmin";
    public static final String LOGOUT = "/siteAdmin";    
    public static final String SITE_ADMIN = "/siteAdmin"; 
 
}
