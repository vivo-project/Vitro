/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.menu;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class Menu extends ArrayList<MenuItem> {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Menu.class.getName());

    protected VitroRequest vreq;
    protected int portalId;
    protected String contextPath;
    
    public Menu(VitroRequest vreq, int portalId) {
        this.vreq = vreq;
        this.portalId = portalId;  
        contextPath = vreq.getContextPath();
    }
    
    public void addItem(String text, String path) {
        add(new MenuItem(text, getUrl(path)));
    }
    
    protected String getUrl(String path) {
        return contextPath + path;
    }
    
    
}
