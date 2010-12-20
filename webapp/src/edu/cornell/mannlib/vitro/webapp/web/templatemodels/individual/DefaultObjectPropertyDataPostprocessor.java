/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class DefaultObjectPropertyDataPostprocessor extends
        BaseObjectPropertyDataPostprocessor {

    protected String KEY_NAME = "name";
    protected String KEY_MONIKER = "moniker";
    protected String KEY_OBJECT = "object";
    
    public DefaultObjectPropertyDataPostprocessor(ObjectPropertyTemplateModel optm, WebappDaoFactory wdf) {
        super(optm, wdf);
    }

    @Override
    /* Apply processing specific to this postprocessor */
    protected void process(Map<String, String> map) {
        addName(map, KEY_NAME, KEY_OBJECT);
        addMoniker(map, KEY_MONIKER, KEY_OBJECT);
    }


    
}
