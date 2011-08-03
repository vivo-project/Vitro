/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A menu item that indicates whether it is the active item or not.
 * 
 * @author rjy7
 *
 */
public class MainMenuItem extends MenuItem {
    
    private static final Log log = LogFactory.getLog(MainMenuItem.class.getName());

    private boolean active;
    
    public MainMenuItem(String linkText, String path, boolean active) {
        super(linkText, path);
        this.active = active;
    }
     
    /* Template properties */
    
    public boolean isActive() {
        return active;
    }
}
