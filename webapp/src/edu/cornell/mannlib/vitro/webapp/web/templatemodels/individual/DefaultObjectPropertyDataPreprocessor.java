/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class DefaultObjectPropertyDataPreprocessor extends
        BaseObjectPropertyDataPreprocessor {

    public DefaultObjectPropertyDataPreprocessor(ObjectPropertyTemplateModel optm, WebappDaoFactory wdf) {
        super(optm, wdf);
    }

    @Override
    /* Apply preprocessing specific to this preprocessor */
    protected void applySpecificPreprocessing(Map<String, String> map) {
        addName(map);
        addMoniker(map);
    }

    private void addName(Map<String, String> map) {
        String name = map.get("name");
        if (name == null) {
            map.put("name", getName(map.get("object")));
        }
    }
    
    private void addMoniker(Map<String, String> map) {
        String moniker = map.get("moniker");
        if (moniker == null) {
            map.put("moniker", getMoniker(map.get("object")));
        }
    }
    
}
