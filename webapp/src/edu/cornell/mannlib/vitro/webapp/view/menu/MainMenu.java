/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.menu;

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
    private static final Log log = LogFactory.getLog(MainMenu.class.getName());
    
    protected VitroRequest vreq;
    
    public MainMenu(VitroRequest vreq) {
        this.vreq = vreq;
    }

    public void addItem(String text, String path) {
        boolean active = isActiveItem(path);
        MainMenuItem i = new MainMenuItem(text, path, active);
        items.add(i);
    }  
    
    // RY NEED TO FIX: doesn't work for Home and other generated tabs:
    // vreq.getServletPath() = /templates/page/basicPage.jsp
    // vreq.getRequestURL() = http://localhost:8080/vivo/templates/page/basicPage.jsp
    // vreq.getRequestURI() = /vivo/templates/page/basicPage.jsp
    private boolean isActiveItem(String path) {
        return vreq.getServletPath().equals(path);
    }
}
