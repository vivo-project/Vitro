/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/** The object property data postprocessor that is used if the view does not specify another postprocessor */
public class DefaultObjectPropertyDataPostProcessor extends BaseObjectPropertyDataPostProcessor {

    private static final Log log = LogFactory.getLog(DefaultObjectPropertyDataPostProcessor.class);   
    
    public DefaultObjectPropertyDataPostProcessor(ObjectPropertyTemplateModel optm,
            WebappDaoFactory wdf) {
        super(optm, wdf);
    }

    @Override
    protected void process(Map<String, String> map) {
        // no default data postprocessing defined yet
    }

}
