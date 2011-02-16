/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;

public class AbortStartup {

    private static final String ATTRIBUTE_NAME = AbortStartup.class.getName();
    
    /**
     * Sets a context attribute to prevent other context listeners from running.
     */
    public static void abortStartup(ServletContext context) {
        context.setAttribute(ATTRIBUTE_NAME, new Boolean(true));
    }
    
    /**
     * Checks whether a previous context listener has caused startup to be aborted.
     */
    public static boolean isStartupAborted(ServletContext context) {
        return (context.getAttribute(ATTRIBUTE_NAME) != null);
    }
    
}
