/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

import edu.cornell.mannlib.vitro.webapp.beans.Property;

public class RequestActionConstants {
    public static String actionNamespace = "java:";
    
    public static String SOME_URI = "?SOME_URI";
    public static Property SOME_PREDICATE = new Property(SOME_URI);
    public static String SOME_LITERAL = "?SOME_LITERAL";
}
