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
    protected void process(Map<String, String> map) {
        addName(map);
        addMoniker(map);
    }

    private void addName(Map<String, String> map) {
        String name = map.get("name");
        if (name == null) {
            map.put("name", getName(map.get("object")));
        }
    }
    /* This is a temporary measure to handle the fact that the current Individual.getMoniker()
     * method returns the individual's VClass if moniker is null. We want to replicate that
     * behavior here, but in future the moniker property (along with other Vitro namespace
     * properties) will be removed. In addition, this type of logic (display x if it exists, otherwise y)
     * will be moved into the display modules (Editing and Display Configuration Improvements).
     */
    private void addMoniker(Map<String, String> map) {
        String moniker = map.get("moniker");
        if (moniker == null) {
            map.put("moniker", getMoniker(map.get("object")));
        }
    }
    
}
