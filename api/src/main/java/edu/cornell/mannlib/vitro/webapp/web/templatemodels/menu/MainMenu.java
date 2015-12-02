/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/** A menu that can indicate the active item.
 * 
 * @author rjy7
 *
 */
public class MainMenu extends Menu {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(MainMenu.class);
    
    protected VitroRequest vreq;
    
    public MainMenu() { }
    
    public MainMenu(VitroRequest vreq) {
        this.vreq = vreq;
    }

    public void addItem(String text, String path) {
        boolean isActive = isActiveItem(path);
        addItem(text, path, isActive);
    }  

    public void addItem(String text, String path, boolean isActive) {
        MainMenuItem i = new MainMenuItem(text, path, isActive);
        items.add(i);
    }
    
    protected boolean isActiveItem(String path) {
        return  vreq != null && vreq.getServletPath().equals(path);                    
    }
}
