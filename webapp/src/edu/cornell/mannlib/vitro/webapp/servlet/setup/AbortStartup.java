/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;

/**
 * Provide a mechanism so a ServletContextListener can set a flag that tells
 * other ServletContextListeners not to run.
 * 
 * The listener that detects a problem should still throw an exception, so
 * Tomcat will declare that the startup has failed and mark the application as
 * not runnable. However, Tomcat will still run the other listeners before
 * giving up. Hence, the need for this flag.
 * 
 * If the other listeners are looking for this flag, they can (and should)
 * decide to simply exit rather than attempting to initialize.
 */
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
