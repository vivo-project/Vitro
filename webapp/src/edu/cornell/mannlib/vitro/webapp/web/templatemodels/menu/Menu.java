/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class Menu extends BaseTemplateModel {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Menu.class.getName());

    protected List<MenuItem> items;
    
    public Menu() {  
        items = new ArrayList<MenuItem>();
    }
    
    public void addItem(String text, String path) {
        items.add(new MenuItem(text, path));
    }
    
    /* Template properties */
    
    public List<MenuItem> getItems() {
        return items;
    }  
    
}
