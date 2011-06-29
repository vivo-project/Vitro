/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/** Data postprocessor for default object property list view **/
public class DefaultListViewDataPostProcessor extends
        BaseObjectPropertyDataPostProcessor {

    private static final Log log = LogFactory.getLog(DefaultListViewDataPostProcessor.class); 
    
    private static final String KEY_NAME = "name";
    private static final String KEY_OBJECT = "object";
    
    public DefaultListViewDataPostProcessor(ObjectPropertyTemplateModel optm, WebappDaoFactory wdf) {
        super(optm, wdf);
    }

    @Override
    /* Apply processing specific to this postprocessor */
    protected void process(Map<String, String> map) {
        addName(map, KEY_NAME, KEY_OBJECT);
    }
 
}