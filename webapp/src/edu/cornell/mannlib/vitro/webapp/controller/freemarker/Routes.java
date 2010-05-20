/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

// For now this class just holds constants for creating links. Could later be used to implement custom routing,
// if we go that route. Separating from Controllers to keep track of which ones are being used with FreeMarker
// Controllers; can recombine later if desired.
public class Routes {

    public static final String ABOUT = "/about";
    public static final String BROWSE = "/browse";
    public static final String COMMENT_FORM = "/comments";
    public static final String INDIVIDUAL = "/individual";
    public static final String INDIVIDUAL_LIST = "/entitylist"; // change
    public static final String SEARCH = "/search"; 
    public static final String TERMS_OF_USE = "/termsOfUse";
    
    // Put these under /admin/...
    // Currently login and site admin are on the same page, but they don't have to be.
    public static final String LOGIN = "/siteAdmin";
    public static final String LOGOUT = "/login_process.jsp"; 
    public static final String SITE_ADMIN = "/siteAdmin"; 
 
}
