/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Map;

/** 
 * These preprocessors take a list of object property statement data derived from a 
 * SPARQL query (or other source) and prepare it for insertion into the template data model.
 * 
 * @author rjy7
 *
 */

public interface ObjectPropertyDataPreprocessor {

    public void process(List<Map<String, String>> data);

}
