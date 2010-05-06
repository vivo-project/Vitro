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
    
    public MainMenu(VitroRequest vreq, int portalId) {
        super(vreq, portalId);
    }

    public void addItem(String text, String path) {
        String url = getUrl(path);
        boolean active = vreq.getServletPath().equals(path);
        MainMenuItem i = new MainMenuItem(text, url, active);
        items.add(i);
    }    
}
