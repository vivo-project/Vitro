/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/** The object property data postprocessor that is used if the view does not specify another postprocessor */
public class MaintainDuplicatesObjectPropertyDataPostProcessor extends BaseObjectPropertyDataPostProcessor {

    private static final Log log = LogFactory.getLog(DefaultObjectPropertyDataPostProcessor.class);

    public MaintainDuplicatesObjectPropertyDataPostProcessor(ObjectPropertyTemplateModel optm,
            WebappDaoFactory wdf) {
        super(optm, wdf);
    }

    @Override
    protected void process(Map<String, String> map) {
        // no default data postprocessing defined yet
    }

    @Override
    public void process(List<Map<String, String>> data) {
    	 if (data.isEmpty()) {
             log.debug("No data to postprocess for property " + objectPropertyTemplateModel.getUri());
             return;
         }

         //Process list is not called as it removes duplicates

         for (Map<String, String> map : data) {
             process(map);
         }
    }

}
