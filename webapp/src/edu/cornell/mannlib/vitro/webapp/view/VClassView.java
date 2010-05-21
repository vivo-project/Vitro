/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.Routes;

public class VClassView extends ViewObject {
    
    private static final Log log = LogFactory.getLog(VClassView.class.getName());
    private static final String URL = Routes.INDIVIDUAL_LIST;
    
    private VClass vclass;
    
    public VClassView(VClass vclass) {
        this.vclass = vclass;
    }

    public String getName() {
        return vclass.getName();
    }
    
    public String getUrl() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("vclassId", vclass.getURI());
        return getUrl(URL, params);
    }
    
    public int getIndividualCount() {
        return vclass.getEntityCount();
    }

}
