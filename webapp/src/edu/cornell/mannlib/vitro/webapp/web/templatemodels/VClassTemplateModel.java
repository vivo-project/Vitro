/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;

public class VClassTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(VClassTemplateModel.class);
    private static final String PATH = Route.INDIVIDUAL_LIST.path();
    
    private final VClass vclass;
    
    public VClassTemplateModel(VClass vclass) {
        this.vclass = vclass;
    }

    public String getUri(){
        return vclass.getURI();
    }
    
    public String getName() {
        return vclass.getName();
    }
    
    public String getUrl() {
        return getUrl(PATH, new ParamMap("vclassId", vclass.getURI()));
    }
    
    public int getIndividualCount() {
        return vclass.getEntityCount();
    }

    public VClassGroupTemplateModel getGroup() {
        VClassGroup group = vclass.getGroup();
        return (group == null) ? null : new VClassGroupTemplateModel(vclass.getGroup());
    }
}
