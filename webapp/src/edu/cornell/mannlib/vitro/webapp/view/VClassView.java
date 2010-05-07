/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;

public class VClassView extends ViewObject {
    
    private static final Log log = LogFactory.getLog(VClassView.class.getName());
    private static final String URL = "/individuallistFM?vclassId=";
    
    private VClass vclass;
    
    public VClassView(VClass vclass) {
        this.vclass = vclass;
    }

    public String getName() {
        return vclass.getName();
    }
    
    public String getUrl() {
        return contextPath + URL + encodeUrl(vclass.getURI()); 
    }
    
    public int getEntityCount() {
        return vclass.getEntityCount();
    }

}
