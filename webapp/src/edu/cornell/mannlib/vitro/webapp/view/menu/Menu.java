/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.view.ViewObject;

public class Menu extends ViewObject {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Menu.class.getName());

    protected VitroRequest vreq;
    protected int portalId;
    protected List<MenuItem> items;
    
    public Menu(VitroRequest vreq, int portalId) {
        this.vreq = vreq;
        this.portalId = portalId;  
        items = new ArrayList<MenuItem>();
    }
    
    public void addItem(String text, String path) {
        items.add(new MenuItem(text, path));
    }
    
    public List<MenuItem> getItems() {
        return items;
    }  
    
}
