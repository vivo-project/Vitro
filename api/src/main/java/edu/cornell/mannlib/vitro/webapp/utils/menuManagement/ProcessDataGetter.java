/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.menuManagement;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public interface ProcessDataGetter{
    public void populateTemplate(HttpServletRequest req, Map<String, Object> pageData, Map<String, Object> templateData);

    public  Model processSubmission(VitroRequest vreq, Resource dataGetterResource);
    
    
}
