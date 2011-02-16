/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Map;

/** 
 * These postprocessors take a list of object property statement data returned from a 
 * SPARQL query and prepare it for insertion into the template data model.
 * 
 * @author rjy7
 *
 */

public interface ObjectPropertyDataPostProcessor {

    public void process(List<Map<String, String>> data);

}
